package com.example.kursach.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class EmployeeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private Long shopId;
    private String login;
    private String shopAddress;
    private String role;
    private List<String> permissions;

    public EmployeeDTO() {
        this.permissions = new ArrayList<>();
    }

    public EmployeeDTO(Long id, String name, Long shopId, String login, String shopAddress, String role) {
        this.id = id;
        this.name = name;
        this.shopId = shopId;
        this.login = login;
        this.shopAddress = shopAddress;
        this.role = role;
        this.permissions = new ArrayList<>();
    }

    // Вспомогательные методы для проверки ролей
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isManager() {
        return "MANAGER".equals(role) || "ADMIN".equals(role);
    }

    public boolean hasPermission(String resource, String action) {
        if (isAdmin()) return true;
        if (permissions == null) return false;
        return permissions.contains(resource + ":" + action);
    }
}