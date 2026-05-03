package com.twohands.authservice.delivery.http;

import com.twohands.authservice.application.auth.forgot.ForgotPasswordCommand;
import com.twohands.authservice.application.auth.forgot.ForgotPasswordUseCase;
import com.twohands.authservice.application.auth.forgot.ResetPasswordCommand;
import com.twohands.authservice.application.auth.forgot.ResetPasswordUseCase;
import com.twohands.authservice.application.auth.login.LoginCommand;
import com.twohands.authservice.application.auth.login.LoginResult;
import com.twohands.authservice.application.auth.login.LoginUseCase;
import com.twohands.authservice.application.auth.logout.LogoutCommand;
import com.twohands.authservice.application.auth.logout.LogoutUseCase;
import com.twohands.authservice.application.auth.password.ChangePasswordCommand;
import com.twohands.authservice.application.auth.password.ChangePasswordUseCase;
import com.twohands.authservice.application.auth.oauth.OAuthLoginCommand;
import com.twohands.authservice.application.auth.oauth.OAuthLoginResult;
import com.twohands.authservice.application.auth.oauth.OAuthLoginUseCase;
import com.twohands.authservice.application.auth.ratelimit.RateLimitService;
import com.twohands.authservice.application.auth.session.ListSessionsUseCase;
import com.twohands.authservice.application.auth.session.RevokeAllSessionsUseCase;
import com.twohands.authservice.application.auth.session.RevokeSessionUseCase;
import com.twohands.authservice.application.auth.register.RegisterCommand;
import com.twohands.authservice.application.auth.register.RegisterResult;
import com.twohands.authservice.application.auth.register.RegisterUseCase;
import com.twohands.authservice.application.auth.token.RefreshTokenCommand;
import com.twohands.authservice.application.auth.token.RefreshTokenResult;
import com.twohands.authservice.application.auth.token.RefreshTokenUseCase;
import com.twohands.authservice.application.auth.verify.VerifyEmailUseCase;
import com.twohands.authservice.delivery.http.dto.ChangePasswordRequest;
import com.twohands.authservice.delivery.http.dto.ForgotPasswordRequest;
import com.twohands.authservice.delivery.http.dto.LoginRequest;
import com.twohands.authservice.delivery.http.dto.LoginResponse;
import com.twohands.authservice.delivery.http.dto.LogoutRequest;
import com.twohands.authservice.delivery.http.dto.OAuthLoginRequest;
import com.twohands.authservice.delivery.http.dto.RevokeSessionRequest;
import com.twohands.authservice.delivery.http.dto.RefreshTokenRequest;
import com.twohands.authservice.delivery.http.dto.RefreshTokenResponse;
import com.twohands.authservice.delivery.http.dto.RegisterRequest;
import com.twohands.authservice.delivery.http.dto.RegisterResponse;
import com.twohands.authservice.delivery.http.dto.ResetPasswordRequest;
import com.twohands.authservice.delivery.http.dto.SessionResponse;
import com.twohands.authservice.delivery.http.dto.VerifyRequest;
import com.twohands.authservice.delivery.http.dto.VerifyResponse;
import com.twohands.authservice.domain.session.RefreshTokenSession;
import com.twohands.authservice.shared.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final OAuthLoginUseCase oauthLoginUseCase;
    private final ListSessionsUseCase listSessionsUseCase;
    private final RevokeSessionUseCase revokeSessionUseCase;
    private final RevokeAllSessionsUseCase revokeAllSessionsUseCase;
    private final RateLimitService rateLimitService;

    public AuthController(RegisterUseCase registerUseCase,
                          VerifyEmailUseCase verifyEmailUseCase,
                          LoginUseCase loginUseCase,
                          LogoutUseCase logoutUseCase,
                          RefreshTokenUseCase refreshTokenUseCase,
                          ForgotPasswordUseCase forgotPasswordUseCase,
                          ResetPasswordUseCase resetPasswordUseCase,
                          ChangePasswordUseCase changePasswordUseCase,
                          OAuthLoginUseCase oauthLoginUseCase,
                          ListSessionsUseCase listSessionsUseCase,
                          RevokeSessionUseCase revokeSessionUseCase,
                          RevokeAllSessionsUseCase revokeAllSessionsUseCase,
                          RateLimitService rateLimitService) {
        this.registerUseCase = registerUseCase;
        this.verifyEmailUseCase = verifyEmailUseCase;
        this.loginUseCase = loginUseCase;
        this.logoutUseCase = logoutUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.forgotPasswordUseCase = forgotPasswordUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
        this.oauthLoginUseCase = oauthLoginUseCase;
        this.listSessionsUseCase = listSessionsUseCase;
        this.revokeSessionUseCase = revokeSessionUseCase;
        this.revokeAllSessionsUseCase = revokeAllSessionsUseCase;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest req) {
        // 1. Mapping Request DTO to a Command object to maintain Clean Architecture boundaries
        // Ánh xạ dữ liệu từ Request DTO sang đối tượng Command để giữ đúng ranh giới kiến trúc sạch
        RegisterCommand cmd = new RegisterCommand(
                req.getEmail(),
                req.getPassword(),
                req.getConfirmPassword()
        );

        // 2. Execute the registration business logic via the Use Case
        // Thực thi logic nghiệp vụ đăng ký thông qua Use Case
        RegisterResult result = registerUseCase.execute(cmd);

        // 3. Return a 200 OK response wrapped in a standardized Response DTO
        // Trả về phản hồi 200 OK được đóng gói trong một đối tượng Response chuẩn hóa
        return ResponseEntity.ok(new RegisterResponse(
                result.userId(),
                result.status(),
                "Registration successful. Please check your email to verify your account."
        ));
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifyResponse> verify(
            @RequestBody VerifyRequest req,
            HttpServletRequest request
    ) {
        // 1. Identify the actual client IP (handling proxies/load balancers)
        // Xác định địa chỉ IP thực của khách hàng (xử lý qua proxy hoặc bộ cân bằng tải)
        String ip = getClientIp(request);

        // 2. Apply rate limiting to prevent OTP brute-force attacks from this IP
        // Áp dụng giới hạn tần suất để ngăn chặn tấn công dò mã OTP từ IP này
        rateLimitService.check("verify", ip);

        // 3. Execute the business logic to validate the OTP and activate the user
        // Thực thi logic nghiệp vụ để kiểm tra mã OTP và kích hoạt người dùng
        verifyEmailUseCase.execute(req.getEmail(), req.getOtp());

        // 4. Return a successful response upon valid verification
        // Trả về phản hồi thành công sau khi xác thực hợp lệ
        return ResponseEntity.ok(new VerifyResponse("Email verified successfully. Your account is now active."));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest req,
            HttpServletRequest request
    ) {
        // 1. Collect metadata for security audit and session management
        // Thu thập siêu dữ liệu để kiểm toán bảo mật và quản lý phiên làm việc
        
        // Identify the actual origin IP address of the client
        // Xác định địa chỉ IP gốc thực tế của khách hàng
        String ip = getClientIp(request);        
        String userAgent = request.getHeader("User-Agent"); // Identify browser/app type (Nhận diện trình duyệt/ứng dụng)
        String deviceId = request.getHeader("X-Device-Id"); // Unique ID for hardware tracking (ID duy nhất để theo dõi thiết bị)

        // 2. Encapsulate data into a Command object (CQRS pattern influence)
        // Đóng gói dữ liệu vào đối tượng Command (ảnh hưởng từ mô hình CQRS)
        LoginCommand cmd = new LoginCommand(
                req.getEmail(),
                req.getPassword(),
                ip,
                userAgent,
                deviceId
        );

        // 3. Delegate authentication logic to the specialized Use Case
        // Ủy thác logic xác thực cho Use Case chuyên biệt xử lý
        LoginResult result = loginUseCase.execute(cmd);

        // 4. Map the internal result to a DTO for the client response
        // Ánh xạ kết quả nội bộ sang DTO để trả về cho khách hàng
        LoginResponse.UserSummary userSummary = new LoginResponse.UserSummary(
                result.userId(),
                result.email(),
                result.status(),
                result.emailVerified(),
                result.roles()
        );

        // 5. Return tokens and user info (Success 200 OK)
        // Trả về các token và thông tin người dùng (Thành công 200 OK)
        return ResponseEntity.ok(new LoginResponse(
                result.accessToken(),
                result.refreshToken(),
                result.tokenType(),
                result.expiresIn(),
                userSummary
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody LogoutRequest req, HttpServletRequest request) {
        rateLimitService.check("session-revoke", getClientIp(request));
        logoutUseCase.execute(new LogoutCommand(req.getRefreshToken()));
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(
            @RequestBody RefreshTokenRequest req,
            HttpServletRequest request
    ) {
        String ip = getClientIp(request);
        rateLimitService.check("refresh", ip);
        String userAgent = request.getHeader("User-Agent");
        String deviceId = request.getHeader("X-Device-Id");

        RefreshTokenCommand cmd = new RefreshTokenCommand(
                req.getRefreshToken(),
                ip,
                userAgent,
                deviceId
        );

        RefreshTokenResult result = refreshTokenUseCase.execute(cmd);

        return ResponseEntity.ok(new RefreshTokenResponse(
                result.accessToken(),
                result.refreshToken(),
                result.tokenType(),
                result.expiresIn()
        ));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Forgot / Reset / Change Password
    // ──────────────────────────────────────────────────────────────────────────

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @RequestBody ForgotPasswordRequest req,
            HttpServletRequest request
    ) {
        forgotPasswordUseCase.execute(new ForgotPasswordCommand(req.getEmail(), getClientIp(request)));
        return ResponseEntity.ok(Map.of("message",
                "If the account exists, password reset instructions have been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequest req) {
        resetPasswordUseCase.execute(new ResetPasswordCommand(
                req.getEmail(),
                req.getResetToken(),
                req.getNewPassword(),
                req.getConfirmPassword()
        ));
        return ResponseEntity.ok(Map.of("message", "Password reset successfully. Please log in with your new password."));
    }

    /** Authenticated endpoint — requires a valid Bearer token. */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody ChangePasswordRequest req) {
        UUID userId = SecurityUtils.getCurrentUserId();
        changePasswordUseCase.execute(new ChangePasswordCommand(
                userId,
                req.getCurrentPassword(),
                req.getNewPassword(),
                req.getConfirmPassword()
        ));
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @PostMapping("/oauth/google")
    public ResponseEntity<LoginResponse> oauthGoogle(@RequestBody OAuthLoginRequest req, HttpServletRequest request) {
        return oauthLogin("google", req, request);
    }

    @PostMapping("/oauth/facebook")
    public ResponseEntity<LoginResponse> oauthFacebook(@RequestBody OAuthLoginRequest req, HttpServletRequest request) {
        return oauthLogin("facebook", req, request);
    }

    private ResponseEntity<LoginResponse> oauthLogin(String provider, OAuthLoginRequest req, HttpServletRequest request) {
        OAuthLoginResult result = oauthLoginUseCase.execute(new OAuthLoginCommand(
                provider,
                req.getToken(),
                getClientIp(request),
                request.getHeader("User-Agent"),
                request.getHeader("X-Device-Id")
        ));

        LoginResponse.UserSummary userSummary = new LoginResponse.UserSummary(
                result.userId(),
                result.email(),
                result.status(),
                result.emailVerified(),
                result.roles()
        );
        return ResponseEntity.ok(new LoginResponse(
                result.accessToken(),
                result.refreshToken(),
                result.tokenType(),
                result.expiresIn(),
                userSummary
        ));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<SessionResponse>> listMySessions(HttpServletRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        rateLimitService.check("session-list", getClientIp(request));
        List<RefreshTokenSession> sessions = listSessionsUseCase.execute(userId);
        return ResponseEntity.ok(sessions.stream().map(this::toSessionResponse).toList());
    }

    @PostMapping("/sessions/revoke")
    public ResponseEntity<Map<String, String>> revokeSession(@RequestBody RevokeSessionRequest req, HttpServletRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        rateLimitService.check("session-revoke", getClientIp(request));
        revokeSessionUseCase.execute(userId, req.getSessionId());
        return ResponseEntity.ok(Map.of("message", "Session revoked successfully"));
    }

    @PostMapping("/sessions/revoke-all")
    public ResponseEntity<Map<String, String>> revokeAllSessions(HttpServletRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        rateLimitService.check("session-revoke", getClientIp(request));
        revokeAllSessionsUseCase.execute(userId);
        return ResponseEntity.ok(Map.of("message", "All sessions revoked successfully"));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Map<String, String>> revokeSessionByPath(
            @PathVariable UUID sessionId,
            HttpServletRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        rateLimitService.check("session-revoke", getClientIp(request));
        revokeSessionUseCase.execute(userId, sessionId);
        return ResponseEntity.ok(Map.of("message", "Session revoked successfully"));
    }

    private SessionResponse toSessionResponse(RefreshTokenSession session) {
        SessionResponse response = new SessionResponse();
        response.setSessionId(session.getId());
        response.setDeviceId(session.getDeviceId());
        response.setIpAddress(session.getIpAddress());
        response.setUserAgent(session.getUserAgent());
        response.setExpiresAt(session.getExpiresAt());
        response.setRevoked(session.isRevoked());
        response.setStatus(session.getStatus() != null ? session.getStatus().name() : null);
        response.setCreatedAt(session.getCreatedAt());
        response.setUpdatedAt(session.getUpdatedAt());
        return response;
    }

    private String getClientIp(HttpServletRequest request) {
        // 1. Check the 'X-Forwarded-For' header for the real client IP address
        // Kiểm tra header 'X-Forwarded-For' để tìm địa chỉ IP thực của khách hàng
        String ip = request.getHeader("X-Forwarded-For");

        // 2. If the header is missing, empty, or 'unknown', fallback to the direct remote address
        // Nếu header bị thiếu, trống hoặc mang giá trị 'unknown', quay lại dùng địa chỉ kết nối trực tiếp
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            // Retrieve the IP address of the client or last proxy that sent the request
            // Lấy địa chỉ IP của khách hàng hoặc proxy cuối cùng gửi yêu cầu đến
            ip = request.getRemoteAddr();
        }
        
        // 3. Return the identified IP address (could be a comma-separated list if multiple proxies are used)
        // Trả về địa chỉ IP đã xác định (có thể là danh sách cách nhau bởi dấu phẩy nếu qua nhiều proxy)
        return ip;
    }
}
