package com.knu.cdp1.repository;

import com.knu.cdp1.model.UploadHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadHistoryRepository extends JpaRepository<UploadHistory, Long> {
}
