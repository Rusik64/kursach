package com.example.kursach.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscountDTO {
    private Long id;
    private Long productId;
    private String productName;
    private Long categoryId;
    private String categoryName;
    private Double percentage;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;
}