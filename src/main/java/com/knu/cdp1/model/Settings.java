package com.knu.cdp1.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@Entity
@Table(name = "settings")
public class Settings {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double delayPenalty;
    private double cancelPenalty;
    private double weatherRiskWeight;

}
