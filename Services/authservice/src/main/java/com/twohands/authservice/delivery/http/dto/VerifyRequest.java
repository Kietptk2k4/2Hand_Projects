package com.twohands.authservice.delivery.http.dto;

import lombok.Data;

@Data
public class VerifyRequest {
    private String email;
    private String otp;
}
