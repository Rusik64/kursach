package com.example.kursach.controller;

import com.example.kursach.dto.EmployeeDTO;
import com.example.kursach.repository.model.Employee;
import com.example.kursach.security.AuditLogger;
import com.example.kursach.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    private final EmployeeService employeeService;
    private final AuditLogger auditLogger;

    public EmployeeController(EmployeeService employeeService, AuditLogger auditLogger) {
        this.employeeService = employeeService;
        this.auditLogger = auditLogger;
    }

    @GetMapping
    public String listEmployees(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO currentUser = (EmployeeDTO) session.getAttribute("employee");
        if (currentUser == null) return "redirect:/login";

        if (!currentUser.isAdmin()) return "redirect:/";

        List<Employee> employees = employeeService.getAllEmployees();

        model.addAttribute("employee", currentUser);
        model.addAttribute("employees", employees);
        model.addAttribute("currentUserId", currentUser.getId()); // ID текущего пользователя

        return "employees";
    }

    @GetMapping("/create")
    public String createEmployeeForm(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO currentUser = (EmployeeDTO) session.getAttribute("employee");
        if (currentUser == null) return "redirect:/login";

        if (!currentUser.isAdmin()) return "redirect:/";

        model.addAttribute("employee", currentUser);
        return "create-employee";
    }

    @PostMapping("/create")
    public String createEmployee(@RequestParam String name,
                                 @RequestParam Long shopId,
                                 @RequestParam String login,
                                 @RequestParam String password,
                                 @RequestParam String role,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO currentUser = (EmployeeDTO) session.getAttribute("employee");
        if (currentUser == null) return "redirect:/login";

        if (!currentUser.isAdmin()) return "redirect:/";

        try {
            employeeService.createEmployee(name, shopId, login, password, role);
            auditLogger.log(currentUser.getName() + " (" + currentUser.getLogin() + ")",
                    "CREATE", "Employee", "Создан сотрудник: " + login + ", роль: " + role);
            redirectAttributes.addFlashAttribute("success", "Сотрудник успешно добавлен");
        } catch (Exception e) {
            auditLogger.log(currentUser.getName() + " (" + currentUser.getLogin() + ")",
                    "CREATE_FAILED", "Employee", "Ошибка создания: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/employees";
    }

    @GetMapping("/edit/{id}")
    public String editEmployeeForm(@PathVariable Long id,
                                   HttpServletRequest request,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO currentUser = (EmployeeDTO) session.getAttribute("employee");
        if (currentUser == null) return "redirect:/login";

        if (!currentUser.isAdmin()) return "redirect:/";

        // Запрещаем редактировать самого себя
        if (currentUser.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("error", "Нельзя редактировать свою учетную запись");
            return "redirect:/employees";
        }

        try {
            Employee employee = employeeService.getEmployeeById(id);
            model.addAttribute("editEmployee", employee);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Сотрудник не найден");
            return "redirect:/employees";
        }

        model.addAttribute("employee", currentUser);
        return "edit-employee";
    }

    @PostMapping("/edit/{id}")
    public String editEmployee(@PathVariable Long id,
                               @RequestParam String name,
                               @RequestParam Long shopId,
                               @RequestParam String login,
                               @RequestParam(required = false) String password,
                               @RequestParam String role,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO currentUser = (EmployeeDTO) session.getAttribute("employee");
        if (currentUser == null) return "redirect:/login";

        if (!currentUser.isAdmin()) return "redirect:/";

        // Запрещаем редактировать самого себя
        if (currentUser.getId().equals(id)) {
            auditLogger.log(currentUser.getName() + " (" + currentUser.getLogin() + ")",
                    "UPDATE_DENIED", "Employee", id, "Попытка изменить свою учетную запись");
            redirectAttributes.addFlashAttribute("error", "Нельзя редактировать свою учетную запись");
            return "redirect:/employees";
        }

        try {

            employeeService.updateEmployee(id, name, shopId, login, password, role);
            auditLogger.log(currentUser.getName() + " (" + currentUser.getLogin() + ")",
                    "UPDATE", "Employee", id, "Обновлен сотрудник: " + login);
            redirectAttributes.addFlashAttribute("success", "Данные сотрудника обновлены");
        } catch (Exception e) {
            auditLogger.log(currentUser.getName() + " (" + currentUser.getLogin() + ")",
                    "UPDATE_FAILED", "Employee", id, "Ошибка: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/employees";
    }

    @PostMapping("/update-role")
    public String updateRole(@RequestParam Long employeeId,
                             @RequestParam String newRole,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO currentUser = (EmployeeDTO) session.getAttribute("employee");
        if (currentUser == null) return "redirect:/login";

        if (!currentUser.isAdmin()) return "redirect:/";

        // Запрещаем менять роль самому себе
        if (currentUser.getId().equals(employeeId)) {
            redirectAttributes.addFlashAttribute("error", "Нельзя изменить свою роль");
            return "redirect:/employees";
        }

        try {
            employeeService.updateRole(employeeId, newRole);
            redirectAttributes.addFlashAttribute("success", "Роль сотрудника изменена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/employees";
    }

    @PostMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable Long id,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/login";

        EmployeeDTO currentUser = (EmployeeDTO) session.getAttribute("employee");
        if (currentUser == null) return "redirect:/login";

        if (!currentUser.isAdmin()) return "redirect:/";

        // Запрещаем удалять самого себя
        if (currentUser.getId().equals(id)) {
            auditLogger.log(currentUser.getName() + " (" + currentUser.getLogin() + ")",
                    "DELETE_DENIED", "Employee", id, "Попытка удалить свою учетную запись");
            redirectAttributes.addFlashAttribute("error", "Нельзя удалить свою учетную запись");
            return "redirect:/employees";
        }

        try {
            Employee employee = employeeService.getEmployeeById(id);
            employeeService.deleteEmployee(id);
            auditLogger.log(currentUser.getName() + " (" + currentUser.getLogin() + ")",
                    "DELETE", "Employee", id, "Удален сотрудник: " + employee.getLogin());
            redirectAttributes.addFlashAttribute("success", "Сотрудник удален");
        } catch (Exception e) {
            auditLogger.log(currentUser.getName() + " (" + currentUser.getLogin() + ")",
                    "DELETE_FAILED", "Employee", id, "Ошибка: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/employees";
    }
}