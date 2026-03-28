package com.gym.system.repository;

import com.gym.system.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SysUserRepository extends JpaRepository<SysUser, Long> {
    Optional<SysUser> findByUsername(String username);

    Optional<SysUser> findByWechatOpenid(String wechatOpenid);
}
