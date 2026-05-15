package com.gym.system.controller;

import com.gym.system.entity.Booking;
import com.gym.system.entity.Coach;
import com.gym.system.entity.Course;
import com.gym.system.entity.Member;
import com.gym.system.entity.MembershipCard;
import com.gym.system.entity.SystemMessage;
import com.gym.system.service.GymService;
import com.gym.system.service.ManageService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gym/manage")
public class ManageController {
    private final ManageService manageService;
    private final GymService gymService;

    public ManageController(ManageService manageService, GymService gymService) {
        this.manageService = manageService;
        this.gymService = gymService;
    }

    private Long coachIdOrNull(Authentication auth, HttpServletRequest req) {
        if (auth != null && auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_COACH"))) {
            Object a = req.getAttribute("coachId");
            if (a instanceof Long) return (Long) a;
        }
        return null;
    }

    @GetMapping("/members")
    public List<Member> members(@RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String status) {
        return manageService.searchMembers(keyword, status);
    }

    @GetMapping("/members/{id}")
    public Member member(@PathVariable Long id) {
        return manageService.getMember(id);
    }

    @PostMapping("/members")
    public Member createMember(@RequestBody Member member) {
        return manageService.saveMember(member);
    }

    @PutMapping("/members/{id}")
    public Member updateMember(@PathVariable Long id, @RequestBody Member member) {
        member.setId(id);
        return manageService.saveMember(member);
    }

    @DeleteMapping("/members/{id}")
    public void deleteMember(@PathVariable Long id) {
        manageService.deleteMember(id);
    }

    @PostMapping("/members/{id}/freeze")
    public Member freezeMember(@PathVariable Long id) {
        return manageService.freezeMember(id);
    }

    @PostMapping("/members/{id}/unfreeze")
    public Member unfreezeMember(@PathVariable Long id) {
        return manageService.unfreezeMember(id);
    }

    @GetMapping("/members/{id}/cards")
    public List<MembershipCard> cards(@PathVariable Long id) {
        return manageService.listCardsByMember(id);
    }

    @PostMapping("/cards/issue")
    public MembershipCard issue(@RequestBody Map<String, Object> body) {
        Long memberId = Long.valueOf(String.valueOf(body.get("memberId")));
        String cardType = String.valueOf(body.get("cardType"));
        Double pay = body.get("payAmount") == null ? 0 : Double.valueOf(String.valueOf(body.get("payAmount")));
        Integer times = body.get("remainingTimes") == null ? null : Integer.valueOf(String.valueOf(body.get("remainingTimes")));
        int days = body.get("validDays") == null ? 30 : Integer.parseInt(String.valueOf(body.get("validDays")));
        return manageService.issueCard(memberId, cardType, pay, times, days);
    }

    @PostMapping("/cards/{id}/renew")
    public MembershipCard renew(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Double pay = body.get("payAmount") == null ? 0 : Double.valueOf(String.valueOf(body.get("payAmount")));
        int days = body.get("extendDays") == null ? 30 : Integer.parseInt(String.valueOf(body.get("extendDays")));
        return manageService.renewCard(id, pay, days);
    }

    @PostMapping("/cards/{id}/lost")
    public MembershipCard lost(@PathVariable Long id) {
        return manageService.reportLost(id);
    }

    @PostMapping("/cards/{id}/cancel")
    public MembershipCard cancelCard(@PathVariable Long id) {
        return manageService.cancelCard(id);
    }

    @PutMapping("/courses/{id}")
    public Course updateCourse(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String title = body.get("title") == null ? null : String.valueOf(body.get("title"));
        String slot = body.get("slot") == null ? null : String.valueOf(body.get("slot"));
        Integer cap = body.get("capacity") == null ? null : Integer.valueOf(String.valueOf(body.get("capacity")));
        String cat = body.get("category") == null ? null : String.valueOf(body.get("category"));
        Long coachId = body.get("coachId") == null ? null : Long.valueOf(String.valueOf(body.get("coachId")));
        Boolean enabled = body.get("enabled") == null ? null : Boolean.valueOf(String.valueOf(body.get("enabled")));
        return manageService.updateCourse(id, title, slot, cap, cat, coachId, enabled);
    }

    @GetMapping("/bookings")
    public List<Booking> bookings(
        Authentication authentication,
        HttpServletRequest request,
        @RequestParam(required = false) Long memberId,
        @RequestParam(required = false) Long courseId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromTime,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toTime
    ) {
        Long cid = coachIdOrNull(authentication, request);
        return manageService.searchBookings(memberId, courseId, status, fromTime, toTime, cid);
    }

    @PostMapping("/bookings/{id}/approve")
    public Booking approve(@PathVariable Long id) {
        return gymService.approveBooking(id);
    }

    @PostMapping("/bookings/{id}/cancel")
    public Booking cancelBooking(@PathVariable Long id) {
        return gymService.cancelBooking(id);
    }

    @GetMapping("/messages")
    public List<SystemMessage> messages() {
        return manageService.listMessages();
    }

    @GetMapping("/stats/charts")
    public Map<String, Object> charts() {
        return manageService.chartData();
    }

    @GetMapping("/stats/finance")
    public Map<String, Object> finance() {
        return manageService.financeSummary();
    }

    @PostMapping("/profile/password")
    public Map<String, String> password(Authentication authentication, @RequestBody Map<String, String> body) {
        manageService.changePassword(authentication.getName(),
            body.getOrDefault("oldPassword", ""),
            body.getOrDefault("newPassword", ""));
        return java.util.Collections.singletonMap("message", "密码已更新");
    }

    @GetMapping("/coach/courses")
    public List<Course> coachCourses(Authentication authentication, HttpServletRequest request) {
        Long cid = coachIdOrNull(authentication, request);
        if (cid == null) {
            throw new RuntimeException("非教练账号");
        }
        return manageService.coursesForCoach(cid);
    }
}
