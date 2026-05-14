package com.twohands.auth_service.domain.user;

public final class UserDomainService {

    public void ensureEmailNotExists(UserRepository userRepository, EmailAddress emailAddress) {
        if (userRepository.existsByEmailNormalized(emailAddress.normalizedValue())) {
            throw new UserDomainError("USER_EMAIL_ALREADY_EXISTS", "Email already exists");
        }
    }

    public void ensureCanAuthenticate(User user) {
        user.ensureLoginAllowed();
    }
}
