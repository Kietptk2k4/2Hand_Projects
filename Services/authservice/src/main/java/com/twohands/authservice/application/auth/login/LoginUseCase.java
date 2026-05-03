package com.twohands.authservice.application.auth.login;

import com.twohands.authservice.application.auth.port.PasswordHasher;
import com.twohands.authservice.application.auth.port.RefreshTokenGenerator;
import com.twohands.authservice.application.auth.port.TokenHasher;
import com.twohands.authservice.application.auth.port.TokenService;
import com.twohands.authservice.application.auth.ratelimit.RateLimitService;
import com.twohands.authservice.application.auth.security.AccountStatusGuard;
import com.twohands.authservice.application.auth.security.SecurityAuditLogger;
import com.twohands.authservice.application.auth.security.SuspiciousActivityService;
import com.twohands.authservice.delivery.http.exception.BadRequestException;
import com.twohands.authservice.delivery.http.exception.UnauthorizedException;
import com.twohands.authservice.domain.login.LoginFailureReason;
import com.twohands.authservice.domain.login.LoginLog;
import com.twohands.authservice.domain.login.LoginLogRepository;
import com.twohands.authservice.domain.role.Role;
import com.twohands.authservice.domain.session.RefreshTokenSession;
import com.twohands.authservice.domain.session.RefreshTokenSessionRepository;
import com.twohands.authservice.domain.session.RefreshTokenStatus;
import com.twohands.authservice.domain.user.User;
import com.twohands.authservice.domain.user.UserRepository;
import com.twohands.authservice.domain.user.UserStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenService tokenService;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final TokenHasher tokenHasher;
    private final RefreshTokenSessionRepository sessionRepository;
    private final LoginLogRepository loginLogRepository;
    private final RateLimitService rateLimitService;
    private final AccountStatusGuard accountStatusGuard;
    private final SuspiciousActivityService suspiciousActivityService;
    private final SecurityAuditLogger auditLogger;

    @Value("${auth.jwt.refresh-token-expiration-seconds}")
    private long refreshExpirationSeconds;

    public LoginUseCase(UserRepository userRepository,
                        PasswordHasher passwordHasher,
                        TokenService tokenService,
                        RefreshTokenGenerator refreshTokenGenerator,
                        TokenHasher tokenHasher,
                        RefreshTokenSessionRepository sessionRepository,
                        LoginLogRepository loginLogRepository,
                        RateLimitService rateLimitService,
                        AccountStatusGuard accountStatusGuard,
                        SuspiciousActivityService suspiciousActivityService,
                        SecurityAuditLogger auditLogger) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.tokenHasher = tokenHasher;
        this.sessionRepository = sessionRepository;
        this.loginLogRepository = loginLogRepository;
        this.rateLimitService = rateLimitService;
        this.accountStatusGuard = accountStatusGuard;
        this.suspiciousActivityService = suspiciousActivityService;
        this.auditLogger = auditLogger;
    }

    /**
     * Login is @Transactional so that user.lastLoginAt update and session creation are atomic.
     * Login log writes use REQUIRES_NEW propagation so they commit independently,
     * even when a failure exception rolls back the outer transaction.
     */
    @Transactional
    public LoginResult execute(LoginCommand cmd) {
        // 1. Basic input validation (Email/Password format)
        // Kiểm tra tính hợp lệ cơ bản của dữ liệu đầu vào
        validate(cmd);

        // 2. Prevent brute-force by checking IP rate limit
        // Chặn tấn công dò mật khẩu bằng cách kiểm tra giới hạn tần suất của IP
        rateLimitService.check("login", cmd.ipAddress());

        // 3. Normalize email for consistent searching
        // Chuẩn hóa email để tìm kiếm chính xác (viết thường, bỏ khoảng trắng)
        String emailNorm = cmd.email().trim().toLowerCase(Locale.ROOT);

        // 4. Find user by email; hide existence if not found (security best practice)
        // Tìm người dùng; trả về "Unauthorized" chung chung nếu không thấy để bảo mật thông tin
        User user = userRepository.findByEmailNormalized(emailNorm)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        // 5. Verify the provided password against the stored hash
        // Kiểm tra mật khẩu người dùng nhập vào có khớp với bản băm (hash) trong DB không
        if (!passwordHasher.matches(cmd.password(), user.getPasswordHash())) {
            
            // 5.1. Log the failed attempt independently for security auditing
            // Ghi lại nhật ký đăng nhập thất bại để phục vụ việc kiểm soát an ninh
            writeLoginLog(user.getId(), cmd, false, LoginFailureReason.INVALID_PASSWORD);
            
            // 5.2. Trigger suspicious activity logic (e.g., counting for account lockout)
            // Kích hoạt dịch vụ theo dõi hành vi đáng ngờ (ví dụ: đếm số lần sai để khóa tài khoản)
            suspiciousActivityService.onLoginFailure(user.getId(), cmd.ipAddress(), LoginFailureReason.INVALID_PASSWORD);
            
            // 5.3. Throw an exception with a generic message to prevent user enumeration
            // Ném ngoại lệ với thông báo chung chung để tránh lộ thông tin người dùng tồn tại hay không
            throw new UnauthorizedException("Invalid credentials");
        }

        // 6. Check if email is verified
        // Kiểm tra xem email đã được xác thực chưa
        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            writeLoginLog(user.getId(), cmd, false, LoginFailureReason.EMAIL_NOT_VERIFIED);
            suspiciousActivityService.onLoginFailure(user.getId(), cmd.ipAddress(), LoginFailureReason.EMAIL_NOT_VERIFIED);
            throw new UnauthorizedException("Please verify your email before logging in.");
        }

        // 7. Check if account is locked, banned, or restricted
        // Kiểm tra xem tài khoản có bị khóa, bị cấm hoặc hạn chế không
        // Sử dụng khối try-catch để handle exception (bắt lỗi) từ bước kiểm tra điều kiện
        try {
            // 1. Trigger hàm kiểm tra (Guard Clause). 
            // Nếu status hợp lệ, code chạy mượt mà qua dòng này.
            // Nếu status vi phạm (LOCKED, BANNED...), nó sẽ throw ra UnauthorizedException.
            accountStatusGuard.assertCanLogin(user);
            
        } catch (UnauthorizedException ex) {
            // 2. Fallback luồng xử lý khi bắt được lỗi (Chỉ chạy khi login không hợp lệ)
            
            // Xác định nguyên nhân (Reason mapping) bằng toán tử 3 ngôi (ternary operator).
            // Nếu user đang bị LOCKED -> map thành ACCOUNT_LOCKED.
            // Các case còn lại (DELETED, SUSPENDED...) -> gom chung thành ACCOUNT_BANNED.
            LoginFailureReason reason = user.getStatus() == UserStatus.LOCKED
                    ? LoginFailureReason.ACCOUNT_LOCKED
                    : LoginFailureReason.ACCOUNT_BANNED;
                    
            // Lưu vết hệ thống (Audit Logging)
            // Ghi lại record login fail vào database/log file kèm theo ID, thông tin request và lý do.
            writeLoginLog(user.getId(), cmd, false, reason);
            
            // Tracking rủi ro bảo mật (Security Monitoring)
            // Báo cáo sự cố cho service an ninh để theo dõi IP này (phục vụ việc block IP nếu spam/brute-force).
            suspiciousActivityService.onLoginFailure(user.getId(), cmd.ipAddress(), reason);
            
            // Rethrow Exception (CỰC KỲ QUAN TRỌNG)
            // Sau khi đã dọn dẹp và ghi log xong, bắt buộc phải ném lại chính cái lỗi ban đầu ra ngoài.
            // Việc này đảm bảo luồng login bị block hoàn toàn và Controller sẽ catch được để return HTTP 401 cho Frontend.
            throw ex;
        }

        // 8. Update account state: Reactivate if inactive and update last login timestamp
        // Cập nhật trạng thái: Kích hoạt lại nếu đang ẩn và cập nhật thời gian đăng nhập cuối
        if (user.getStatus() == UserStatus.INACTIVE) {
            user.setStatus(UserStatus.ACTIVE);
        }
        user.setLastLoginAt(Instant.now());
        user = userRepository.save(user);

        // 9. Collect roles and issue JWT Access Token
        // Extract the list of roles from the user object and convert it into a data stream.
        // Trích xuất danh sách các quyền từ đối tượng người dùng và chuyển nó thành một luồng dữ liệu.
        List<String> roleCodes = user.getRoles().stream()
        // Map each Role object to its corresponding string code using a method reference.
        // Ánh xạ mỗi đối tượng Role thành chuỗi mã tương ứng bằng cách sử dụng method reference.
        .map(Role::getCode)
        // Collect all the transformed string codes and return them as a new List.
        // Thu thập tất cả các chuỗi mã đã được chuyển đổi và trả về dưới dạng một List mới.
        .collect(Collectors.toList());

        // Call the token service to generate a new JWT access token for the authenticated user.
        // Gọi dịch vụ token để tạo một mã thông báo truy cập JWT mới cho người dùng đã xác thực.
        // Pass the user ID, email, and extracted role codes as parameters to embed into the token payload.
        // Truyền ID người dùng, email và danh sách mã quyền đã trích xuất làm tham số để nhúng vào dữ liệu (payload) của token.
        // Store the resulting token string in the accessToken variable so it can be returned to the client.
        // Lưu chuỗi token kết quả vào biến accessToken để có thể trả về cho phía máy khách (client).
        String accessToken = tokenService.issueAccessToken(user.getId(), user.getEmail(), roleCodes);


        // 10. Generate and hash a persistent Refresh Token (Session)
        // Tạo và băm Refresh Token để quản lý phiên đăng nhập lâu dài
        String plainRefreshToken = refreshTokenGenerator.generate();
        String tokenHash = tokenHasher.hash(plainRefreshToken);

        sessionRepository.save(buildSession(user.getId(), tokenHash, cmd));

        // 11. Final auditing and security success tracking
        // Ghi nhật ký thành công và cập nhật hệ thống theo dõi hành vi
        writeLoginLog(user.getId(), cmd, true, null);
        suspiciousActivityService.onLoginSuccess(user.getId());
        auditLogger.info("LOGIN_SUCCESS", user.getId(), "ip=" + cmd.ipAddress() + ", device=" + cmd.deviceId());

        // 12. Return the complete login response
        // Trả về kết quả đăng nhập đầy đủ
        return new LoginResult(
                accessToken,
                plainRefreshToken,
                "Bearer",
                tokenService.getAccessTokenExpirationSeconds(),
                user.getId(),
                user.getEmail(),
                user.getStatus().name(),
                Boolean.TRUE.equals(user.getEmailVerified()),
                roleCodes
        );
    }

    private void checkAccountStatus(User user, LoginCommand cmd) {
        switch (user.getStatus()) {
            case PENDING_VERIFICATION -> {
                writeLoginLog(user.getId(), cmd, false, LoginFailureReason.EMAIL_NOT_VERIFIED);
                throw new UnauthorizedException("Account is not verified. Please check your email.");
            }
            case SUSPENDED, TEMP_BANNED -> {
                writeLoginLog(user.getId(), cmd, false, LoginFailureReason.ACCOUNT_BANNED);
                throw new UnauthorizedException("Account is suspended.");
            }
            case PERMANENT_BANNED -> {
                writeLoginLog(user.getId(), cmd, false, LoginFailureReason.ACCOUNT_BANNED);
                throw new UnauthorizedException("Account has been permanently banned.");
            }
            case LOCKED -> {
                writeLoginLog(user.getId(), cmd, false, LoginFailureReason.ACCOUNT_LOCKED);
                throw new UnauthorizedException("Account is locked. Please contact support.");
            }
            case UNDER_REVIEW, RESTRICTED -> {
                writeLoginLog(user.getId(), cmd, false, LoginFailureReason.ACCOUNT_BANNED);
                throw new UnauthorizedException("Account access is currently restricted.");
            }
            case DELETED, HARD_DELETED -> {
                writeLoginLog(user.getId(), cmd, false, LoginFailureReason.ACCOUNT_BANNED);
                throw new UnauthorizedException("Invalid credentials");
            }
            default -> { /* ACTIVE, INACTIVE — allow login */ }
        }
    }

    /**
     * Records the login attempt details into the database for auditing and security tracking.
     * Ghi lại chi tiết nỗ lực đăng nhập vào cơ sở dữ liệu để kiểm toán và theo dõi bảo mật.
     */
    private void writeLoginLog(UUID userId, LoginCommand cmd, boolean success, LoginFailureReason reason) {
        // 1. Initialize a new LoginLog entity
        // Khởi tạo một thực thể LoginLog mới
        LoginLog log = new LoginLog();
        
        // 2. Link the log to the user (can be null if user not found)
        // Liên kết nhật ký với người dùng (có thể để trống nếu không tìm thấy người dùng)
        log.setUserId(userId);
        
        // 3. Set the authentication method used
        // Thiết lập phương thức xác thực đã sử dụng
        log.setLoginMethod("EMAIL_PASSWORD");
        
        // 4. Capture network and device metadata from the command
        // Thu thập siêu dữ liệu về mạng và thiết bị từ đối tượng command
        log.setIpAddress(cmd.ipAddress());
        log.setUserAgent(cmd.userAgent());
        
        // 5. Record whether the attempt was successful or failed
        // Ghi lại trạng thái nỗ lực là thành công hay thất bại
        log.setSuccess(success);
        
        // 6. If failed, record the specific reason (e.g., INVALID_PASSWORD)
        // Nếu thất bại, ghi lại lý do cụ thể (ví dụ: Sai mật khẩu)
        log.setFailureReason(reason);
        
        // 7. Persist the log entry to the database
        // Lưu bản ghi nhật ký vào cơ sở dữ liệu
        loginLogRepository.save(log);
    }

    private RefreshTokenSession buildSession(UUID userId, String tokenHash, LoginCommand cmd) {
        RefreshTokenSession session = new RefreshTokenSession();
        session.setUserId(userId);
        session.setTokenHash(tokenHash);
        session.setDeviceId(cmd.deviceId());
        session.setIpAddress(cmd.ipAddress());
        session.setUserAgent(cmd.userAgent());
        session.setExpiresAt(Instant.now().plusSeconds(refreshExpirationSeconds));
        session.setRevoked(false);
        session.setStatus(RefreshTokenStatus.ACTIVE);
        return session;
    }

    /**
     * Validates the mandatory fields of the login command.
     * Kiểm tra tính hợp lệ của các trường bắt buộc trong lệnh đăng nhập.
     */
    private void validate(LoginCommand cmd) {
        // 1. Ensure the email is provided and not just empty spaces
        // Đảm bảo email đã được cung cấp và không chỉ toàn khoảng trắng
        if (cmd.email() == null || cmd.email().isBlank()) {
            throw new BadRequestException("Email is required");
        }

        // 2. Ensure the password is provided and not just empty spaces
        // Đảm bảo mật khẩu đã được cung cấp và không chỉ toàn khoảng trắng
        if (cmd.password() == null || cmd.password().isBlank()) {
            throw new BadRequestException("Password is required");
        }
    }
}
