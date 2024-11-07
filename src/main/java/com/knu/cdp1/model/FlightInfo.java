package com.knu.cdp1.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "flight_info")
public class FlightInfo {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String flightNumber;
    private int passengers;
    private int seats;
    private double cost;
    private String departure;
    private String arrival;
    private String weather;
    private int risk;
    private String status;

}
