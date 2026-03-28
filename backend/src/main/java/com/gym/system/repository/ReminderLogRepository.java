package com.gym.system.repository;

import com.gym.system.entity.ReminderLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReminderLogRepository extends JpaRepository<ReminderLog, Long> {
}
