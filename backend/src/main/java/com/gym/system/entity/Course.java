package com.gym.system.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

@Entity
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String slot;
    private Integer capacity;
    private Integer remainingSlots;
    @Version
    private Long version;
    @ManyToOne
    private Coach coach;
    /** YOGA, AEROBIC, STRENGTH, PRIVATE */
    private String category;
    private Boolean enabled;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSlot() { return slot; }
    public void setSlot(String slot) { this.slot = slot; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public Integer getRemainingSlots() { return remainingSlots; }
    public void setRemainingSlots(Integer remainingSlots) { this.remainingSlots = remainingSlots; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public Coach getCoach() { return coach; }
    public void setCoach(Coach coach) { this.coach = coach; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
