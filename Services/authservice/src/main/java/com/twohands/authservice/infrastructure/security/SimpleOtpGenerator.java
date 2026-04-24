package com.twohands.authservice.infrastructure.security;

import com.twohands.authservice.application.auth.port.OtpGenerator;
import org.springframework.stereotype.Component;

@Component
public class SimpleOtpGenerator implements OtpGenerator {

    /**
     * Generates a random 6-digit One-Time Password (OTP).
     * Tạo mã mật khẩu dùng một lần (OTP) ngẫu nhiên có 6 chữ số.
     */
    @Override
    public String generate() {
        // Calculate a random integer between 100,000 and 999,999
        // Tính toán một số nguyên ngẫu nhiên nằm trong khoảng từ 100,000 đến 999,999
        //Math.random(): Returns a double value greater than or equal to 0.0 and less than 1.0.
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }
}
