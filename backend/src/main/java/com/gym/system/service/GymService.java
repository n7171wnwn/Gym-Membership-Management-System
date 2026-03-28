package com.gym.system.service;

import com.gym.system.entity.Booking;
import com.gym.system.entity.Coach;
import com.gym.system.entity.Consumption;
import com.gym.system.entity.Course;
import com.gym.system.entity.Member;
import com.gym.system.entity.ReminderLog;
import com.gym.system.entity.SyncLog;
import com.gym.system.entity.SystemMessage;
import com.gym.system.messaging.SyncPublisher;
import com.gym.system.repository.BookingRepository;
import com.gym.system.repository.CoachRepository;
import com.gym.system.repository.ConsumptionRepository;
import com.gym.system.repository.CourseRepository;
import com.gym.system.repository.MemberRepository;
import com.gym.system.repository.ReminderLogRepository;
import com.gym.system.repository.SyncLogRepository;
import com.gym.system.repository.SystemMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GymService {
    private final MemberRepository memberRepository;
    private final CourseRepository courseRepository;
    private final BookingRepository bookingRepository;
    private final ConsumptionRepository consumptionRepository;
    private final CoachRepository coachRepository;
    private final ReminderLogRepository reminderLogRepository;
    private final SyncLogRepository syncLogRepository;
    private final SyncPublisher syncPublisher;
    private final SystemMessageRepository systemMessageRepository;

    public GymService(MemberRepository memberRepository, CourseRepository courseRepository,
                      BookingRepository bookingRepository, ConsumptionRepository consumptionRepository,
                      CoachRepository coachRepository, ReminderLogRepository reminderLogRepository,
                      SyncLogRepository syncLogRepository, SyncPublisher syncPublisher,
                      SystemMessageRepository systemMessageRepository) {
        this.memberRepository = memberRepository;
        this.courseRepository = courseRepository;
        this.bookingRepository = bookingRepository;
        this.consumptionRepository = consumptionRepository;
        this.coachRepository = coachRepository;
        this.reminderLogRepository = reminderLogRepository;
        this.syncLogRepository = syncLogRepository;
        this.syncPublisher = syncPublisher;
        this.systemMessageRepository = systemMessageRepository;
    }

    @Cacheable("members")
    public List<Member> listMembers() { return memberRepository.findAll(); }
    @Cacheable("courses")
    public List<Course> listCourses() { return courseRepository.findAll(); }
    public List<Booking> listBookings() { return bookingRepository.findAll(); }
    public List<Consumption> listConsumptions() { return consumptionRepository.findAll(); }
    public List<Coach> listCoaches() { return coachRepository.findAll(); }
    public List<ReminderLog> listReminderLogs() { return reminderLogRepository.findAll(); }
    public List<SyncLog> listSyncLogs() { return syncLogRepository.findAll(); }

    @CacheEvict(value = {"members", "dashboard"}, allEntries = true)
    public Member createMember(Member member) {
        member.setLevel("Bronze");
        if (member.getStatus() == null || member.getStatus().isEmpty()) {
            member.setStatus("NORMAL");
        }
        Member saved = memberRepository.save(member);
        writeSyncLog("web", "miniapp", "MEMBER_CREATE", "memberId=" + saved.getId(), "SUCCESS");
        return saved;
    }

    @CacheEvict(value = {"courses", "dashboard"}, allEntries = true)
    public Coach createCoach(Coach coach) {
        Coach saved = coachRepository.save(coach);
        writeSyncLog("web", "miniapp", "COACH_CREATE", "coachId=" + saved.getId(), "SUCCESS");
        return saved;
    }

    @CacheEvict(value = {"courses", "dashboard"}, allEntries = true)
    public Course createCourse(Course course, Long coachId) {
        Coach coach = coachRepository.findById(coachId).orElseThrow(() -> new RuntimeException("教练不存在"));
        course.setCoach(coach);
        course.setRemainingSlots(course.getCapacity());
        if (course.getCategory() == null || course.getCategory().isEmpty()) {
            course.setCategory("AEROBIC");
        }
        if (course.getEnabled() == null) {
            course.setEnabled(true);
        }
        Course saved = courseRepository.save(course);
        writeSyncLog("web", "miniapp", "COURSE_CREATE", "courseId=" + saved.getId(), "SUCCESS");
        return saved;
    }

    @Transactional
    @CacheEvict(value = {"courses", "dashboard"}, allEntries = true)
    public Booking createBooking(Long memberId, Long courseId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("会员不存在"));
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("课程不存在"));
        if (course.getEnabled() != null && !course.getEnabled()) {
            throw new RuntimeException("课程已下架");
        }
        if (!"NORMAL".equals(member.getStatus()) && member.getStatus() != null) {
            throw new RuntimeException("会员状态不可预约：" + member.getStatus());
        }
        if (bookingRepository.existsByMemberIdAndCourseIdAndStatusIn(memberId, courseId,
                java.util.Arrays.asList("PENDING", "APPROVED"))) {
            throw new RuntimeException("该课程已有待审核或已通过预约");
        }
        if (course.getRemainingSlots() == null || course.getRemainingSlots() <= 0) {
            throw new RuntimeException("课程名额已满");
        }

        Booking booking = new Booking();
        booking.setMember(member);
        booking.setCourse(course);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setStatus("PENDING");
        Booking saved = bookingRepository.save(booking);
        pushSystemMessage(member, "BOOKING_SUBMIT", "预约已提交", "您的课程「" + course.getTitle() + "」预约已提交，等待审核");
        writeSyncLog("miniapp", "web", "BOOKING_CREATE", "bookingId=" + saved.getId(), "SUCCESS");
        return saved;
    }

    @Transactional
    @CacheEvict(value = {"courses", "dashboard"}, allEntries = true)
    public Booking approveBooking(Long bookingId) {
        Booking b = bookingRepository.findById(bookingId).orElseThrow(() -> new RuntimeException("预约不存在"));
        String st = b.getStatus() == null ? "PENDING" : b.getStatus();
        if (!"PENDING".equals(st)) {
            throw new RuntimeException("只能审核待处理预约");
        }
        Course course = b.getCourse();
        if (course.getRemainingSlots() == null || course.getRemainingSlots() <= 0) {
            throw new RuntimeException("课程名额已满，无法通过");
        }
        course.setRemainingSlots(course.getRemainingSlots() - 1);
        courseRepository.save(course);
        b.setStatus("APPROVED");
        Booking saved = bookingRepository.save(b);
        pushSystemMessage(b.getMember(), "BOOKING_OK", "预约成功", "您的课程「" + course.getTitle() + "」预约已通过");
        writeSyncLog("web", "miniapp", "BOOKING_APPROVE", "bookingId=" + saved.getId(), "SUCCESS");
        return saved;
    }

    @Transactional
    @CacheEvict(value = {"courses", "dashboard"}, allEntries = true)
    public Booking cancelBooking(Long bookingId) {
        Booking b = bookingRepository.findById(bookingId).orElseThrow(() -> new RuntimeException("预约不存在"));
        String st = b.getStatus();
        if ("CANCELLED".equals(st)) {
            throw new RuntimeException("已取消");
        }
        Course course = b.getCourse();
        if ("APPROVED".equals(st)) {
            course.setRemainingSlots(course.getRemainingSlots() + 1);
            courseRepository.save(course);
        }
        b.setStatus("CANCELLED");
        Booking saved = bookingRepository.save(b);
        pushSystemMessage(b.getMember(), "BOOKING_CANCEL", "预约已取消", "课程「" + course.getTitle() + "」预约已取消");
        writeSyncLog("web", "miniapp", "BOOKING_CANCEL", "bookingId=" + saved.getId(), "SUCCESS");
        return saved;
    }

    private void pushSystemMessage(Member member, String type, String title, String content) {
        SystemMessage m = new SystemMessage();
        m.setMember(member);
        m.setMsgType(type);
        m.setTitle(title);
        m.setContent(content);
        m.setReadFlag(false);
        m.setCreatedAt(LocalDateTime.now());
        systemMessageRepository.save(m);
    }

    @Transactional
    @CacheEvict(value = {"members", "dashboard"}, allEntries = true)
    public Consumption createConsumption(Long memberId, String type, Double amount) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("会员不存在"));
        Consumption consumption = new Consumption();
        consumption.setMember(member);
        consumption.setType(type);
        consumption.setAmount(amount);
        consumption.setCreatedAt(LocalDateTime.now());
        Consumption saved = consumptionRepository.save(consumption);
        refreshMemberLevel(member);
        writeSyncLog("web", "miniapp", "CONSUMPTION_CREATE", "consumptionId=" + saved.getId(), "SUCCESS");
        return saved;
    }

    @CacheEvict(value = {"members", "dashboard"}, allEntries = true)
    public Member updateHealth(Long memberId, String healthTracking) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("会员不存在"));
        member.setHealthTracking(healthTracking);
        Member saved = memberRepository.save(member);
        writeSyncLog("miniapp", "web", "HEALTH_UPDATE", "memberId=" + saved.getId(), "SUCCESS");
        return saved;
    }

    @Cacheable("dashboard")
    public Map<String, Object> dashboard() {
        Map<String, Object> map = new HashMap<>();
        map.put("totalRevenue", consumptionRepository.sumAllAmount());
        LocalDate now = LocalDate.now();
        LocalDate next3 = now.plusDays(3);
        long expiringSoon = memberRepository.findAll().stream()
            .filter(m -> m.getExpireDate() != null && !m.getExpireDate().isBefore(now) && !m.getExpireDate().isAfter(next3))
            .count();
        map.put("expiringSoon", expiringSoon);
        map.put("memberCount", memberRepository.count());
        map.put("courseCount", courseRepository.count());
        map.put("coachCount", coachRepository.count());
        map.put("syncLogCount", syncLogRepository.count());
        return map;
    }

    public List<Map<String, Object>> courseSalesRanking() {
        return bookingRepository.findAll().stream()
            .filter(b -> "APPROVED".equals(b.getStatus()))
            .collect(Collectors.groupingBy(
            b -> b.getCourse().getTitle(), Collectors.counting()
        )).entrySet().stream().map(e -> {
            Map<String, Object> row = new HashMap<>();
            row.put("courseTitle", e.getKey());
            row.put("count", e.getValue());
            return row;
        }).collect(Collectors.toList());
    }

    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional
    public void generateExpiryReminderDaily() {
        LocalDate now = LocalDate.now();
        LocalDate next3 = now.plusDays(3);
        List<Member> members = memberRepository.findAll().stream()
            .filter(m -> m.getExpireDate() != null && !m.getExpireDate().isBefore(now) && !m.getExpireDate().isAfter(next3))
            .collect(Collectors.toList());
        for (Member member : members) {
            ReminderLog log = new ReminderLog();
            log.setMember(member);
            log.setChannel("WECHAT_TEMPLATE");
            log.setMessage("会员即将在3天内到期，请及时续费");
            log.setCreatedAt(LocalDateTime.now());
            reminderLogRepository.save(log);
            writeSyncLog("web", "miniapp", "REMINDER_PUSH", "memberId=" + member.getId(), "SUCCESS");
        }
    }

    public void triggerReminderNow() {
        generateExpiryReminderDaily();
    }

    private void refreshMemberLevel(Member member) {
        double total = consumptionRepository.sumAmountByMemberId(member.getId());
        if (total >= 5000) member.setLevel("Gold");
        else if (total >= 2000) member.setLevel("Silver");
        else member.setLevel("Bronze");
        memberRepository.save(member);
    }

    private void writeSyncLog(String source, String target, String action, String payload, String status) {
        SyncLog log = new SyncLog();
        log.setSourceTerminal(source);
        log.setTargetTerminal(target);
        log.setActionType(action);
        log.setPayload(payload);
        log.setStatus(status);
        log.setCreatedAt(LocalDateTime.now());
        syncLogRepository.save(log);
        syncPublisher.publish(action, payload);
    }
}
