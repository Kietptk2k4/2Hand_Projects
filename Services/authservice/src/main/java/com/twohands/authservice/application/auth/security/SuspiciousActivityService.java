package com.twohands.authservice.application.auth.security;

import com.twohands.authservice.application.auth.port.AttemptStore;
import com.twohands.authservice.domain.login.LoginFailureReason;
import com.twohands.authservice.domain.session.RefreshTokenSessionRepository;
import com.twohands.authservice.domain.user.User;
import com.twohands.authservice.domain.user.UserRepository;
import com.twohands.authservice.domain.user.UserStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SuspiciousActivityService {

    private final AttemptStore attemptStore;
    private final UserRepository userRepository;
    private final RefreshTokenSessionRepository sessionRepository;
    private final SecurityAuditLogger auditLogger;

    @Value("${auth.security.failed-login-lock-threshold:5}")
    private int failedLoginLockThreshold;

    @Value("${auth.security.failed-login-window-seconds:900}")
    private long failedLoginWindowSeconds;

    @Value("${auth.security.suspicious-reuse-lock-threshold:2}")
    private int suspiciousReuseLockThreshold;

    @Value("${auth.security.suspicious-reuse-window-seconds:900}")
    private long suspiciousReuseWindowSeconds;

    public SuspiciousActivityService(AttemptStore attemptStore,
                                     UserRepository userRepository,
                                     RefreshTokenSessionRepository sessionRepository,
                                     SecurityAuditLogger auditLogger) {
        this.attemptStore = attemptStore;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.auditLogger = auditLogger;
    }

    public void onLoginSuccess(UUID userId) {
        attemptStore.delete(failedLoginKey(userId));
    }

    // /**
    //  * Xử lý sự kiện khi người dùng đăng nhập thất bại.
    //  * Hàm này sẽ tăng bộ đếm số lần đăng nhập sai. Nếu số lần sai vượt quá 
    //  * ngưỡng cho phép trong một khoảng thời gian nhất định, tài khoản sẽ bị khóa.
    //  * * @Transactional đảm bảo tính toàn vẹn dữ liệu: việc tăng bộ đếm và khóa tài khoản
    //  * (nếu xảy ra) sẽ được thực thi trong cùng một transaction.
    //  *
    //  * @param userId    Mã định danh (UUID) của người dùng đăng nhập thất bại.
    //  * @param ipAddress Địa chỉ IP của thiết bị thực hiện yêu cầu đăng nhập.
    //  * @param reason    Nguyên nhân đăng nhập thất bại (ví dụ: sai mật khẩu, sai OTP).
    //  */
    @Transactional
    public void onLoginFailure(UUID userId, String ipAddress, LoginFailureReason reason) {
        
        // Tăng bộ đếm số lần đăng nhập thất bại của user này trong Redis/Cache
        // failedLoginWindowSeconds: Thời gian sống (TTL) của bộ đếm (ví dụ: đếm trong vòng 15 phút)
        long count = attemptStore.increment(failedLoginKey(userId), failedLoginWindowSeconds);
        
        // Ghi log audit để theo dõi bảo mật, bao gồm lý do, IP và số lần đã thất bại
        auditLogger.warn("LOGIN_FAILURE", userId, "reason=" + reason + ", ip=" + ipAddress + ", count=" + count);
        
        // Kiểm tra xem số lần thất bại đã đạt hoặc vượt ngưỡng giới hạn cho phép chưa
        if (count >= failedLoginLockThreshold) {
            // Khóa tài khoản nếu vượt ngưỡng để ngăn chặn tấn công Brute-force
            lockAccount(userId, "Repeated login failures");
        }
    }

    @Transactional
    public void onRefreshTokenReuseDetected(UUID userId, String ipAddress) {
        long count = attemptStore.increment(suspiciousReuseKey(userId), suspiciousReuseWindowSeconds);
        auditLogger.warn("REFRESH_TOKEN_REUSE", userId, "ip=" + ipAddress + ", count=" + count);
        sessionRepository.revokeAllActiveByUserId(userId);
        if (count >= suspiciousReuseLockThreshold) {
            lockAccount(userId, "Suspicious refresh-token reuse detected");
        }
    }

    // /**
    //  * Khóa tài khoản người dùng và thu hồi tất cả các phiên đăng nhập đang hoạt động.
    //  * @Transactional đảm bảo việc cập nhật trạng thái user và xóa/hủy session 
    //  * được thực thi trong cùng một transaction, tránh tình trạng bất đồng bộ dữ liệu 
    //  * (ví dụ: tài khoản đã báo khóa nhưng người dùng vẫn còn giữ token/session hợp lệ).
    //  *
    //  * @param userId Mã định danh (UUID) của người dùng cần khóa.
    //  * @param reason Lý do khóa tài khoản (ví dụ: "Repeated login failures").
    //  */
    @Transactional
    public void lockAccount(UUID userId, String reason) {
        // Tìm kiếm user trong database. ifPresent() giúp xử lý gọn gàng và an toàn 
        // cho trường hợp user không tồn tại, tránh lỗi NullPointerException.
        userRepository.findById(userId).ifPresent(user -> {
            
            // Chỉ thực hiện các thao tác nếu tài khoản CHƯA ở trạng thái LOCKED
            // (Tính Idempotent: gọi hàm nhiều lần vẫn cho ra cùng một kết quả, tránh query DB dư thừa)
            if (user.getStatus() != UserStatus.LOCKED) {
                
                // 1. Cập nhật trạng thái tài khoản
                user.setStatus(UserStatus.LOCKED);
                userRepository.save(user);
                
                // 2. Thu hồi toàn bộ các phiên đăng nhập (session/token) hiện tại
                // Thao tác này cực kỳ quan trọng: nó ép người dùng bị đăng xuất 
                // ngay lập tức trên tất cả các thiết bị (Web, App,...).
                sessionRepository.revokeAllActiveByUserId(userId);
                
                // 3. Ghi log kiểm toán lưu vết hệ thống
                auditLogger.warn("ACCOUNT_LOCKED", userId, reason);
            }
        });
    }

    private String failedLoginKey(UUID userId) {
        return "auth:security:failed-login:user:" + userId;
    }

    private String suspiciousReuseKey(UUID userId) {
        return "auth:security:suspicious-reuse:user:" + userId;
    }
}
