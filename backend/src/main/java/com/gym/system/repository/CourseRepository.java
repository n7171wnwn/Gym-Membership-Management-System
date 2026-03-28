package com.gym.system.repository;

import com.gym.system.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByCoach_Id(Long coachId);
}
