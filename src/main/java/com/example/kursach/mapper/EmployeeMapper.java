package com.example.kursach.mapper;

import com.example.kursach.dto.EmployeeDTO;
import com.example.kursach.repository.model.Employee;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EmployeeMapper {

    public EmployeeDTO toDTO(Employee employee, List<String> permissions) {
        if (employee == null) {
            return null;
        }

        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(employee.getId());
        dto.setName(employee.getName());
        dto.setShopId(employee.getShopId());
        dto.setLogin(employee.getLogin());
        dto.setRole(employee.getRole() != null ? employee.getRole() : "CASHIER");
        dto.setPermissions(permissions != null ? permissions : new ArrayList<>());

        return dto;
    }
}