package com.gym.system.config;

import com.gym.system.entity.Coach;
import com.gym.system.entity.Course;
import com.gym.system.entity.Member;
import com.gym.system.entity.SysUser;
import com.gym.system.repository.CoachRepository;
import com.gym.system.repository.CourseRepository;
import com.gym.system.repository.MemberRepository;
import com.gym.system.repository.SysUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner init(
        MemberRepository memberRepository,
        CourseRepository courseRepository,
        CoachRepository coachRepository,
        SysUserRepository sysUserRepository
    ) {
        return args -> {
            Coach coach;
            if (coachRepository.count() == 0) {
                Coach c = new Coach();
                c.setName("王教练");
                c.setSpecialty("减脂塑形");
                coach = coachRepository.save(c);
            } else {
                coach = coachRepository.findAll().get(0);
            }

            if (sysUserRepository.count() == 0) {
                seedUser(sysUserRepository, "admin", "admin123", "ADMIN", "系统管理员", null);
                seedUser(sysUserRepository, "reception", "reception123", "RECEPTION", "前台", null);
                seedUser(sysUserRepository, "coach", "coach123", "COACH", "教练账号", coach.getId());
                seedUser(sysUserRepository, "root", "hjj060618", "ADMIN", "超级管理员", null);
                seedUser(sysUserRepository, "member", "member123", "MEMBER", "测试会员", null);
            }

            if (memberRepository.count() == 0) {
                Member m1 = new Member();
                m1.setName("张三");
                m1.setPhone("13800000001");
                m1.setGoal("减脂");
                m1.setLevel("Bronze");
                m1.setStatus("NORMAL");
                m1.setExpireDate(LocalDate.now().plusDays(5));
                memberRepository.save(m1);
            }

            if (courseRepository.count() == 0) {
                Course c1 = new Course();
                c1.setTitle("HIIT 燃脂");
                c1.setCoach(coach);
                c1.setSlot("周一 19:00");
                c1.setCapacity(10);
                c1.setRemainingSlots(10);
                c1.setCategory("AEROBIC");
                c1.setEnabled(true);
                courseRepository.save(c1);
            }
        };
    }

    private static void seedUser(SysUserRepository repo, String u, String p, String role, String name, Long coachId) {
        SysUser s = new SysUser();
        s.setUsername(u);
        s.setPassword(p);
        s.setRole(role);
        s.setDisplayName(name);
        s.setLinkedCoachId(coachId);
        repo.save(s);
    }
}
