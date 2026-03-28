package com.gym.system.config;

import com.gym.system.entity.Booking;
import com.gym.system.entity.Coach;
import com.gym.system.entity.Consumption;
import com.gym.system.entity.Course;
import com.gym.system.entity.Member;
import com.gym.system.entity.MembershipCard;
import com.gym.system.entity.SystemMessage;
import com.gym.system.repository.CoachRepository;
import com.gym.system.repository.ConsumptionRepository;
import com.gym.system.repository.CourseRepository;
import com.gym.system.repository.MemberRepository;
import com.gym.system.repository.MembershipCardRepository;
import com.gym.system.repository.SystemMessageRepository;
import com.gym.system.service.GymService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DemoDataLoader {

    private static final String DEMO_PHONE = "19988880001";

    @Bean
    @Order(100)
    CommandLineRunner seedDemoData(
        @Value("${app.demo-seed:true}") boolean demoSeed,
        MemberRepository memberRepository,
        CoachRepository coachRepository,
        CourseRepository courseRepository,
        ConsumptionRepository consumptionRepository,
        MembershipCardRepository membershipCardRepository,
        SystemMessageRepository systemMessageRepository,
        GymService gymService
    ) {
        return args -> {
            if (!demoSeed || memberRepository.existsByPhone(DEMO_PHONE)) {
                return;
            }

            List<Coach> coaches = new ArrayList<>(coachRepository.findAll());
            if (coaches.stream().noneMatch(c -> "李蕾".equals(c.getName()))) {
                Coach c = new Coach();
                c.setName("李蕾");
                c.setSpecialty("力量 / 器械");
                coaches.add(coachRepository.save(c));
            }
            if (coaches.stream().noneMatch(c -> "陈静".equals(c.getName()))) {
                Coach c = new Coach();
                c.setName("陈静");
                c.setSpecialty("瑜伽 / 普拉提");
                coaches.add(coachRepository.save(c));
            }
            coaches = coachRepository.findAll();

            Coach coachDefault = coaches.get(0);
            Coach coachLi = coaches.stream().filter(c -> "李蕾".equals(c.getName())).findFirst().orElse(coachDefault);
            Coach coachChen = coaches.stream().filter(c -> "陈静".equals(c.getName())).findFirst().orElse(coachDefault);

            List<Member> members = new ArrayList<>();
            members.add(saveMember(memberRepository, "李四", "13800001002", "增肌", "NORMAL", "Silver", LocalDate.now().plusDays(60)));
            members.add(saveMember(memberRepository, "王五", "13800001003", "体脂管理", "NORMAL", "Bronze", LocalDate.now().plusDays(2)));
            members.add(saveMember(memberRepository, "钱七", "13800001004", "体态调整", "FROZEN", "Bronze", LocalDate.now().plusDays(90)));
            members.add(saveMember(memberRepository, "孙八", "13800001005", "有氧", "EXPIRED", "Bronze", LocalDate.now().minusDays(5)));
            members.add(saveMember(memberRepository, "周九", "13800001006", "综合", "NORMAL", "Gold", LocalDate.now().plusDays(180)));
            members.add(saveMember(memberRepository, "吴十", "13800001007", "拉伸", "NORMAL", "Bronze", LocalDate.now().plusDays(30)));
            members.add(saveMember(memberRepository, "郑一", "13800001008", "私教入门", "NORMAL", "Bronze", LocalDate.now().plusDays(45)));
            members.add(saveMember(memberRepository, "演示用户赵", DEMO_PHONE, "毕业论文演示数据", "NORMAL", "Silver", LocalDate.now().plusDays(365)));

            Member mZhang = memberRepository.findAll().stream()
                .filter(m -> "13800000001".equals(m.getPhone()))
                .findFirst()
                .orElse(null);

            List<Course> courses = new ArrayList<>(courseRepository.findAll());
            courses.add(saveCourse(courseRepository, "晨间流瑜伽", coachChen, "周三 07:30", 12, "YOGA"));
            courses.add(saveCourse(courseRepository, "杠铃操 · 力量", coachLi, "周二 20:00", 15, "STRENGTH"));
            courses.add(saveCourse(courseRepository, "动感单车", coachDefault, "周四 19:00", 20, "AEROBIC"));
            courses.add(saveCourse(courseRepository, "一对一私教体验", coachLi, "周六 10:00", 2, "PRIVATE"));
            courses.add(saveCourse(courseRepository, "体态矫正小班", coachChen, "周五 18:30", 8, "YOGA"));
            courses = courseRepository.findAll();

            Course cYoga = findCourseByTitle(courses, "晨间流瑜伽");
            Course cHiit = findCourseByTitle(courses, "HIIT 燃脂");
            if (cHiit == null) {
                cHiit = courses.stream().filter(c -> c.getTitle() != null && c.getTitle().contains("HIIT")).findFirst().orElse(courses.get(0));
            }
            Course cBike = findCourseByTitle(courses, "动感单车");
            Course cPrivate = findCourseByTitle(courses, "一对一私教体验");
            Course cBarbell = findCourseByTitle(courses, "杠铃操 · 力量");

            double[] amountsNum = {680, 299, 199, 1200, 88, 520, 399};
            String[] consumptionTypes = {"办卡", "买课", "私教", "续费", "其他", "办卡", "买课"};
            LocalDateTime now = LocalDateTime.now();
            for (int i = 0; i < amountsNum.length; i++) {
                Consumption cons = new Consumption();
                cons.setMember(members.get(i % members.size()));
                cons.setType(consumptionTypes[i]);
                cons.setAmount(amountsNum[i]);
                cons.setCreatedAt(now.minusMonths((i + 1) / 3).minusDays(i * 3L + 5));
                consumptionRepository.save(cons);
            }
            for (int i = 0; i < 5; i++) {
                Consumption cons = new Consumption();
                cons.setMember(members.get((i + 3) % members.size()));
                cons.setType("办卡");
                cons.setAmount(800.0 + i * 100);
                cons.setCreatedAt(now.minusMonths(1).withDayOfMonth(3 + i).withHour(10));
                consumptionRepository.save(cons);
            }

            Member cardM = members.get(0);
            MembershipCard mc = new MembershipCard();
            mc.setMember(cardM);
            mc.setCardType("MONTH");
            mc.setBalance(0.0);
            mc.setRemainingTimes(0);
            mc.setStatus("ACTIVE");
            mc.setValidFrom(LocalDate.now().minusDays(5));
            mc.setValidTo(LocalDate.now().plusDays(25));
            membershipCardRepository.save(mc);

            MembershipCard mc2 = new MembershipCard();
            mc2.setMember(members.get(1));
            mc2.setCardType("COUNT");
            mc2.setBalance(0.0);
            mc2.setRemainingTimes(8);
            mc2.setStatus("ACTIVE");
            mc2.setValidFrom(LocalDate.now());
            mc2.setValidTo(LocalDate.now().plusDays(90));
            membershipCardRepository.save(mc2);

            saveMsg(systemMessageRepository, members.get(2), "EXPIRE", "会员即将到期提醒",
                "您的会籍将在近期到期，欢迎到店续费。");
            saveMsg(systemMessageRepository, members.get(4), "BOOKING_OK", "预约成功",
                "您预约的课程已通过审核。");
            saveMsg(systemMessageRepository, members.get(5), "BOOKING_CANCEL", "预约已取消",
                "您有一条预约记录已取消。");

            topUpCoursesForDemoSeeding(courseRepository, cHiit, cYoga, cBike, cPrivate, cBarbell);

            Member li = members.get(0);
            Member wang = members.get(1);
            Member zhou = members.get(4);
            Member wu = members.get(5);
            Member zheng = members.get(6);

            if (mZhang != null && cHiit != null) {
                Booking b1 = gymService.createBooking(mZhang.getId(), cHiit.getId());
                gymService.approveBooking(b1.getId());
            }
            if (cYoga != null) {
                Booking b2 = gymService.createBooking(li.getId(), cYoga.getId());
                gymService.approveBooking(b2.getId());
                Booking b3 = gymService.createBooking(wang.getId(), cYoga.getId());
                gymService.approveBooking(b3.getId());
            }
            Long bikeBookingId = null;
            if (cBike != null) {
                bikeBookingId = gymService.createBooking(zhou.getId(), cBike.getId()).getId();
            }
            if (cBarbell != null) {
                gymService.createBooking(wu.getId(), cBarbell.getId());
            }
            Long privateBookingId = null;
            if (cPrivate != null) {
                privateBookingId = gymService.createBooking(zheng.getId(), cPrivate.getId()).getId();
            }
            if (bikeBookingId != null) {
                gymService.approveBooking(bikeBookingId);
            }
            if (privateBookingId != null) {
                gymService.cancelBooking(privateBookingId);
            }
        };
    }

    private static Member saveMember(MemberRepository repo, String name, String phone, String goal, String status, String level,
                                     LocalDate expire) {
        Member m = new Member();
        m.setName(name);
        m.setPhone(phone);
        m.setGoal(goal);
        m.setStatus(status);
        m.setLevel(level);
        m.setExpireDate(expire);
        return repo.save(m);
    }

    private static Course saveCourse(CourseRepository repo, String title, Coach coach, String slot, int cap, String category) {
        Course c = new Course();
        c.setTitle(title);
        c.setCoach(coach);
        c.setSlot(slot);
        c.setCapacity(cap);
        c.setRemainingSlots(cap);
        c.setCategory(category);
        c.setEnabled(true);
        return repo.save(c);
    }

    private static Course findCourseByTitle(List<Course> list, String title) {
        return list.stream().filter(c -> title.equals(c.getTitle())).findFirst().orElse(null);
    }

    /**
     * 旧库或历史预约可能把 remaining_slots 扣到 0，而 createBooking 要求名额大于 0。
     * 演示种子执行前把相关课程余量恢复到上限，避免启动失败。
     */
    private static void topUpCoursesForDemoSeeding(CourseRepository repo, Course... refs) {
        for (Course ref : refs) {
            if (ref == null || ref.getId() == null) {
                continue;
            }
            Course c = repo.findById(ref.getId()).orElse(null);
            if (c == null) {
                continue;
            }
            int cap = c.getCapacity() != null ? c.getCapacity() : 20;
            c.setRemainingSlots(cap);
            repo.save(c);
        }
    }

    private static void saveMsg(SystemMessageRepository repo, Member member, String type, String title, String content) {
        SystemMessage m = new SystemMessage();
        m.setMember(member);
        m.setMsgType(type);
        m.setTitle(title);
        m.setContent(content);
        m.setReadFlag(false);
        m.setCreatedAt(LocalDateTime.now().minusHours(2));
        repo.save(m);
    }
}
