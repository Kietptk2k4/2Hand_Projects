package com.twohands.authservice.application.auth.security;

import com.twohands.authservice.delivery.http.exception.UnauthorizedException;
import com.twohands.authservice.domain.user.User;
import com.twohands.authservice.domain.user.UserStatus;
import org.springframework.stereotype.Service;

@Service
public class AccountStatusGuard {

    // /**
    //  * Kiểm tra xem người dùng có đủ điều kiện để đăng nhập hay không (Guard Clause).
    //  * Hàm này hoạt động theo cơ chế "Fail-fast": Nếu trạng thái không hợp lệ, 
    //  * nó sẽ ném ra ngoại lệ ngay lập tức để chặn đứng luồng đăng nhập.
    //  *
    //  * @param user Đối tượng người dùng đang thực hiện đăng nhập.
    //  * @throws UnauthorizedException Nếu tài khoản ở trạng thái không được phép truy cập.
    //  */
    public void assertCanLogin(User user) {
        
        // Sử dụng Enhanced Switch (từ Java 14+) giúp code ngắn gọn và an toàn hơn,
        // không cần dùng từ khóa 'break' nên tránh được lỗi trôi lệnh (fall-through).
        switch (user.getStatus()) {
            
            // 1. Nhóm HỢP LỆ: Cho phép đăng nhập
            // (INACTIVE vẫn được đăng nhập có thể là để thực hiện luồng kích hoạt lại tài khoản)
            case ACTIVE, INACTIVE -> {
                return; // Thoát hàm êm đẹp, luồng đăng nhập được tiếp tục
            }
            
            // 2. Nhóm CẦN HÀNH ĐỘNG: Trả về thông báo cụ thể để hướng dẫn người dùng
            case PENDING_VERIFICATION -> throw new UnauthorizedException("Account is not verified. Please check your email.");
            case LOCKED -> throw new UnauthorizedException("Account is locked. Please contact support.");
            
            // 3. Nhóm HẠN CHẾ: Gom chung các trạng thái phạt/xem xét để trả về một lỗi chung,
            // tránh làm lộ quá nhiều quy trình xử lý nội bộ của hệ thống.
            case SUSPENDED, TEMP_BANNED, PERMANENT_BANNED, UNDER_REVIEW, RESTRICTED ->
                    throw new UnauthorizedException("Account access is currently restricted.");
            
            // 4. Nhóm ĐÃ XÓA: Che giấu sự tồn tại của tài khoản (Security Best Practice)
            // Thay vì báo "Tài khoản đã xóa", hệ thống báo "Sai thông tin". 
            // Việc này giúp ngăn chặn tấn công dò quét tài khoản (User Enumeration).
            case DELETED, HARD_DELETED -> throw new UnauthorizedException("Invalid credentials");
            
            // 5. MẶC ĐỊNH: Fallback an toàn phòng trường hợp sau này team bổ sung
            // thêm enum status mới nhưng quên cập nhật code ở đây.
            default -> throw new UnauthorizedException("Account is not allowed to log in");
        }
    }

    public void assertCanRefresh(User user) {
        switch (user.getStatus()) {
            case ACTIVE, INACTIVE -> {
                return;
            }
            case LOCKED -> throw new UnauthorizedException("Account is locked.");
            case SUSPENDED, TEMP_BANNED, PERMANENT_BANNED, UNDER_REVIEW, RESTRICTED ->
                    throw new UnauthorizedException("Account access is currently restricted.");
            case PENDING_VERIFICATION -> throw new UnauthorizedException("Account is not verified.");
            case DELETED, HARD_DELETED -> throw new UnauthorizedException("Invalid credentials");
            default -> throw new UnauthorizedException("Refresh is not allowed");
        }
    }

    public boolean blocksPrincipal(UserStatus status) {
        return status != UserStatus.ACTIVE;
    }
}
