package com.gym.system.repository;

import com.gym.system.entity.SystemMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SystemMessageRepository extends JpaRepository<SystemMessage, Long> {
    List<SystemMessage> findAllByOrderByCreatedAtDesc();

    List<SystemMessage> findByMember_IdOrderByCreatedAtDesc(Long memberId);
}
