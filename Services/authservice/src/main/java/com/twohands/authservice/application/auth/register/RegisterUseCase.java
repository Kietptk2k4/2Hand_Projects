package com.twohands.authservice.application.auth.register;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.authservice.application.auth.event.OutboxRecord;
import com.twohands.authservice.application.auth.port.OtpGenerator;
import com.twohands.authservice.application.auth.port.OtpStore;
import com.twohands.authservice.application.auth.port.OutboxRepository;
import com.twohands.authservice.application.auth.port.PasswordHasher;
import com.twohands.authservice.delivery.http.exception.BadRequestException;
import com.twohands.authservice.domain.role.Role;
import com.twohands.authservice.domain.role.RoleRepository;
import com.twohands.authservice.domain.user.User;
import com.twohands.authservice.domain.user.UserRepository;
import com.twohands.authservice.domain.user.UserStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class RegisterUseCase {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordHasher passwordHasher;
    private final OtpGenerator otpGenerator;
    private final OtpStore otpStore;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public RegisterUseCase(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordHasher passwordHasher,
            OtpGenerator otpGenerator,
            OtpStore otpStore,
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordHasher = passwordHasher;
        this.otpGenerator = otpGenerator;
        this.otpStore = otpStore;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Executes within a single transaction.
     * DB writes (user + role + outbox event) are atomic — all succeed or all roll back.
     * OTP is stored in Redis after the DB writes but before commit.
     * If Redis fails, the transaction rolls back, keeping DB consistent.
     * Kafka publishing is decoupled via the outbox table and handled asynchronously.
     */
    /**
     * Thực hiện quy trình đăng ký người dùng mới trong một Transaction duy nhất.
     * Đảm bảo tính nguyên tử (Atomic): Lưu DB, ghi Outbox và lưu Redis phải cùng thành công hoặc cùng thất bại.
     */
    @Transactional
    public RegisterResult execute(RegisterCommand cmd) {
        // 1. Kiểm tra tính hợp lệ sơ bộ của dữ liệu đầu vào (Format email, password...)
        validate(cmd);

        // 2. Chuẩn hóa email (viết thường, xóa khoảng trắng) để tránh trùng lặp tài khoản do định dạng
        String emailNorm = cmd.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailNormalized(emailNorm)) {
            throw new BadRequestException("Email already registered");
        }

        // 3. Mã hóa mật khẩu trước khi lưu để đảm bảo an toàn thông tin
        String hash = passwordHasher.hash(cmd.password());

        // 4. Khởi tạo đối tượng User mới với trạng thái chờ xác thực (PENDING_VERIFICATION)
        User user = new User();
        user.setEmail(cmd.email());
        user.setEmailNormalized(emailNorm);
        user.setPasswordHash(hash);
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);

        // 5. Gán quyền mặc định (Role USER) cho người dùng mới
        Role role = roleRepository.findByCode("USER")
                .orElseThrow(() -> new IllegalStateException("Role USER not found in database"));
        user.getRoles().add(role);

        // 6. Lưu User vào Database. Transaction vẫn đang mở, chưa commit xuống DB vật lý.
        user = userRepository.save(user);

        // 7. Xử lý mã OTP để xác thực đăng ký:
        // - Tạo mã OTP ngẫu nhiên
        // - Lưu vào Redis với thời gian hết hạn (TTL) là 300 giây (5 phút)
        // Lưu ý: Nếu Redis lỗi, Transaction sẽ rollback, User vừa save ở trên cũng sẽ bị xóa.
        String otp = otpGenerator.generate();
        String otpKey = "auth:otp:register:" + emailNorm;
        otpStore.save(otpKey, otp, 300);

        // 8. Áp dụng Outbox Pattern:
        // Lưu một bản ghi "Sự kiện" vào bảng Outbox trong cùng Transaction với User.
        // Một Worker khác sẽ quét bảng này để gửi email qua Kafka/SMTP sau, tránh làm chậm luồng đăng ký.
        OutboxRecord outboxRecord = buildOutboxRecord(user.getEmail(), otp);
        outboxRepository.save(outboxRecord);

        // 9. Trả về kết quả sau khi mọi bước chuẩn bị đã hoàn tất thành công
        return new RegisterResult(user.getId(), user.getStatus().name());
    }

    // /**
    //  * Đóng gói thông tin người dùng (email, OTP) thành một bản ghi OutboxRecord.
    //  * Áp dụng Transactional Outbox pattern để đảm bảo tính nhất quán khi gửi event.
    //  *
    //  * @param email Địa chỉ email của người dùng
    //  * @param otp   Mã xác thực
    //  * @return Bản ghi OutboxRecord chứa định danh UUID, loại sự kiện và payload JSON
    //  * @throws IllegalStateException nếu quá trình chuyển đổi dữ liệu sang JSON thất bại
    //  */
    private OutboxRecord buildOutboxRecord(String email, String otp) {
        try {
            // Chuyển map chứa email và otp thành chuỗi JSON
            String payload = objectMapper.writeValueAsString(Map.of(
                    "email", email,
                    "otp", otp
            ));
            // Tạo bản ghi sự kiện với ID ngẫu nhiên, gắn nhãn là người dùng mới đăng ký
            return new OutboxRecord(UUID.randomUUID(), "USER_REGISTERED", "auth-service", payload);
        } catch (JsonProcessingException e) {
            // Nếu không thể chuyển đổi dữ liệu thành JSON (thường do lỗi hệ thống/thư viện),
            // gói lỗi này thành unchecked exception (IllegalStateException) 
            // để báo hiệu trạng thái không hợp lệ mà không cần đổi chữ ký của hàm.
            throw new IllegalStateException("Failed to serialize outbox event payload", e);
        }
    }

    private void validate(RegisterCommand cmd) {
        if (cmd.email() == null || cmd.email().isBlank()) {
            throw new BadRequestException("Email is required");
        }
        if (cmd.password() == null || cmd.password().isBlank()) {
            throw new BadRequestException("Password is required");
        }
        if (!cmd.password().equals(cmd.confirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }
    }
}
