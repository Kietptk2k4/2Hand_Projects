package com.twohands.auth_service.delivery.http.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VerifyEmailRequest(
        @NotBlank(message = "Ma OTP la bat buoc")
        @Size(min = 6, max = 6, message = "Ma OTP phai gom dung 6 chu so")
        @Pattern(regexp = "^\\d{6}$", message = "Ma OTP phai gom dung 6 chu so")
        String token
) {
}
