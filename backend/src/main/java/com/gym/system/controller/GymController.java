package com.gym.system.controller;

import com.alibaba.excel.EasyExcel;
import com.gym.system.dto.MemberExportRow;
import com.gym.system.entity.Booking;
import com.gym.system.entity.Coach;
import com.gym.system.entity.Consumption;
import com.gym.system.entity.Course;
import com.gym.system.entity.Member;
import com.gym.system.entity.ReminderLog;
import com.gym.system.entity.SyncLog;
import com.gym.system.service.GymService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gym")
public class GymController {
    private final GymService gymService;

    public GymController(GymService gymService) {
        this.gymService = gymService;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() { return gymService.dashboard(); }

    /** 小程序首页公告，无需登录 */
    @GetMapping("/announcements")
    public List<Map<String, Object>> announcements() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(annPublic("欢迎来到活力健身", "新用户办卡享 8 折优惠，详询前台。", "2026-03-01"));
        list.add(annPublic("春季燃脂训练营", "4 月起每周三晚团课，欢迎预约体验。", "2026-03-15"));
        list.add(annPublic("场地维护通知", "跑步机区域将例行维护，请错峰使用。", "2026-03-20"));
        return list;
    }

    private static Map<String, Object> annPublic(String title, String content, String date) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("title", title);
        m.put("content", content);
        m.put("date", date);
        return m;
    }

    @GetMapping("/members")
    public List<Member> members() { return gymService.listMembers(); }

    @GetMapping("/members/export")
    public void exportMembers(HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("member-report", StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        List<MemberExportRow> rows = new ArrayList<>();
        for (Member m : gymService.listMembers()) {
            MemberExportRow row = new MemberExportRow();
            row.setId(m.getId());
            row.setName(m.getName());
            row.setPhone(m.getPhone());
            row.setGoal(m.getGoal());
            row.setLevel(m.getLevel());
            row.setExpireDate(m.getExpireDate() == null ? "" : m.getExpireDate().toString());
            rows.add(row);
        }
        EasyExcel.write(response.getOutputStream(), MemberExportRow.class).sheet("会员表").doWrite(rows);
    }

    @PostMapping("/members")
    public Member addMember(@RequestBody Member member) { return gymService.createMember(member); }

    @PostMapping("/members/{id}/health")
    public Member updateHealth(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return gymService.updateHealth(id, body.getOrDefault("healthTracking", ""));
    }

    @GetMapping("/courses")
    public List<Course> courses() { return gymService.listCourses(); }

    @PostMapping("/courses")
    public Course addCourse(@RequestBody Map<String, Object> body) {
        Course course = new Course();
        course.setTitle(String.valueOf(body.get("title")));
        course.setSlot(String.valueOf(body.get("slot")));
        course.setCapacity(Integer.valueOf(String.valueOf(body.get("capacity"))));
        if (body.get("category") != null) {
            course.setCategory(String.valueOf(body.get("category")));
        }
        if (body.get("enabled") != null) {
            course.setEnabled(Boolean.valueOf(String.valueOf(body.get("enabled"))));
        }
        Long coachId = Long.valueOf(String.valueOf(body.get("coachId")));
        return gymService.createCourse(course, coachId);
    }

    @GetMapping("/coaches")
    public List<Coach> coaches() { return gymService.listCoaches(); }

    @PostMapping("/coaches")
    public Coach addCoach(@RequestBody Coach coach) { return gymService.createCoach(coach); }

    @PutMapping("/coaches/{id}")
    public Coach updateCoach(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String name = body.get("name") == null ? null : String.valueOf(body.get("name"));
        String specialty = body.get("specialty") == null ? null : String.valueOf(body.get("specialty"));
        return gymService.updateCoach(id, name, specialty);
    }

    @DeleteMapping("/coaches/{id}")
    public void deleteCoach(@PathVariable Long id) {
        gymService.deleteCoach(id);
    }

    @GetMapping("/bookings")
    public List<Booking> bookings() { return gymService.listBookings(); }

    @PostMapping("/bookings")
    public Booking addBooking(@RequestBody Map<String, Long> body, Authentication authentication, HttpServletRequest request) {
        Long memberId = body.get("memberId");
        Long courseId = body.get("courseId");
        if (authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MEMBER"))) {
            Object mid = request.getAttribute("memberId");
            if (!(mid instanceof Long)) {
                throw new RuntimeException("会员身份无效，请重新登录");
            }
            // 会员端预约必须绑定到自身，忽略前端传入 memberId，避免越权代约
            memberId = (Long) mid;
        }
        return gymService.createBooking(memberId, courseId);
    }

    @GetMapping("/consumptions")
    public List<Consumption> consumptions() { return gymService.listConsumptions(); }

    @PostMapping("/consumptions")
    public Consumption addConsumption(@RequestBody Map<String, Object> body) {
        Long memberId = Long.valueOf(String.valueOf(body.get("memberId")));
        String type = String.valueOf(body.get("type"));
        Double amount = Double.valueOf(String.valueOf(body.get("amount")));
        return gymService.createConsumption(memberId, type, amount);
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> map = new HashMap<>();
        map.put("status", "ok");
        return map;
    }

    @GetMapping("/sync-logs")
    public List<SyncLog> syncLogs() { return gymService.listSyncLogs(); }

    @GetMapping("/reminder-logs")
    public List<ReminderLog> reminderLogs() { return gymService.listReminderLogs(); }

    @PostMapping("/reminders/trigger")
    public Map<String, Object> triggerReminders() {
        gymService.triggerReminderNow();
        Map<String, Object> map = new HashMap<>();
        map.put("message", "提醒任务已触发");
        return map;
    }

    @GetMapping("/stats/course-sales")
    public List<Map<String, Object>> courseSales() { return gymService.courseSalesRanking(); }
}
