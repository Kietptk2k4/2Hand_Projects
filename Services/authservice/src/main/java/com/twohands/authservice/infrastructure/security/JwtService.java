package com.twohands.authservice.infrastructure.security;

import com.twohands.authservice.application.auth.port.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtService implements TokenService {

    // Inject the JWT secret key string
    // Inject chuỗi khóa bí mật JWT
    @Value("${auth.jwt.secret}")
    private String secret;

    // Load the issuer identifier to attach to tokens, indicating which server generated them.
    // Load định danh của tổ chức phát hành để đính kèm vào token, cho biết server nào đã generate ra chúng.
    @Value("${auth.jwt.issuer}")
    private String issuer;

    // Map the access token's lifespan (in seconds) from properties to manage token expiration logic.
    // Map thời gian sống của access token (tính bằng giây) từ properties để quản lý logic hết hạn của token.
    @Value("${auth.jwt.access-token-expiration-seconds}")
    private long expirationSeconds;

    // Declare a SecretKey object to store the cryptographically secure key used for signing tokens.
    // Khai báo một object SecretKey để lưu trữ khóa mã hóa bảo mật dùng cho việc ký các token.
    private SecretKey secretKey;

    // Use @PostConstruct to trigger this setup method automatically right after Spring finishes dependency injection.
    // Dùng @PostConstruct để trigger hàm setup này tự động ngay sau khi Spring hoàn tất quá trình dependency injection.
    @PostConstruct
    public void init() {
        
        // Convert the plain text secret string into a UTF-8 byte array for cryptographic processing.
        // Convert chuỗi secret dạng plain text thành một mảng byte UTF-8 để phục vụ cho quá trình xử lý mã hóa.
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        
        // Validate the key length to ensure it meets the minimum security requirement for the HMAC-SHA256 algorithm.
        // Validate độ dài của khóa để đảm bảo nó đáp ứng yêu cầu bảo mật tối thiểu (256 bits) cho thuật toán HMAC-SHA256.
        if (keyBytes.length < 32) {
            
            // Throw a runtime exception to crash the app immediately on startup if the secret key is too weak.
            // Throw ra một runtime exception để crash app ngay lập tức lúc startup nếu khóa bí mật cấu hình quá yếu.
            throw new IllegalStateException(
                    "JWT secret must be at least 32 characters (256 bits). Check auth.jwt.secret.");
        }
        
        // Generate and assign the final SecretKey instance using the validated byte array.
        // Generate và gán instance SecretKey cuối cùng bằng cách sử dụng mảng byte đã được validate.
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // Override the method from the interface to implement the JWT access token generation logic.
    // Ghi đè phương thức từ interface để triển khai logic tạo mã thông báo truy cập JWT.
    @Override
    public String issueAccessToken(UUID userId, String email, List<String> roles) {
        
        // Get the current system time in milliseconds to calculate token lifecycle events.
        // Lấy thời gian hệ thống hiện tại tính bằng mili giây để tính toán các mốc thời gian của vòng đời token.
        long nowMs = System.currentTimeMillis();
        
        // Initialize the JWT builder to construct the token's payload and cryptographic signature.
        // Khởi tạo trình dựng JWT để thiết lập phần dữ liệu (payload) và chữ ký mã hóa của token.
        return Jwts.builder()
                
                // Set the 'sub' (Subject) claim using the user's unique ID to identify who the token belongs to.
                // Thiết lập claim 'sub' (Chủ thể) bằng ID duy nhất của người dùng để xác định token thuộc về ai.
                .subject(userId.toString())
                
                // Set the 'iss' (Issuer) claim to specify the server or application that issued this token.
                // Thiết lập claim 'iss' (Tổ chức phát hành) để chỉ định máy chủ hoặc ứng dụng đã cấp token này.
                .issuer(issuer)
                
                // Add a custom claim to embed the user's email directly into the token payload.
                // Thêm một claim tùy chỉnh để nhúng trực tiếp email của người dùng vào dữ liệu token.
                .claim("email", email)
                
                // Add another custom claim containing the user's roles for authorization purposes on the client side.
                // Thêm một claim tùy chỉnh khác chứa các quyền của người dùng phục vụ cho mục đích phân quyền ở phía client.
                .claim("roles", roles)
                
                // Set the 'iat' (Issued At) claim to record the exact moment this token was created.
                // Thiết lập claim 'iat' (Thời điểm phát hành) để ghi lại thời khắc chính xác token này được tạo ra.
                .issuedAt(new Date(nowMs))
                
                // Calculate and set the 'exp' (Expiration Time) claim so the token automatically expires after a specific duration.
                // Tính toán và thiết lập claim 'exp' (Thời gian hết hạn) để token tự động hết hiệu lực sau một khoảng thời gian nhất định.
                .expiration(new Date(nowMs + expirationSeconds * 1000L))
                
                // Secure the token by cryptographically signing it using the configured secret key.
                // Bảo mật token bằng cách ký mã hóa nó sử dụng khóa bí mật đã được cấu hình từ trước.
                .signWith(secretKey)
                
                // Serialize all the configured claims and the signature into a compact, URL-safe String format.
                // Đóng gói tất cả các claim đã thiết lập và chữ ký thành một định dạng chuỗi String nhỏ gọn, an toàn cho URL.
                .compact();
    }

    @Override
    public long getAccessTokenExpirationSeconds() {
        return expirationSeconds;
    }

    @Override
    public UUID extractUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return UUID.fromString(claims.getSubject());
    }
}
