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
        List<Member> members = memberRepository.search(emptyToNull(keyword), emptyToNull(status));
        syncMemberStatusesByExpireDate(members);
        return members;
    }

    @CacheEvict(value = {"members", "dashboard"}, allEntries = true)
    @Transactional
    public Member saveMember(Member member) {
        Member target = member;
        if (member.getId() != null) {
            target = memberRepository.findById(member.getId())
                .orElseThrow(() -> new RuntimeException("会员不存在"));
            target.setName(member.getName());
            target.setPhone(member.getPhone());
            target.setGoal(member.getGoal());
            target.setHealthTracking(member.getHealthTracking());
            target.setExpireDate(member.getExpireDate());
            target.setLevel(member.getLevel());
            target.setStatus(member.getStatus());
            if (member.getFrozenAt() != null) {
                target.setFrozenAt(member.getFrozenAt());
            }
        }
        if (target.getStatus() == null || target.getStatus().isEmpty()) {
            target.setStatus("NORMAL");
        }
        if (target.getId() == null && (target.getLevel() == null || target.getLevel().isEmpty())) {
            target.setLevel("Bronze");
        }
        if (!"FROZEN".equals(target.getStatus())) {
            target.setFrozenAt(null);
        }
        normalizeMemberStatus(target);
        return memberRepository.save(target);
    }

    @CacheEvict(value = {"members", "dashboard"}, allEntries = true)
    @Transactional
    public void deleteMember(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new RuntimeException("会员不存在"));
        if (!consumptionRepository.findByMember_IdOrderByCreatedAtDesc(id).isEmpty()) {
            throw new RuntimeException("该会员有消费记录，不能直接删除，请先保留历史或改为逻辑删除");
        }
        if (!membershipCardRepository.findByMemberIdOrderByIdDesc(id).isEmpty()) {
            throw new RuntimeException("该会员有会员卡记录，不能直接删除，请先保留历史或改为逻辑删除");
        }
        memberRepository.delete(member);
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
        int days = validDays > 0 ? validDays : 30;
        LocalDate from = computeNextCardValidFrom(memberId);
        card.setValidFrom(from);
        card.setValidTo(from.plusDays(days));
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
        syncMemberExpireFromActiveCards(memberId);
        return saved;
    }

    @Transactional
    @CacheEvict(value = {"members", "dashboard"}, allEntries = true)
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
        syncMemberExpireFromActiveCards(card.getMember().getId());
        return saved;
    }

    @Transactional
    @CacheEvict(value = {"members", "dashboard"}, allEntries = true)
    public MembershipCard reportLost(Long cardId) {
        MembershipCard card = membershipCardRepository.findById(cardId).orElseThrow(() -> new RuntimeException("会员卡不存在"));
        card.setStatus("LOST");
        MembershipCard saved = membershipCardRepository.save(card);
        syncMemberExpireFromActiveCards(card.getMember().getId());
        return saved;
    }

    @Transactional
    @CacheEvict(value = {"members", "dashboard"}, allEntries = true)
    public MembershipCard cancelCard(Long cardId) {
        MembershipCard card = membershipCardRepository.findById(cardId).orElseThrow(() -> new RuntimeException("会员卡不存在"));
        card.setStatus("CANCELLED");
        MembershipCard saved = membershipCardRepository.save(card);
        syncMemberExpireFromActiveCards(card.getMember().getId());
        return saved;
    }

    /**
     * 新办时间卡接在已有 ACTIVE 卡之后：从「当前 ACTIVE 中最晚 validTo 的次日」起算；
     * 若该日早于今天（例如旧卡已过期未处理），则从今天起算；无任何 ACTIVE 卡则从今天起算。
     */
    private LocalDate computeNextCardValidFrom(Long memberId) {
        LocalDate today = LocalDate.now();
        LocalDate latestEnd = null;
        for (MembershipCard c : membershipCardRepository.findByMemberIdOrderByIdDesc(memberId)) {
            if (!"ACTIVE".equals(c.getStatus()) || c.getValidTo() == null) {
                continue;
            }
            if (latestEnd == null || c.getValidTo().isAfter(latestEnd)) {
                latestEnd = c.getValidTo();
            }
        }
        if (latestEnd == null) {
            return today;
        }
        LocalDate candidate = latestEnd.plusDays(1);
        return candidate.isBefore(today) ? today : candidate;
    }

    /**
     * 将会员「到期日」设为当前所有状态为 ACTIVE 的卡中 validTo 的最晚一天；若无有效卡则为 null。
     */
    private void syncMemberExpireFromActiveCards(Long memberId) {
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) {
            return;
        }
        LocalDate maxTo = null;
        for (MembershipCard c : membershipCardRepository.findByMemberIdOrderByIdDesc(memberId)) {
            if ("ACTIVE".equals(c.getStatus()) && c.getValidTo() != null) {
                if (maxTo == null || c.getValidTo().isAfter(maxTo)) {
                    maxTo = c.getValidTo();
                }
            }
        }
        member.setExpireDate(maxTo);
        normalizeMemberStatus(member);
        memberRepository.save(member);
    }

    private void syncMemberStatusesByExpireDate(List<Member> members) {
        if (members == null || members.isEmpty()) {
            return;
        }
        LocalDate today = LocalDate.now();
        boolean changed = false;
        for (Member member : members) {
            if (normalizeMemberStatus(member, today)) {
                changed = true;
            }
        }
        if (changed) {
            memberRepository.saveAll(members);
        }
    }

    public Member freezeMember(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("会员不存在"));
        if ("FROZEN".equals(member.getStatus())) {
            return member;
        }
        member.setFrozenAt(LocalDate.now());
        member.setStatus("FROZEN");
        memberRepository.save(member);
        return member;
    }

    public Member unfreezeMember(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("会员不存在"));
        if (!"FROZEN".equals(member.getStatus())) {
            return member;
        }
        LocalDate frozenAt = member.getFrozenAt();
        LocalDate today = LocalDate.now();
        if (frozenAt != null) {
            int frozenDays = (int) java.time.temporal.ChronoUnit.DAYS.between(frozenAt, today);
            if (frozenDays > 0) {
                extendActiveCards(member.getId(), frozenDays);
            }
        }
        member.setFrozenAt(null);
        normalizeMemberStatus(member);
        memberRepository.save(member);
        return member;
    }

    private void extendActiveCards(Long memberId, int days) {
        if (days <= 0) {
            return;
        }
        List<MembershipCard> cards = membershipCardRepository.findByMemberIdOrderByIdDesc(memberId);
        boolean changed = false;
        for (MembershipCard c : cards) {
            if ("ACTIVE".equals(c.getStatus()) && c.getValidTo() != null) {
                c.setValidTo(c.getValidTo().plusDays(days));
                changed = true;
            }
        }
        if (changed) {
            membershipCardRepository.saveAll(cards);
            syncMemberExpireFromActiveCards(memberId);
        }
    }

    private void normalizeMemberStatus(Member member) {
        normalizeMemberStatus(member, LocalDate.now());
    }

    private boolean normalizeMemberStatus(Member member, LocalDate today) {
        if (member == null) {
            return false;
        }
        if ("FROZEN".equals(member.getStatus())) {
            return false;
        }
        LocalDate expireDate = member.getExpireDate();
        if (expireDate != null && expireDate.isBefore(today) && !"EXPIRED".equals(member.getStatus())) {
            member.setStatus("EXPIRED");
            return true;
        }
        return false;
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
