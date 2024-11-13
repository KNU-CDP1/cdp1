package com.knu.cdp1.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class UploadHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private String flightNames;

    private LocalDateTime uploadDate;

    @Column(columnDefinition = "TEXT")
    private String details;

    // 기본 생성자
    public UploadHistory() {
    }

    // 생성자
    public UploadHistory(String fileName, String flightNames, LocalDateTime uploadDate, String details) {
        this.fileName = fileName;
        this.flightNames = flightNames;
        this.uploadDate = uploadDate;
        this.details = details;
    }

    // Getter와 Setter
    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFlightNames() {
        return flightNames;
    }

    public void setFlightNames(String flightNames) {
        this.flightNames = flightNames;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
