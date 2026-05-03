package com.twohands.authservice.infrastructure.security;

import com.twohands.authservice.application.auth.port.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordHasher implements PasswordHasher {

    // 1. Use Spring Security's BCrypt implementation
    // Sử dụng bộ mã hóa BCrypt mặc định của Spring Security
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Hashes a raw password into a secure, irreversible string.
     * Băm mật khẩu thô thành một chuỗi bảo mật, không thể đảo ngược.
     */
    @Override
    public String hash(String rawPassword) {
        // BCrypt automatically generates a random salt and includes it in the result
        // BCrypt tự động tạo salt (muối) ngẫu nhiên và nhúng nó vào kết quả băm
        return encoder.encode(rawPassword);
    }

    /**
     * Checks if a raw password matches the previously hashed password.
     * Kiểm tra xem mật khẩu thô có khớp với mật khẩu đã băm trước đó không.
     */
    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        // The salt is extracted from the encodedPassword to verify the rawPassword
        // Salt được tách ra từ chuỗi encodedPassword để kiểm tra mật khẩu thô
        return encoder.matches(rawPassword, encodedPassword);
    }
}