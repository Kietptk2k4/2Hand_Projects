package com.twohands.authservice.infrastructure.persistence.mapper;

import com.twohands.authservice.domain.login.LoginLog;
import com.twohands.authservice.infrastructure.persistence.entity.LoginLogEntity;
import com.twohands.authservice.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class LoginLogMapper {

    /**
     * Chuyển đổi từ thực thể Database (LoginLogEntity) sang đối tượng nghiệp vụ (LoginLog).
     * Dùng để đọc dữ liệu lịch sử đăng nhập nhằm phục vụ việc kiểm tra bảo mật hoặc hiển thị cho người dùng.
     */
    public LoginLog toDomain(LoginLogEntity entity) {
        // 1. Kiểm tra an toàn: Tránh lỗi NullPointerException nếu bản ghi log không tồn tại
        if (entity == null) {
            return null;
        }

        // 2. Khởi tạo đối tượng Domain để chứa dữ liệu trả về cho lớp nghiệp vụ
        LoginLog domain = new LoginLog();
        domain.setId(entity.getId());

        // 3. Logic xử lý "Phẳng hóa ID người dùng" (Flattening User ID):
        // Trong Database, log liên kết với một Object UserEntity (nhiều dữ liệu).
        // Khi đưa ra tầng Domain, ta chỉ cần trích xuất lấy cái ID của người dùng để gọn nhẹ.
        if (entity.getUser() != null) {
            domain.setUserId(entity.getUser().getId());
        }

        // 4. Khôi phục các thông tin kỹ thuật về phiên đăng nhập
        domain.setLoginMethod(entity.getLoginMethod()); // Phương thức: PASSWORD, SOCIAL_LOGIN...
        domain.setIpAddress(entity.getIpAddress());     // Địa chỉ IP của thiết bị thực hiện
        domain.setUserAgent(entity.getUserAgent());     // Thông tin phần mềm/trình duyệt đăng nhập
        
        // 5. Khôi phục trạng thái và kết quả đăng nhập
        domain.setSuccess(entity.isSuccess());           // Thành công hay thất bại
        domain.setFailureReason(entity.getFailureReason()); // Nguyên nhân nếu đăng nhập hụt
        
        // 6. Các mốc thời gian ghi nhận bản ghi
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());

        // 7. Trả về đối tượng Domain đã được làm sạch và tối giản hóa
        return domain;
    }

    /**
     * Chuyển đổi dữ liệu Lịch sử đăng nhập từ Domain sang Entity để lưu trữ.
     * Ghi lại các thông tin chi tiết về phiên đăng nhập như IP, thiết bị, và kết quả.
     */
    public LoginLogEntity toEntity(LoginLog domain) {
        if (domain == null) {
            return null;
        }

        // 2. Khởi tạo thực thể LoginLogEntity và ánh xạ các thông tin kỹ thuật
        LoginLogEntity entity = new LoginLogEntity();
        entity.setId(domain.getId());
        entity.setLoginMethod(domain.getLoginMethod()); // Ví dụ: "PASSWORD", "OAUTH2"
        entity.setIpAddress(domain.getIpAddress());     // Địa chỉ IP của người đăng nhập
        entity.setUserAgent(domain.getUserAgent());     // Thông tin trình duyệt và hệ điều hành
        
        // 3. Trạng thái và lý do (nếu có) khi đăng nhập
        entity.setSuccess(domain.isSuccess());           // Đăng nhập thành công hay thất bại
        entity.setFailureReason(domain.getFailureReason()); // Lý do thất bại (ví dụ: Sai mật khẩu)
        
        // 4. Các mốc thời gian hệ thống
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        // 5. Liên kết bản ghi Log với người dùng cụ thể
        if (domain.getUserId() != null) {
            // Tạo đối tượng UserEntity đại diện (Proxy) chỉ với ID
            UserEntity user = new UserEntity();
            
            // Thiết lập ID người dùng để Hibernate ánh xạ vào cột Foreign Key (user_id)
            user.setId(domain.getUserId());
            
            // Gán người dùng vào bản ghi log này
            entity.setUser(user);
        }

        return entity;
    }

    
}
