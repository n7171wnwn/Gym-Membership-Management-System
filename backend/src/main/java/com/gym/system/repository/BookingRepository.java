package com.gym.system.repository;

import com.gym.system.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    long countByCourseId(Long courseId);
    boolean existsByMemberIdAndCourseId(Long memberId, Long courseId);
    boolean existsByMemberIdAndCourseIdAndStatusIn(Long memberId, Long courseId, Collection<String> statuses);

    @Query("select b from Booking b where " +
        "(:memberId is null or b.member.id = :memberId) and " +
        "(:courseId is null or b.course.id = :courseId) and " +
        "(:status is null or :status = '' or b.status = :status) and " +
        "(:fromTime is null or b.createdAt >= :fromTime) and " +
        "(:toTime is null or b.createdAt <= :toTime)")
    List<Booking> search(@Param("memberId") Long memberId, @Param("courseId") Long courseId,
                         @Param("status") String status, @Param("fromTime") LocalDateTime fromTime,
                         @Param("toTime") LocalDateTime toTime);
}
