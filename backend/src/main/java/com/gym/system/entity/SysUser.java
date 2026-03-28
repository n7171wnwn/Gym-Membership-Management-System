package com.gym.system.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class SysUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    /** ADMIN, RECEPTION, COACH, MEMBER */
    private String role;
    private String displayName;
    /** 教练账号关联的教练 ID，非教练为空 */
    private Long linkedCoachId;
    /** 会员账号关联的会员 ID */
    private Long linkedMemberId;
    /** 微信小程序 openid，用于一键登录 */
    private String wechatOpenid;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public Long getLinkedCoachId() { return linkedCoachId; }
    public void setLinkedCoachId(Long linkedCoachId) { this.linkedCoachId = linkedCoachId; }
    public Long getLinkedMemberId() { return linkedMemberId; }
    public void setLinkedMemberId(Long linkedMemberId) { this.linkedMemberId = linkedMemberId; }
    public String getWechatOpenid() { return wechatOpenid; }
    public void setWechatOpenid(String wechatOpenid) { this.wechatOpenid = wechatOpenid; }
}
