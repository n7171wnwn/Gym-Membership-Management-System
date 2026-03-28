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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
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

    @GetMapping("/bookings")
    public List<Booking> bookings() { return gymService.listBookings(); }

    @PostMapping("/bookings")
    public Booking addBooking(@RequestBody Map<String, Long> body) {
        Long memberId = body.get("memberId");
        Long courseId = body.get("courseId");
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
