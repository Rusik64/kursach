package com.example.kursach.repository.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee implements Serializable {
    private Long id;
    private String name;
    private String login;
    private String password;
    private Long shopId;
    private String role;
    private List<String> permissions;
}
