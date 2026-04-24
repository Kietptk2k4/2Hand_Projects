package com.twohands.authservice.application.auth.verify;

import com.twohands.authservice.application.auth.port.AttemptStore;
import com.twohands.authservice.application.auth.port.OtpStore;
import com.twohands.authservice.delivery.http.exception.BadRequestException;
import com.twohands.authservice.delivery.http.exception.TooManyRequestsException;
import com.twohands.authservice.domain.user.User;
import com.twohands.authservice.domain.user.UserRepository;
import com.twohands.authservice.domain.user.UserStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class VerifyEmailUseCase {

    private final UserRepository userRepository;
    private final OtpStore otpStore;
    private final AttemptStore attemptStore;

    @Value("${auth.otp.max-attempts}")
    private int maxAttempts;

    @Value("${auth.otp.ttl-seconds}")
    private long otpTtl;

    public VerifyEmailUseCase(UserRepository userRepository,
                              OtpStore otpStore,
                              AttemptStore attemptStore) {
        this.userRepository = userRepository;
        this.otpStore = otpStore;
        this.attemptStore = attemptStore;
    }

    @Transactional
    public void execute(String email, String otp) {
        // 1. Normalize the email to ensure consistency with the database
        // Chuẩn hóa email để đảm bảo tính nhất quán với dữ liệu trong database
        String emailNorm = email.trim().toLowerCase(Locale.ROOT);

        // 2. Retrieve user and verify if the account is already activated
        // Truy xuất người dùng và kiểm tra xem tài khoản đã được xác thực trước đó chưa
        User user = userRepository.findByEmailNormalized(emailNorm)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new BadRequestException("Email is already verified");
        }

        // 3. Check for maximum failed attempts to prevent brute-force attacks
        // Kiểm tra số lần thử sai tối đa để ngăn chặn các cuộc tấn công dò mã
        String failKey = "auth:otp:fail:" + emailNorm;
        long currentAttempts = attemptStore.getCount(failKey);

        if (currentAttempts >= maxAttempts) {
            throw new TooManyRequestsException("Maximum OTP attempts exceeded. Please request a new OTP.");
        }

        // 4. Retrieve the stored OTP from Redis and check for expiration
        // Lấy mã OTP đã lưu từ Redis và kiểm tra xem nó còn hạn hay không
        String otpKey = "auth:otp:register:" + emailNorm;
        String storedOtp = otpStore.get(otpKey);

        if (storedOtp == null) {
            throw new BadRequestException("OTP has expired or does not exist. Please request a new OTP.");
        }

        // 5. Validate the provided OTP against the stored one
        // So khớp mã OTP người dùng gửi lên với mã đã lưu trong hệ thống
        if (!storedOtp.equals(otp)) {
            // Increment the fail counter if the OTP is incorrect
            // Tăng bộ đếm số lần thất bại nếu mã OTP không chính xác
            long newAttempts = attemptStore.increment(failKey, otpTtl);
            
            if (newAttempts >= maxAttempts) {
                throw new TooManyRequestsException("Maximum OTP attempts exceeded. Please request a new OTP.");
            }
            throw new BadRequestException("Invalid OTP. " + (maxAttempts - newAttempts) + " attempt(s) remaining.");
        }

        // 6. OTP is correct: Activate the user and update status in DB
        // OTP chính xác: Kích hoạt người dùng và cập nhật trạng thái vào Database
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);
        userRepository.save(user);

        // 7. Clean up Redis data after successful verification
        // Dọn dẹp dữ liệu trong Redis (xóa OTP và bộ đếm lỗi) sau khi xác thực thành công
        otpStore.delete(otpKey);
        attemptStore.delete(failKey);
    }
}
