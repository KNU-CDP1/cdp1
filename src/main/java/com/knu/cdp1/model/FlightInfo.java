package com.knu.cdp1.model;

import jakarta.persistence.*;


@Entity
@Table(name = "flight_info")
public class FlightInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private int date;

    private String flightNumber;
    private int passengers;
    private double cost;
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
    private int isDelayed;

    // 현재 진척도 (0 ~ 100)
    private double currentPosition;
    private String previousTime;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
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


    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
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

    public int isDelayed() {
        return isDelayed;
    }

    public void setDelayed(int isDelayed) {
        this.isDelayed = isDelayed;
    }

    public double getCurrentPosition(){
        return currentPosition;
    }

    public void setCurrentPosition(double currentPosition){
        this.currentPosition = currentPosition;
    }

    public String getPreviousTime() { 
        return previousTime; 
    }

    public void setPreviousTime(String previousTime) { 
        this.previousTime = previousTime; 
    }
}
