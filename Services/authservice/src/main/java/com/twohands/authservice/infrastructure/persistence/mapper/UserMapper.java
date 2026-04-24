package com.twohands.authservice.infrastructure.persistence.mapper;

import com.twohands.authservice.domain.user.User;
import com.twohands.authservice.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    private final RoleMapper roleMapper;
    private final UserSettingsMapper userSettingsMapper;
    private final UserProfileMapper userProfileMapper;
    private final LoginLogMapper loginLogMapper;
    private final OAuthAccountMapper oAuthAccountMapper;
    private final RefreshTokenSessionMapper refreshTokenSessionMapper;

    public UserMapper(RoleMapper roleMapper,
                      UserSettingsMapper userSettingsMapper,
                      UserProfileMapper userProfileMapper,
                      LoginLogMapper loginLogMapper,
                      OAuthAccountMapper oAuthAccountMapper,
                      RefreshTokenSessionMapper refreshTokenSessionMapper) {
        this.roleMapper = roleMapper;
        this.userSettingsMapper = userSettingsMapper;
        this.userProfileMapper = userProfileMapper;
        this.loginLogMapper = loginLogMapper;
        this.oAuthAccountMapper = oAuthAccountMapper;
        this.refreshTokenSessionMapper = refreshTokenSessionMapper;
    }

    /**
     * Chuyển đổi dữ liệu từ thực thể Database (UserEntity) sang đối tượng nghiệp vụ (User Domain).
     * Mục đích: Lấy dữ liệu đã lưu trữ để thực hiện các tính năng logic, kiểm tra quyền hoặc hiển thị.
     */
    public User toDomain(UserEntity entity) {
        // 1. Kiểm tra nếu thực thể rỗng (chưa có trong DB) thì trả về null
        if (entity == null) {
            return null;
        }

        // 2. Khởi tạo đối tượng Domain và đổ các thông tin định danh cơ bản từ Database vào
        User user = new User();
        user.setId(entity.getId());
        user.setEmail(entity.getEmail());
        user.setEmailNormalized(entity.getEmailNormalized());
        user.setPhone(entity.getPhone());
        user.setPasswordHash(entity.getPasswordHash());
        user.setStatus(entity.getStatus());
        user.setEmailVerified(entity.getEmailVerified());
        user.setPhoneVerified(entity.getPhoneVerified());
        user.setPasswordChangedAt(entity.getPasswordChangedAt());
        user.setLastLoginAt(entity.getLastLoginAt());
        user.setCreatedAt(entity.getCreatedAt());
        user.setUpdatedAt(entity.getUpdatedAt());

        // 3. Tái cấu trúc danh sách vai trò (Roles):
        // Chuyển đổi các bản ghi quyền hạn từ DB thành tập hợp (Set) các quyền trong logic xử lý.
        user.setRoles(entity.getRoles().stream()
                // Giải nén quyền hạn: Chuyển đổi các thực thể Role từ DB thành đối tượng nghiệp vụ,
                // đồng thời làm phẳng danh sách Permission đi kèm để chuẩn bị cho việc kiểm tra quyền (Authorization).
                // Biến đổi thực thể Role thành đối tượng Role nghiệp vụ
                .map(roleMapper::toDomain)
                .collect(Collectors.toSet())); // Gom lại thành Set (đảm bảo không trùng lặp quyền)

        // 4. Khôi phục các thông tin mở rộng (1-1): Cấu hình và Hồ sơ người dùng
        user.setSettings(userSettingsMapper.toDomain(entity.getSettings()));
        user.setProfile(userProfileMapper.toDomain(entity.getProfile()));

        // 5. Khôi phục danh sách lịch sử đăng nhập: 
        // Giúp hệ thống xem lại các lần truy cập từ các thiết bị và IP khác nhau.
        user.setLoginLogs(entity.getLoginLogs().stream()
                // Đọc lịch sử truy cập: Chuyển đổi các bản ghi log thô thành danh sách lịch sử hoạt động,
                // trích xuất ID người dùng để hiển thị thông tin thiết bị, IP và thời gian đăng nhập.
                .map(loginLogMapper::toDomain)
                .collect(Collectors.toList()));

        // 6. Khôi phục các liên kết tài khoản mạng xã hội (OAuth):
        // Để kiểm tra người dùng này đã liên kết với Google, Facebook... hay chưa.
        user.setOauthAccounts(entity.getOauthAccounts().stream()
                // Nhận diện liên kết mạng xã hội: Chuyển đổi thông tin tài khoản Google/Facebook... từ DB
                // sang đối tượng Domain để xác định phương thức đăng nhập và các định danh từ bên thứ ba.
                .map(oAuthAccountMapper::toDomain)
                .collect(Collectors.toList()));

        // 7. Khôi phục các phiên làm việc (Tokens): 
        // Dùng để kiểm tra tính hợp lệ của token khi người dùng quay lại ứng dụng.
        user.setRefreshTokenSessions(entity.getRefreshTokenSessions().stream()
                // Kiểm tra phiên đăng nhập: Chuyển đổi dữ liệu phiên từ Database thành các đối tượng Session,
                // phục vụ việc xác thực Token, kiểm tra trạng thái thu hồi (revoked) và thời gian hết hạn.
                .map(refreshTokenSessionMapper::toDomain)
                .collect(Collectors.toList()));

        // 8. Trả về đối tượng User hoàn chỉnh chứa đầy đủ thông tin liên quan
        return user;
    }

    public UserEntity toEntity(User domain) {
        // 1. Kiểm tra null đầu vào để tránh lỗi NullPointerException
        if (domain == null) {
            return null;
        }

        // 2. Khởi tạo đối tượng Entity và copy các thông tin cơ bản (Fields đơn giản)
        UserEntity entity = new UserEntity();
        entity.setId(domain.getId());
        entity.setEmail(domain.getEmail());
        entity.setEmailNormalized(domain.getEmailNormalized());
        entity.setPhone(domain.getPhone());
        entity.setPasswordHash(domain.getPasswordHash());
        entity.setStatus(domain.getStatus());
        entity.setEmailVerified(domain.getEmailVerified());
        entity.setPhoneVerified(domain.getPhoneVerified());
        entity.setPasswordChangedAt(domain.getPasswordChangedAt());
        entity.setLastLoginAt(domain.getLastLoginAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        // 3. Chuyển đổi danh sách Roles từ Domain sang Entity
        if (domain.getRoles() != null) {
            entity.setRoles(domain.getRoles().stream()
                    // Ánh xạ danh sách vai trò (Admin, User...): Chuyển đổi quyền hạn từ tầng nghiệp vụ sang 
                    // thực thể Database để hệ thống biết người dùng này có những quyền truy cập nào.
                    .map(roleMapper::toEntity)
                    .collect(Collectors.toSet()));
        }

        // 4. Mapping các đối tượng 1-1 (Settings và Profile)
        entity.setSettings(userSettingsMapper.toEntity(domain.getSettings()));
        entity.setProfile(userProfileMapper.toEntity(domain.getProfile()));

        // 5. Xử lý danh sách Lịch sử đăng nhập (LoginLogs)
        if (domain.getLoginLogs() != null) {
            entity.setLoginLogs(domain.getLoginLogs().stream()
                    // Khởi tạo bản ghi lịch sử: Chuyển đổi dữ liệu đăng nhập (IP, thiết bị, thời gian) thành 
                    // thực thể lưu trữ, phục vụ cho việc truy vết bảo mật và kiểm tra lịch sử hoạt động.
                    .map(loginLogMapper::toEntity) // Chuyển sang Entity
                    // Quan trọng: Gán ngược 'entity' User này vào từng log để giữ liên kết 2 chiều
                    .peek(log -> log.setUser(entity))
                    .collect(Collectors.toList()));
        }

        // 6. Xử lý danh sách tài khoản liên kết (OAuth Accounts)
        if (domain.getOauthAccounts() != null) {
            entity.setOauthAccounts(domain.getOauthAccounts().stream()
                    // Đồng bộ tài khoản liên kết: Chuyển đổi thông tin định danh từ các bên thứ ba (Google, Facebook...)
                    // sang thực thể liên kết, cho phép người dùng đăng nhập đa phương thức vào hệ thống.
                    .map(oAuthAccountMapper::toEntity)
                    // Thiết lập quan hệ 2 chiều: gán User hiện tại cho tài khoản OAuth
                    .peek(account -> account.setUser(entity))
                    .collect(Collectors.toList()));
        }

        // 7. Xử lý danh sách phiên làm việc (Refresh Token Sessions)
        if (domain.getRefreshTokenSessions() != null) {
            entity.setRefreshTokenSessions(domain.getRefreshTokenSessions().stream()
                    // Quản lý phiên duy trì đăng nhập: Chuyển đổi các thông tin Token Hash và thời gian hết hạn 
                    // thành thực thể phiên (Session), giúp hệ thống duy trì hoặc thu hồi quyền truy cập từ xa.
                    .map(refreshTokenSessionMapper::toEntity)
                    // Thiết lập quan hệ 2 chiều: gán User hiện tại cho session
                    .peek(session -> session.setUser(entity))
                    .collect(Collectors.toList()));
        }

        // 8. Đảm bảo liên kết 2 chiều cho Settings và Profile (nếu có)
        // Điều này giúp Hibernate lưu đúng khóa ngoại (Foreign Key) vào DB
        if (entity.getSettings() != null) {
            entity.getSettings().setUser(entity);
        }
        if (entity.getProfile() != null) {
            entity.getProfile().setUser(entity);
        }

        return entity;
    }
}
