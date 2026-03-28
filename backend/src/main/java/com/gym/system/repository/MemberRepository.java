package com.gym.system.repository;

import com.gym.system.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByPhone(String phone);

    @Query("select m from Member m where " +
        "(:kw is null or :kw = '' or m.name like concat('%', :kw, '%') or m.phone like concat('%', :kw, '%')) and " +
        "(:status is null or :status = '' or m.status = :status)")
    List<Member> search(@Param("kw") String keyword, @Param("status") String status);
}
