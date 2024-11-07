package com.knu.cdp1.model;

import jakarta.persistence.*;


@Entity
@Table(name = "flight_info")
public class FlightInfo {

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
    private int plannedStart;
    private int plannedEnd;
    private boolean inFlight;
    private double seatCost;

    // 추가 필드
    private int delayTime;
    private boolean cancelled;
    private int adjustedStart;
    private int adjustedEnd;
    private double risk;

    // 날씨 관련 필드
    private double windSpeed;
    private double rainfall;
    private double visibility;

    // 지연 여부 필드
    private boolean isDelayed;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public int getPassengers() {
        return passengers;
    }

    public void setPassengers(int passengers) {
        this.passengers = passengers;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public String getArrival() {
        return arrival;
    }

    public void setArrival(String arrival) {
        this.arrival = arrival;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public int getPlannedStart() {
        return plannedStart;
    }

    public void setPlannedStart(int plannedStart) {
        this.plannedStart = plannedStart;
    }

    public int getPlannedEnd() {
        return plannedEnd;
    }

    public void setPlannedEnd(int plannedEnd) {
        this.plannedEnd = plannedEnd;
    }

    public boolean isInFlight() {
        return inFlight;
    }

    public void setInFlight(boolean inFlight) {
        this.inFlight = inFlight;
    }

    public double getSeatCost() {
        return seatCost;
    }

    public void setSeatCost(double seatCost) {
        this.seatCost = seatCost;
    }

    public int getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public int getAdjustedStart() {
        return adjustedStart;
    }

    public void setAdjustedStart(int adjustedStart) {
        this.adjustedStart = adjustedStart;
    }

    public int getAdjustedEnd() {
        return adjustedEnd;
    }

    public void setAdjustedEnd(int adjustedEnd) {
        this.adjustedEnd = adjustedEnd;
    }

    public double getRisk() {
        return risk;
    }

    public void setRisk(double risk) {
        this.risk = risk;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public double getRainfall() {
        return rainfall;
    }

    public void setRainfall(double rainfall) {
        this.rainfall = rainfall;
    }

    public double getVisibility() {
        return visibility;
    }

    public void setVisibility(double visibility) {
        this.visibility = visibility;
    }

    public boolean isDelayed() {
        return isDelayed;
    }

    public void setDelayed(boolean isDelayed) {
        this.isDelayed = isDelayed;
    }
}
