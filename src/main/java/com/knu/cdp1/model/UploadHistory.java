package com.knu.cdp1.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
public class UploadHistory {

    // Getter와 Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private String flightNames;

    private LocalDateTime uploadDate;

    @Column(columnDefinition = "TEXT")
    private String details;

    private String author;

    // 기본 생성자
    public UploadHistory() {
    }

    // 생성자
    public UploadHistory(String fileName, String flightNames, LocalDateTime uploadDate, String details , String author) {
        this.fileName = fileName;
        this.flightNames = flightNames;
        this.uploadDate = uploadDate;
        this.details = details;
        this.author = author;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFlightNames(String flightNames) {
        this.flightNames = flightNames;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
