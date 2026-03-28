package com.gym.system.repository;

import com.gym.system.entity.MembershipCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MembershipCardRepository extends JpaRepository<MembershipCard, Long> {
    List<MembershipCard> findByMemberIdOrderByIdDesc(Long memberId);
}
