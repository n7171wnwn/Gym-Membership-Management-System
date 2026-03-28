package com.gym.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.system.entity.Booking;
import com.gym.system.entity.Consumption;
import com.gym.system.entity.Member;
import com.gym.system.entity.MembershipCard;
import com.gym.system.entity.SystemMessage;
import com.gym.system.repository.BookingRepository;
import com.gym.system.repository.ConsumptionRepository;
import com.gym.system.repository.MemberRepository;
import com.gym.system.repository.MembershipCardRepository;
import com.gym.system.repository.SystemMessageRepository;
import com.gym.system.service.GymService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gym/member")
public class MemberPortalController {
    private final MemberRepository memberRepository;
    private final BookingRepository bookingRepository;
    private final MembershipCardRepository membershipCardRepository;
    private final ConsumptionRepository consumptionRepository;
    private final SystemMessageRepository systemMessageRepository;
    private final GymService gymService;
    private final ObjectMapper objectMapper;

    public MemberPortalController(MemberRepository memberRepository, BookingRepository bookingRepository,
                                  MembershipCardRepository membershipCardRepository,
                                  ConsumptionRepository consumptionRepository,
                                  SystemMessageRepository systemMessageRepository, GymService gymService,
                                  ObjectMapper objectMapper) {
        this.memberRepository = memberRepository;
        this.bookingRepository = bookingRepository;
        this.membershipCardRepository = membershipCardRepository;
        this.consumptionRepository = consumptionRepository;
        this.systemMessageRepository = systemMessageRepository;
        this.gymService = gymService;
        this.objectMapper = objectMapper;
    }

    private Long requireMemberId(HttpServletRequest request) {
        Object a = request.getAttribute("memberId");
        if (!(a instanceof Long)) {
            throw new RuntimeException("会员身份无效，请重新登录");
        }
        return (Long) a;
    }

    @GetMapping("/profile")
    public Member profile(HttpServletRequest request) {
        Long mid = requireMemberId(request);
        return memberRepository.findById(mid).orElseThrow(() -> new RuntimeException("会员不存在"));
    }

    @GetMapping("/bookings")
    public List<Booking> myBookings(HttpServletRequest request) {
        Long mid = requireMemberId(request);
        return bookingRepository.search(mid, null, null, null, null);
    }

    @PostMapping("/bookings/{id}/cancel")
    public Booking cancelMyBooking(@PathVariable Long id, HttpServletRequest request) {
        Long mid = requireMemberId(request);
        Booking b = bookingRepository.findById(id).orElseThrow(() -> new RuntimeException("预约不存在"));
        if (b.getMember() == null || !mid.equals(b.getMember().getId())) {
            throw new RuntimeException("无权取消该预约");
        }
        return gymService.cancelBooking(id);
    }

    @GetMapping("/cards")
    public List<MembershipCard> myCards(HttpServletRequest request) {
        Long mid = requireMemberId(request);
        return membershipCardRepository.findByMemberIdOrderByIdDesc(mid);
    }

    @GetMapping("/consumptions")
    public List<Consumption> myConsumptions(HttpServletRequest request) {
        Long mid = requireMemberId(request);
        return consumptionRepository.findByMember_IdOrderByCreatedAtDesc(mid);
    }

    @GetMapping("/messages")
    public List<SystemMessage> myMessages(HttpServletRequest request) {
        Long mid = requireMemberId(request);
        return systemMessageRepository.findByMember_IdOrderByCreatedAtDesc(mid);
    }

    @PutMapping("/health")
    public Member updateHealth(@RequestBody Map<String, Object> body, HttpServletRequest request) throws Exception {
        Long mid = requireMemberId(request);
        Member m = memberRepository.findById(mid).orElseThrow(() -> new RuntimeException("会员不存在"));
        Map<String, Object> track = new LinkedHashMap<>();
        if (m.getHealthTracking() != null && !m.getHealthTracking().isEmpty()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> prev = objectMapper.readValue(m.getHealthTracking(), Map.class);
                track.putAll(prev);
            } catch (Exception ignored) {
                track.put("legacyNote", m.getHealthTracking());
            }
        }
        if (body.get("height") != null) {
            track.put("height", body.get("height"));
        }
        if (body.get("weight") != null) {
            track.put("weight", body.get("weight"));
        }
        if (body.get("bodyFat") != null) {
            track.put("bodyFat", body.get("bodyFat"));
        }
        if (body.get("note") != null) {
            track.put("note", body.get("note"));
        }
        track.put("updatedAt", java.time.LocalDate.now().toString());
        String json = objectMapper.writeValueAsString(track);
        return gymService.updateHealth(mid, json);
    }
}
