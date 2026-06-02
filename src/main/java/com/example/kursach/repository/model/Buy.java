package com.example.kursach.repository.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Buy {
    private Long id;
    private double sum;
    private Long shopId;
    private LocalDateTime createdAt;
}
