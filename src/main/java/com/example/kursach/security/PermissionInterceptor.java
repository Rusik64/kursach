package com.example.kursach.security;

import com.example.kursach.dto.EmployeeDTO;
import com.example.kursach.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private final EmployeeService employeeService;

    public PermissionInterceptor(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequirePermission requirePermission = handlerMethod.getMethodAnnotation(RequirePermission.class);

        // Если аннотация отсутствует, разрешаем доступ
        if (requirePermission == null) {
            return true;
        }

        HttpSession session = request.getSession();
        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");

        if (employee == null) {
            response.sendRedirect("/login");
            return false;
        }

        // Проверяем права
        if (!employeeService.hasPermission(employee, requirePermission.resource(), requirePermission.action())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return false;
        }

        return true;
    }
}