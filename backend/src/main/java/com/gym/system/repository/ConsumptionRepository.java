package com.gym.system.repository;

import com.gym.system.entity.Consumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ConsumptionRepository extends JpaRepository<Consumption, Long> {
    @Query("select coalesce(sum(c.amount),0) from Consumption c where c.member.id = :memberId")
    Double sumAmountByMemberId(Long memberId);

    @Query("select coalesce(sum(c.amount),0) from Consumption c")
    Double sumAllAmount();

    @Query(value = "select date_format(c.created_at, '%Y-%m') as ym, coalesce(sum(c.amount),0) " +
        "from consumption c group by ym order by ym", nativeQuery = true)
    List<Object[]> sumAmountGroupByMonth();
}
