package com.twohands.authservice.infrastructure.persistence.mapper;

import com.twohands.authservice.domain.session.RefreshTokenSession;
import com.twohands.authservice.infrastructure.persistence.entity.RefreshTokenSessionEntity;
import com.twohands.authservice.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenSessionMapper {

    public RefreshTokenSession toDomain(RefreshTokenSessionEntity entity) {
        if (entity == null) {
            return null;
        }
        RefreshTokenSession domain = new RefreshTokenSession();
        domain.setId(entity.getId());
        if (entity.getUser() != null) {
            domain.setUserId(entity.getUser().getId());
        }
        domain.setTokenHash(entity.getTokenHash());
        domain.setDeviceId(entity.getDeviceId());
        domain.setIpAddress(entity.getIpAddress());
        domain.setUserAgent(entity.getUserAgent());
        domain.setExpiresAt(entity.getExpiresAt());
        domain.setRevoked(entity.isRevoked());
        domain.setStatus(entity.getStatus());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }

    /**
     * Chuyển đổi thông tin Phiên làm việc (Refresh Token Session) từ tầng Domain sang Entity.
     * Dùng để quản lý trạng thái đăng nhập duy trì và bảo mật thiết bị của người dùng.
     */
    public RefreshTokenSessionEntity toEntity(RefreshTokenSession domain) {
        // 1. Kiểm tra null đầu vào để đảm bảo an toàn cho luồng xử lý
        if (domain == null) {
            return null;
        }

        // 2. Khởi tạo thực thể và ánh xạ các thông tin định danh/bảo mật của Token
        RefreshTokenSessionEntity entity = new RefreshTokenSessionEntity();
        entity.setId(domain.getId());
        
        // Lưu bản băm (hash) của token thay vì token gốc để đảm bảo an toàn dữ liệu
        entity.setTokenHash(domain.getTokenHash());
        
        // 3. Lưu thông tin ngữ cảnh thiết bị và mạng để kiểm soát bảo mật
        entity.setDeviceId(domain.getDeviceId());  // ID duy nhất của thiết bị
        entity.setIpAddress(domain.getIpAddress()); // Địa chỉ IP khi khởi tạo session
        entity.setUserAgent(domain.getUserAgent()); // Thông tin trình duyệt/ứng dụng
        
        // 4. Quản lý vòng đời và trạng thái của Session
        entity.setExpiresAt(domain.getExpiresAt()); // Thời điểm token hết hạn
        entity.setRevoked(domain.isRevoked());     // Trạng thái đã bị thu hồi/khóa hay chưa
        entity.setStatus(domain.getStatus());       // Trạng thái cụ thể (ví dụ: ACTIVE, EXPIRED)
        
        // 5. Các mốc thời gian hệ thống
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        // 6. Thiết lập mối quan hệ với User sở hữu phiên làm việc này
        if (domain.getUserId() != null) {
            // Khởi tạo đối tượng User đại diện chỉ với ID để tối ưu hiệu suất (Proxy)
            UserEntity user = new UserEntity();
            user.setId(domain.getUserId());
            
            // Gán User vào Session để xác định chủ sở hữu
            entity.setUser(user);
        }

        // 7. Trả về thực thể để thực hiện các thao tác tiếp theo (thường là lưu vào DB)
        return entity;
    }
}
