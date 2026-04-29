package com.gym.system.controller;

import com.gym.system.dto.LoginRequest;
import com.gym.system.entity.Member;
import com.gym.system.entity.SysUser;
import com.gym.system.repository.MemberRepository;
import com.gym.system.repository.SysUserRepository;
import com.gym.system.security.JwtService;
import com.gym.system.service.WxMiniAuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final MemberRepository memberRepository;
    private final WxMiniAuthService wxMiniAuthService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(JwtService jwtService, SysUserRepository sysUserRepository,
                         MemberRepository memberRepository, WxMiniAuthService wxMiniAuthService,
                         PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.sysUserRepository = sysUserRepository;
        this.memberRepository = memberRepository;
        this.wxMiniAuthService = wxMiniAuthService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        String user = request.getUsername();
        String pass = request.getPassword();

        SysUser u = sysUserRepository.findByUsername(user).orElse(null);
        if (u == null || !verifyAndUpgradePasswordIfNeeded(u, pass)) {
            throw new RuntimeException("用户名或密码错误");
        }
        String jwtRole = "ROLE_" + u.getRole();
        Long coachId = "COACH".equals(u.getRole()) ? u.getLinkedCoachId() : null;
        Long memberId = "MEMBER".equals(u.getRole()) ? u.getLinkedMemberId() : null;
        String token = jwtService.generateToken(u.getUsername(), jwtRole, coachId, memberId);
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("username", u.getUsername());
        map.put("role", jwtRole);
        map.put("displayName", u.getDisplayName());
        map.put("coachId", coachId);
        map.put("memberId", memberId);
        return map;
    }

    /**
     * 兼容历史明文密码：若检测到是明文且校验通过，则自动升级为 BCrypt。
     */
    private boolean verifyAndUpgradePasswordIfNeeded(SysUser u, String raw) {
        String stored = u.getPassword();
        if (stored == null) return false;

        boolean looksBcrypt = stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$");
        if (looksBcrypt) {
            return passwordEncoder.matches(raw, stored);
        }
        // 旧库：明文对比
        if (!raw.equals(stored)) return false;
        u.setPassword(passwordEncoder.encode(raw));
        sysUserRepository.save(u);
        return true;
    }

    /**
     * 小程序：手机号绑定馆内会员账号后签发 JWT（可选携带 wx.login 的 code 写入 openid）。
     */
    @PostMapping("/member/bind-phone")
    public Map<String, Object> bindPhone(@RequestBody Map<String, String> body) {
        String phone = body.getOrDefault("phone", "").trim();
        String code = body.get("code");
        if (phone.isEmpty()) {
            throw new RuntimeException("请输入手机号");
        }
        Member m = memberRepository.findByPhone(phone)
            .orElseThrow(() -> new RuntimeException("该手机号未登记为会员，请联系前台"));
        String openid = null;
        if (code != null && !code.trim().isEmpty() && wxMiniAuthService.configured()) {
            openid = wxMiniAuthService.code2SessionOpenid(code.trim());
        }
        String uname = "wx_" + phone;
        SysUser u = sysUserRepository.findByUsername(uname).orElseGet(SysUser::new);
        u.setUsername(uname);
        if (u.getId() == null && (u.getPassword() == null || u.getPassword().isEmpty())) {
            // 小程序绑定账号不提供密码登录能力，仍然用加密后的随机串存储
            u.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
        }
        u.setRole("MEMBER");
        u.setDisplayName(m.getName());
        u.setLinkedMemberId(m.getId());
        if (openid != null) {
            u.setWechatOpenid(openid);
        }
        sysUserRepository.save(u);

        String token = jwtService.generateToken(u.getUsername(), "ROLE_MEMBER", null, m.getId());
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("username", u.getUsername());
        map.put("role", "ROLE_MEMBER");
        map.put("displayName", u.getDisplayName());
        map.put("memberId", m.getId());
        map.put("wxConfigured", wxMiniAuthService.configured());
        return map;
    }

    /**
     * 小程序：仅微信 code 换 openid；已绑定则直接返回 token，否则 needBind=true。
     */
    @PostMapping("/wx-login")
    public Map<String, Object> wxLogin(@RequestBody Map<String, String> body) {
        if (!wxMiniAuthService.configured()) {
            Map<String, Object> m = new HashMap<>();
            m.put("needBind", true);
            m.put("message", "未配置微信小程序密钥，请使用手机号绑定登录");
            return m;
        }
        String code = body.getOrDefault("code", "").trim();
        if (code.isEmpty()) {
            throw new RuntimeException("缺少微信登录 code");
        }
        String openid = wxMiniAuthService.code2SessionOpenid(code);
        SysUser u = sysUserRepository.findByWechatOpenid(openid).orElse(null);
        if (u == null || u.getLinkedMemberId() == null) {
            Map<String, Object> m = new HashMap<>();
            m.put("needBind", true);
            m.put("message", "首次使用请绑定健身房预留手机号");
            return m;
        }
        Long mid = u.getLinkedMemberId();
        String token = jwtService.generateToken(u.getUsername(), "ROLE_MEMBER", null, mid);
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("username", u.getUsername());
        map.put("role", "ROLE_MEMBER");
        map.put("displayName", u.getDisplayName());
        map.put("memberId", mid);
        map.put("needBind", false);
        return map;
    }
}
