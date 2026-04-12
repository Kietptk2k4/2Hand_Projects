package com.twohands.authservice.application.auth.register;

import com.twohands.authservice.application.auth.port.AuthEventPublisher;
import com.twohands.authservice.application.auth.port.OtpGenerator;
import com.twohands.authservice.application.auth.port.OtpStore;
import com.twohands.authservice.application.auth.port.PasswordHasher;
import com.twohands.authservice.domain.role.Role;
import com.twohands.authservice.domain.role.RoleRepository;
import com.twohands.authservice.domain.user.User;
import com.twohands.authservice.domain.user.UserRepository;
import com.twohands.authservice.domain.user.UserStatus;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class RegisterUseCase {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordHasher passwordHasher;
    private final OtpGenerator otpGenerator;
    private final OtpStore otpStore;
    private final AuthEventPublisher authEventPublisher;

    public RegisterUseCase(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordHasher passwordHasher,
            OtpGenerator otpGenerator,
            OtpStore otpStore,
            AuthEventPublisher authEventPublisher
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordHasher = passwordHasher;
        this.otpGenerator = otpGenerator;
        this.otpStore = otpStore;
        this.authEventPublisher = authEventPublisher;
    }

    public RegisterResult execute(RegisterCommand cmd) {
        validate(cmd);

        String emailNorm = cmd.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailNormalized(emailNorm)) {
            throw new IllegalArgumentException("Email already exists");
        }

        String hash = passwordHasher.hash(cmd.password());

        User user = new User();
        user.setEmail(cmd.email());
        user.setEmailNormalized(emailNorm);
        user.setPasswordHash(hash);
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);

        user = userRepository.save(user);

        Role role = roleRepository.findByCode("USER")
                .orElseThrow(() -> new IllegalStateException("Role USER not found"));

        user.getRoles().add(role);
        user = userRepository.save(user);

        String otp = otpGenerator.generate();
        String key = "auth:otp:register:" + emailNorm;
        otpStore.save(key, otp, 300);
        authEventPublisher.publishVerification(user.getEmail(), otp);

        return new RegisterResult(user.getId(), user.getStatus().name());
    }

    private void validate(RegisterCommand cmd) {
        if (cmd.email() == null || cmd.email().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (cmd.password() == null || cmd.password().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (!cmd.password().equals(cmd.confirmPassword())) {
            throw new IllegalArgumentException("Password not match");
        }
    }
}
