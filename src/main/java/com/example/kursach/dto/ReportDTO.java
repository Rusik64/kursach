package com.example.kursach.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportDTO {
    private Long id;
    private Long shopId;
    private String reportType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double totalSales;
    private Integer totalChecks;
    private String createdBy;
    private LocalDateTime createdAt;
}