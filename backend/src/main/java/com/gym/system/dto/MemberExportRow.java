package com.gym.system.dto;

import com.alibaba.excel.annotation.ExcelProperty;

public class MemberExportRow {
    @ExcelProperty("会员ID")
    private Long id;
    @ExcelProperty("姓名")
    private String name;
    @ExcelProperty("手机号")
    private String phone;
    @ExcelProperty("健身目标")
    private String goal;
    @ExcelProperty("会员等级")
    private String level;
    @ExcelProperty("到期日期")
    private String expireDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getExpireDate() { return expireDate; }
    public void setExpireDate(String expireDate) { this.expireDate = expireDate; }
}
