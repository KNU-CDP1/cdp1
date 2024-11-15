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

    private String title;

    // 기본 생성자
    public UploadHistory() {
    }

    // 생성자
    public UploadHistory(String fileName, String flightNames, LocalDateTime uploadDate, String title, String details , String author) {
        this.fileName = fileName;
        this.flightNames = flightNames;
        this.uploadDate = uploadDate;
        this.details = details;
        this.author = author;
        this.title = title;
    }


}
