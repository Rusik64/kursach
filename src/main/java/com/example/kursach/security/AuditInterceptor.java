package com.example.kursach.security;

import com.example.kursach.dto.EmployeeDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuditInterceptor implements HandlerInterceptor {

    private final AuditLogger auditLogger;

    public AuditInterceptor(AuditLogger auditLogger) {
        this.auditLogger = auditLogger;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {

        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();

        // Пропускаем статические ресурсы
        if (uri.startsWith("/css/") || uri.startsWith("/js/") ||
                uri.startsWith("/images/") || uri.equals("/favicon.ico")) {
            return;
        }

        // Получаем пользователя
        EmployeeDTO employee = (EmployeeDTO) request.getSession().getAttribute("employee");
        String username = employee != null ? employee.getName() + " (" + employee.getLogin() + ")" : "anonymous";

        // Логируем только изменяющие операции
        if (isModifyingMethod(method)) {
            auditLogger.log(username, method, uri, "Status: " + status);
        }

        // Логируем ошибки доступа
        if (status == 403) {
            auditLogger.log(username, "ACCESS_DENIED", uri, "HTTP 403 Forbidden");
        }
    }

    private boolean isModifyingMethod(String method) {
        return "POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method);
    }
}