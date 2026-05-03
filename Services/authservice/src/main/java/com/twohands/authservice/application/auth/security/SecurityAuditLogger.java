package com.twohands.authservice.application.auth.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SecurityAuditLogger {

    private static final Logger log = LoggerFactory.getLogger(SecurityAuditLogger.class);

    public void info(String event, UUID userId, String details) {
        log.info("security_event={} user_id={} details={}", event, userId, details);
    }

    // /**
    //  * Ghi log cảnh báo (warning) về một sự kiện bảo mật trong hệ thống.
    //  * * Hàm này sử dụng cấu trúc log có định dạng (structured logging) để dễ dàng 
    //  * tìm kiếm và phân tích trên các hệ thống quản lý log (như ELK, Splunk, v.v.).
    //  *
    //  * @param event   Tên hoặc loại sự kiện bảo mật (ví dụ: "FAILED_LOGIN", "UNAUTHORIZED_ACCESS").
    //  * @param userId  Mã định danh (UUID) của người dùng liên quan đến sự kiện này.
    //  * @param details Các thông tin chi tiết bổ sung liên quan đến sự kiện bảo mật.
    //  */
    public void warn(String event, UUID userId, String details) {
        // Ghi log ở mức WARN với các tham số được truyền vào pattern để tránh lặp chuỗi
        log.warn("security_event={} user_id={} details={}", event, userId, details);
    }
}
