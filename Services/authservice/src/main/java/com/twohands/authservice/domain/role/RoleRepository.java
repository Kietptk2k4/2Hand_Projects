package com.twohands.authservice.domain.role;

import java.util.Optional;

public interface RoleRepository {

    Optional<Role> findByCode(String code);
}
