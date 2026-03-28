package com.gym.system.controller;

import com.gym.system.dto.LoginRequest;
import com.gym.system.entity.SysUser;
import com.gym.system.repository.SysUserRepository;
import com.gym.system.security.JwtService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/gym/auth")
public class AuthController {
    private final JwtService jwtService;
    private final SysUserRepository sysUserRepository;

    public AuthController(JwtService jwtService, SysUserRepository sysUserRepository) {
        this.jwtService = jwtService;
        this.sysUserRepository = sysUserRepository;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        String user = request.getUsername();
        String pass = request.getPassword();

        SysUser u = sysUserRepository.findByUsername(user).orElse(null);
        if (u == null || !pass.equals(u.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        String jwtRole = "ROLE_" + u.getRole();
        Long coachId = "COACH".equals(u.getRole()) ? u.getLinkedCoachId() : null;
        String token = jwtService.generateToken(u.getUsername(), jwtRole, coachId);
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("username", u.getUsername());
        map.put("role", jwtRole);
        map.put("displayName", u.getDisplayName());
        map.put("coachId", coachId);
        return map;
    }
}
