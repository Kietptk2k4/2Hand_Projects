package com.twohands.auth_service.unit.application.auth.changepassword;

import com.twohands.auth_service.application.auth.changepassword.ChangePasswordCommand;
import com.twohands.auth_service.application.auth.changepassword.ChangePasswordValidationService;
import com.twohands.auth_service.exception.AppException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChangePasswordValidationServiceTest {

    private final ChangePasswordValidationService service = new ChangePasswordValidationService();

    @Test
    void shouldRejectWeakNewPassword() {
        AppException ex = assertThrows(
                AppException.class,
                () -> service.validate(command("OldPassword123", "weakpass", "weakpass"))
        );

        assertEquals("Mat khau moi khong dat yeu cau do manh.", ex.getMessage());
    }

    @Test
    void shouldRejectConfirmPasswordMismatch() {
        AppException ex = assertThrows(
                AppException.class,
                () -> service.validate(command("OldPassword123", "NewPassword123", "NewPassword124"))
        );

        assertEquals("Xac nhan mat khau moi khong khop.", ex.getMessage());
    }

    @Test
    void shouldRejectSameCurrentAndNewPassword() {
        AppException ex = assertThrows(
                AppException.class,
                () -> service.validate(command("SamePassword123", "SamePassword123", "SamePassword123"))
        );

        assertEquals("Mat khau moi phai khac mat khau hien tai.", ex.getMessage());
    }

    private ChangePasswordCommand command(String currentPassword, String newPassword, String confirmNewPassword) {
        return new ChangePasswordCommand(UUID.randomUUID(), currentPassword, newPassword, confirmNewPassword);
    }
}
