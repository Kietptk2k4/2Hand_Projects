package com.twohands.authservice.infrastructure.persistence.mapper;

import com.twohands.authservice.domain.oauth.OAuthAccount;
import com.twohands.authservice.infrastructure.persistence.entity.OAuthAccountEntity;
import com.twohands.authservice.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class OAuthAccountMapper {

    /**
     * Chuyển đổi từ thực thể OAuthAccountEntity (Database) sang đối tượng OAuthAccount (Domain).
     * Mục đích: Lấy thông tin tài khoản liên kết để thực hiện các logic đăng nhập bằng bên thứ ba.
     */
    public OAuthAccount toDomain(OAuthAccountEntity entity) {
        // 1. Kiểm tra null: Trả về null nếu không tìm thấy bản ghi trong cơ sở dữ liệu
        if (entity == null) {
            return null;
        }

        // 2. Khởi tạo đối tượng Domain để chứa dữ liệu phục vụ xử lý nghiệp vụ
        OAuthAccount domain = new OAuthAccount();
        domain.setId(entity.getId());

        // 3. Trích xuất ID người dùng (Flattening User ID):
        // Chuyển từ quan hệ Object (UserEntity) sang dạng ID đơn giản (UUID/Long).
        // Giúp tầng Domain biết tài khoản mạng xã hội này thuộc về ai mà không cần tải toàn bộ dữ liệu User.
        if (entity.getUser() != null) {
            domain.setUserId(entity.getUser().getId());
        }

        // 4. Khôi phục thông tin từ nhà cung cấp dịch vụ (Provider)
        domain.setProvider(entity.getProvider());           // Ví dụ: GOOGLE, FACEBOOK, GITHUB
        domain.setProviderUserId(entity.getProviderUserId()); // ID định danh duy nhất từ phía nhà cung cấp
        domain.setEmail(entity.getEmail());                 // Email đăng ký của tài khoản bên thứ ba
        
        // 5. Khôi phục trạng thái hoạt động và các mốc thời gian
        domain.setStatus(entity.getStatus());               // Trạng thái: ACTIVE, LINKED, UNLINKED...
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());

        // 6. Trả về đối tượng Domain đã được làm gọn (chỉ chứa ID của User)
        return domain;
    }
    
    /**
     * Chuyển đổi thông tin tài khoản OAuth (Google, Facebook, v.v.) từ Domain sang Entity.
     */
    public OAuthAccountEntity toEntity(OAuthAccount domain) {
        // 1. Kiểm tra nếu dữ liệu đầu vào trống thì thoát sớm
        if (domain == null) {
            return null;
        }
        // 2. Khởi tạo thực thể và sao chép các thông tin định danh của tài khoản OAuth
        OAuthAccountEntity entity = new OAuthAccountEntity();
        entity.setId(domain.getId());
        entity.setProvider(domain.getProvider());           // Ví dụ: "GOOGLE", "FACEBOOK"
        entity.setProviderUserId(domain.getProviderUserId()); // ID định danh của User bên phía Provider
        entity.setEmail(domain.getEmail());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        // 3. Thiết lập mối quan hệ với thực thể User (Chủ sở hữu tài khoản)
        if (domain.getUserId() != null) {
            // Tạo một đối tượng UserEntity rỗng để đại diện cho khóa ngoại
            UserEntity user = new UserEntity();
            // Chỉ cần set ID để Hibernate hiểu đây là "người dùng nào" 
            // mà không cần phải tốn công truy vấn toàn bộ thông tin User từ DB lên.
            user.setId(domain.getUserId());
            // Gán đối tượng user vừa tạo làm chủ sở hữu của tài khoản OAuth này
            entity.setUser(user);
        }
        return entity;
    
    
    }

}
