package com.example.kursach.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuditLogger {

    // Имя логгера должно совпадать с именем в logback-spring.xml
    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    public void log(String username, String action, String entity, Long entityId, String details) {
        String message = String.format("%-30s | %-15s | %-15s | %-5s | %s",
                username != null ? username : "SYSTEM",
                action,
                entity,
                entityId != null ? entityId.toString() : "-",
                details != null ? details : ""
        );

        auditLog.info(message);
    }

    public void log(String username, String action, String entity, String details) {
        log(username, action, entity, null, details);
    }

    public void log(String username, String action, String details) {
        log(username, action, "-", null, details);
    }
}