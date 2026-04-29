package com.gym.system.service;

import com.gym.system.entity.Booking;
import com.gym.system.entity.Coach;
import com.gym.system.entity.Course;
import com.gym.system.entity.Member;
import com.gym.system.entity.MembershipCard;
import com.gym.system.entity.SysUser;
import com.gym.system.entity.SystemMessage;
import com.gym.system.repository.BookingRepository;
import com.gym.system.repository.CoachRepository;
import com.gym.system.repository.ConsumptionRepository;
import com.gym.system.repository.CourseRepository;
import com.gym.system.repository.MembershipCardRepository;
import com.gym.system.repository.MemberRepository;
import com.gym.system.repository.SysUserRepository;
import com.gym.system.repository.SystemMessageRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ManageService {
    private final MemberRepository memberRepository;
    private final MembershipCardRepository membershipCardRepository;
    private final CourseRepository courseRepository;
    private final CoachRepository coachRepository;
    private final BookingRepository bookingRepository;
    private final SystemMessageRepository systemMessageRepository;
    private final SysUserRepository sysUserRepository;
    private final GymService gymService;
    private final ConsumptionRepository consumptionRepository;
    private final PasswordEncoder passwordEncoder;

    public ManageService(MemberRepository memberRepository, MembershipCardRepository membershipCardRepository,
                         CourseRepository courseRepository, CoachRepository coachRepository,
                         BookingRepository bookingRepository, SystemMessageRepository systemMessageRepository,
                         SysUserRepository sysUserRepository, GymService gymService,
                         ConsumptionRepository consumptionRepository,
                         PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.membershipCardRepository = membershipCardRepository;
        this.courseRepository = courseRepository;
        this.coachRepository = coachRepository;
        this.bookingRepository = bookingRepository;
        this.systemMessageRepository = systemMessageRepository;
        this.sysUserRepository = sysUserRepository;
        this.gymService = gymService;
        this.consumptionRepository = consumptionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Member> searchMembers(String keyword, String status) {
        return memberRepository.search(emptyToNull(keyword), emptyToNull(status));
    }

    @CacheEvict(value = {"members", "dashboard"}, allEntries = true)
    @Transactional
    public Member saveMember(Member member) {
        if (member.getId() == null && (member.getStatus() == null || member.getStatus().isEmpty())) {
            member.setStatus("NORMAL");
        }
        if (member.getId() == null && (member.getLevel() == null || member.getLevel().isEmpty())) {
            member.setLevel("Bronze");
        }
        return memberRepository.save(member);
    }

    @CacheEvict(value = {"members", "dashboard"}, allEntries = true)
    @Transactional
    public void deleteMember(Long id) {
        memberRepository.deleteById(id);
    }

    public Member getMember(Long id) {
        return memberRepository.findById(id).orElseThrow(() -> new RuntimeException("会员不存在"));
    }

    public List<MembershipCard> listCardsByMember(Long memberId) {
        return membershipCardRepository.findByMemberIdOrderByIdDesc(memberId);
    }

    @Transactional
    @CacheEvict(value = {"members", "dashboard"}, allEntries = true)
    public MembershipCard issueCard(Long memberId, String cardType, Double payAmount, Integer remainingTimes,
                                    int validDays) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("会员不存在"));
        MembershipCard card = new MembershipCard();
        card.setMember(member);
        card.setCardType(cardType);
        card.setStatus("ACTIVE");
        card.setValidFrom(LocalDate.now());
        card.setValidTo(LocalDate.now().plusDays(validDays > 0 ? validDays : 30));
        if ("COUNT".equals(cardType)) {
            card.setRemainingTimes(remainingTimes != null ? remainingTimes : 0);
            card.setBalance(0.0);
        } else {
            card.setBalance(payAmount != null ? payAmount : 0);
            card.setRemainingTimes(0);
        }
        MembershipCard saved = membershipCardRepository.save(card);
        if (payAmount != null && payAmount > 0) {
            gymService.createConsumption(memberId, "办卡", payAmount);
        }
        return saved;
    }

    @Transactional
    public MembershipCard renewCard(Long cardId, Double payAmount, int extendDays) {
        MembershipCard card = membershipCardRepository.findById(cardId).orElseThrow(() -> new RuntimeException("会员卡不存在"));
        if ("CANCELLED".equals(card.getStatus()) || "LOST".equals(card.getStatus())) {
            throw new RuntimeException("卡状态不可续费");
        }
        LocalDate end = card.getValidTo() != null ? card.getValidTo() : LocalDate.now();
        card.setValidTo(end.plusDays(extendDays > 0 ? extendDays : 30));
        MembershipCard saved = membershipCardRepository.save(card);
        if (payAmount != null && payAmount > 0) {
            gymService.createConsumption(card.getMember().getId(), "续费", payAmount);
        }
        return saved;
    }

    @Transactional
    public MembershipCard reportLost(Long cardId) {
        MembershipCard card = membershipCardRepository.findById(cardId).orElseThrow(() -> new RuntimeException("会员卡不存在"));
        card.setStatus("LOST");
        return membershipCardRepository.save(card);
    }

    @Transactional
    public MembershipCard cancelCard(Long cardId) {
        MembershipCard card = membershipCardRepository.findById(cardId).orElseThrow(() -> new RuntimeException("会员卡不存在"));
        card.setStatus("CANCELLED");
        return membershipCardRepository.save(card);
    }

    @CacheEvict(value = {"courses", "dashboard"}, allEntries = true)
    @Transactional
    public Course updateCourse(Long id, String title, String slot, Integer capacity, String category,
                               Long coachId, Boolean enabled) {
        Course c = courseRepository.findById(id).orElseThrow(() -> new RuntimeException("课程不存在"));
        if (title != null) c.setTitle(title);
        if (slot != null) c.setSlot(slot);
        if (category != null) c.setCategory(category);
        if (enabled != null) c.setEnabled(enabled);
        if (coachId != null) {
            Coach coach = coachRepository.findById(coachId).orElseThrow(() -> new RuntimeException("教练不存在"));
            c.setCoach(coach);
        }
        if (capacity != null && !capacity.equals(c.getCapacity())) {
            int diff = capacity - c.getCapacity();
            c.setCapacity(capacity);
            int rem = c.getRemainingSlots() != null ? c.getRemainingSlots() + diff : capacity;
            c.setRemainingSlots(Math.max(0, rem));
        }
        return courseRepository.save(c);
    }

    public List<Booking> searchBookings(Long memberId, Long courseId, String status,
                                        LocalDateTime fromTime, LocalDateTime toTime, Long coachUserCoachId) {
        List<Booking> list = bookingRepository.search(memberId, courseId, emptyToNull(status), fromTime, toTime);
        if (coachUserCoachId == null) {
            return list;
        }
        List<Booking> out = new ArrayList<>();
        for (Booking b : list) {
            if (b.getCourse().getCoach() != null && coachUserCoachId.equals(b.getCourse().getCoach().getId())) {
                out.add(b);
            }
        }
        return out;
    }

    public List<SystemMessage> listMessages() {
        return systemMessageRepository.findAllByOrderByCreatedAtDesc();
    }

    public Map<String, Object> financeSummary() {
        Map<String, Object> m = new HashMap<>();
        m.put("totalRevenue", consumptionRepository.sumAllAmount());
        m.put("monthly", consumptionRepository.sumAmountGroupByMonth());
        return m;
    }

    public Map<String, Object> chartData() {
        Map<String, Object> m = new HashMap<>();
        List<Object[]> rows = consumptionRepository.sumAmountGroupByMonth();
        List<String> months = new ArrayList<>();
        List<Double> amounts = new ArrayList<>();
        for (Object[] r : rows) {
            months.add(String.valueOf(r[0]));
            amounts.add(((Number) r[1]).doubleValue());
        }
        m.put("revenueMonths", months);
        m.put("revenueAmounts", amounts);
        m.put("bookingHeat", gymService.courseSalesRanking());
        Map<String, Object> dash = gymService.dashboard();
        m.put("memberCount", dash.get("memberCount"));
        m.put("courseCount", dash.get("courseCount"));
        return m;
    }

    @Transactional
    public void changePassword(String username, String oldPass, String newPass) {
        SysUser u = sysUserRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("用户不存在"));
        if (!verifyPassword(u, oldPass)) {
            throw new RuntimeException("原密码错误");
        }
        u.setPassword(passwordEncoder.encode(newPass));
        sysUserRepository.save(u);
    }

    /**
     * 兼容旧库明文密码，逐步升级为 BCrypt。
     */
    private boolean verifyPassword(SysUser u, String raw) {
        String stored = u.getPassword();
        if (stored == null) return false;
        boolean looksBcrypt = stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$");
        if (looksBcrypt) {
            return passwordEncoder.matches(raw, stored);
        }
        if (!raw.equals(stored)) return false;
        u.setPassword(passwordEncoder.encode(raw));
        sysUserRepository.save(u);
        return true;
    }

    public List<Course> coursesForCoach(Long coachId) {
        return courseRepository.findByCoach_Id(coachId);
    }

    private static String emptyToNull(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        return s;
    }
}
