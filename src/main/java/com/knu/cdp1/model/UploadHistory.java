package com.knu.cdp1.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "upload_history")
public class UploadHistory {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String uamList; // "UAM1, UAM2, ..." 형식으로 저장
    private LocalDateTime uploadTime;

}
