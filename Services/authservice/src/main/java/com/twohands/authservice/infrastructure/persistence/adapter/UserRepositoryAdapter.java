package com.twohands.authservice.infrastructure.persistence.adapter;

import com.twohands.authservice.domain.user.User;
import com.twohands.authservice.domain.user.UserRepository;
import com.twohands.authservice.infrastructure.persistence.entity.RoleEntity;
import com.twohands.authservice.infrastructure.persistence.entity.UserEntity;
import com.twohands.authservice.infrastructure.persistence.mapper.UserMapper;
import com.twohands.authservice.infrastructure.persistence.repository.RoleJpaRepository;
import com.twohands.authservice.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final RoleJpaRepository roleJpaRepository;
    private final UserMapper userMapper;

    public UserRepositoryAdapter(UserJpaRepository userJpaRepository,
                                 RoleJpaRepository roleJpaRepository,
                                 UserMapper userMapper) {
        this.userJpaRepository = userJpaRepository;
        this.roleJpaRepository = roleJpaRepository;
        this.userMapper = userMapper;
    }

    @Override
    public boolean existsByEmailNormalized(String emailNormalized) {
        return userJpaRepository.existsByEmailNormalized(emailNormalized);
    }

    @Override
    public Optional<User> findByEmailNormalized(String emailNormalized) {
        return userJpaRepository.findByEmailNormalized(emailNormalized)
                .map(userMapper::toDomain);
    }

    /**
     * Thực hiện lưu trữ hoặc cập nhật thông tin người dùng vào Database.
     * Đảm bảo đồng bộ hóa các quan hệ (Roles) và chuyển đổi qua lại giữa Domain - Entity.
     */
    @Override
    public User save(User user) {
        // 1. Chuyển đổi đối tượng User từ Domain/DTO sang UserEntity để làm việc với JPA
        UserEntity entity = userMapper.toEntity(user);

        // 2. Kiểm tra nếu danh sách quyền (roles) của user không trống
        if (!user.getRoles().isEmpty()) {
            // Lấy ra danh sách các ID của quyền từ object User
            Set<UUID> roleIds = user.getRoles().stream()
                    .map(r -> r.getId())
                    .collect(Collectors.toSet());
            // Tìm kiếm các RoleEntity tương ứng trong DB bằng các ID đã lấy
            // Việc này giúp đảm bảo các Role này đã tồn tại và được Hibernate quản lý (managed)
            Set<RoleEntity> managedRoles = new HashSet<>(roleJpaRepository.findAllById(roleIds));
            // Gán danh sách quyền đã tìm thấy vào thực thể User
            entity.setRoles(managedRoles);
        }
        // 3. Thực hiện lưu (insert/update) UserEntity vào cơ sở dữ liệu
        UserEntity saved = userJpaRepository.save(entity);
        // 4. Chuyển đổi thực thể đã lưu ngược lại thành đối tượng Domain để trả về cho lớp gọi
        return userMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userJpaRepository.findById(id).map(userMapper::toDomain);
    }
}
