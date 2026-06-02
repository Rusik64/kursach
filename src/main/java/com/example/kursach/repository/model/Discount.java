package com.example.kursach.repository.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Discount {
    private Long id;
    private Long categoryId;
    private Long productId;
    private Double percentage;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String productName;
    private String categoryName;

    public boolean isActive() {
        if (startDate == null || endDate == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return (now.isEqual(startDate) || now.isAfter(startDate)) &&
                (now.isEqual(endDate) || now.isBefore(endDate));
    }

    public boolean isActiveAt(LocalDateTime dateTime) {
        if (startDate == null || endDate == null || dateTime == null) {
            return false;
        }
        return (dateTime.isEqual(startDate) || dateTime.isAfter(startDate)) &&
                (dateTime.isEqual(endDate) || dateTime.isBefore(endDate));
    }
}
