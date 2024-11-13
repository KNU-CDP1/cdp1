package com.knu.cdp1.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "settings")
public class Settings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int delayCost;      // 기존 delayPenalty
    private int cancelCost;     // 기존 cancelPenalty
    private int riskAlpha;      // 기존 weatherRiskWeight

    // Getter and Setter for id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Getter and Setter for delayCost
    public double getDelayCost() {
        return delayCost;
    }

    public void setDelayCost(int delayCost) {
        this.delayCost = delayCost;
    }

    // Getter and Setter for cancelCost
    public double getCancelCost() {
        return cancelCost;
    }

    public void setCancelCost(int cancelCost) {
        this.cancelCost = cancelCost;
    }

    // Getter and Setter for riskAlpha
    public double getRiskAlpha() {
        return riskAlpha;
    }

    public void setRiskAlpha(int riskAlpha) {
        this.riskAlpha = riskAlpha;
    }
}
