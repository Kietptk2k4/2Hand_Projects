package com.twohands.authservice.infrastructure.persistence.mapper;

import com.twohands.authservice.domain.role.Role;
import com.twohands.authservice.infrastructure.persistence.entity.PermissionEntity;
import com.twohands.authservice.infrastructure.persistence.entity.RoleEntity;
import com.twohands.authservice.infrastructure.persistence.entity.RolePermissionEntity;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class RoleMapper {

    private final PermissionMapper permissionMapper;

    public RoleMapper(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    /**
     * Chuyển đổi từ thực thể RoleEntity sang đối tượng nghiệp vụ Role.
     * Giải nén các mối quan hệ phức tạp từ database để đưa về cấu trúc Domain đơn giản hơn.
     */
    public Role toDomain(RoleEntity entity) {
        // 1. Kiểm tra an toàn: Nếu entity rỗng thì không xử lý
        if (entity == null) {
            return null;
        }

        // 2. Khởi tạo đối tượng Role và đổ các dữ liệu định danh từ Database vào
        Role role = new Role();
        role.setId(entity.getId());
        role.setCode(entity.getCode()); // Mã vai trò (ví dụ: "ADMIN_ROOT")
        role.setName(entity.getName()); // Tên hiển thị (ví dụ: "Quản trị viên hệ thống")
        role.setCreatedAt(entity.getCreatedAt());
        role.setUpdatedAt(entity.getUpdatedAt());

        // 3. Xử lý logic "Phẳng hóa" quyền hạn (Flattening Permissions):
        // Trong DB, Role liên kết với Permission qua bảng trung gian RolePermissionEntity.
        // Tại đây, chúng ta lược bỏ lớp trung gian đó để lấy trực tiếp danh sách Permission.
        role.setPermissions(
                entity.getRolePermissions()
                        .stream() // Bật băng chuyền duyệt qua các bản ghi ở bảng trung gian
                        
                        // Với mỗi bản ghi trung gian (rp), ta chỉ lấy phần "Permission" bên trong 
                        // và dùng permissionMapper để chuyển nó về dạng Domain.
                        .map(rp -> permissionMapper.toDomain(rp.getPermission()))
                        
                        // Gom tất cả các quyền tìm được vào một tập hợp Set (để loại bỏ trùng lặp)
                        .collect(Collectors.toSet())
        );

        // 4. Trả về đối tượng Role đã có đầy đủ danh sách quyền hạn đi kèm
        return role;
    }

    /**
     * Chuyển đổi đối tượng Role từ tầng Domain sang RoleEntity.
     * Đặc biệt xử lý việc ánh xạ các quyền hạn (Permissions) thông qua bảng trung gian RolePermission.
     */
    public RoleEntity toEntity(Role domain) {
        // 1. Kiểm tra null để tránh lỗi hệ thống
        if (domain == null) {
            return null;
        }

        // 2. Khởi tạo thực thể RoleEntity và sao chép các thuộc tính cơ bản
        RoleEntity entity = new RoleEntity();
        entity.setId(domain.getId());
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        // 3. Xử lý danh sách quyền hạn (Permissions) đi kèm
        if (domain.getPermissions() != null && !domain.getPermissions().isEmpty()) {
            // Khởi tạo Set chứa các thực thể trung gian (RolePermissionEntity)
            entity.setRolePermissions(new HashSet<>());

            // Duyệt qua từng Permission trong Domain để tạo các bản ghi liên kết
            domain.getPermissions().forEach(permission -> {
                // Khởi tạo thực thể trung gian để nối Role và Permission
                RolePermissionEntity rolePermission = new RolePermissionEntity();

                // Gán Role hiện tại cho thực thể trung gian (Liên kết ngược)
                rolePermission.setRole(entity);

                // Tạo nhanh thực thể PermissionEntity từ ID để thiết lập mối quan hệ
                PermissionEntity permissionEntity = new PermissionEntity();
                permissionEntity.setId(permission.getId());
                
                // Gán Permission vào thực thể trung gian
                rolePermission.setPermission(permissionEntity);

                // Thêm bản ghi trung gian vừa tạo vào danh sách của RoleEntity
                entity.getRolePermissions().add(rolePermission);
            });
        }

        // 4. Trả về thực thể hoàn thiện để sẵn sàng lưu xuống Database
        return entity;
    }
}
