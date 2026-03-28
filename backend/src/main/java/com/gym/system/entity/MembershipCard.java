package com.gym.system.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.LocalDate;

@Entity
public class MembershipCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Member member;
    /** MONTH, SEASON, YEAR, COUNT */
    private String cardType;
    /** 储值余额（元） */
    private Double balance;
    /** 次卡剩余次数 */
    private Integer remainingTimes;
    /** ACTIVE, LOST, CANCELLED */
    private String status;
    private LocalDate validFrom;
    private LocalDate validTo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }
    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }
    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }
    public Integer getRemainingTimes() { return remainingTimes; }
    public void setRemainingTimes(Integer remainingTimes) { this.remainingTimes = remainingTimes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }
    public LocalDate getValidTo() { return validTo; }
    public void setValidTo(LocalDate validTo) { this.validTo = validTo; }
}
