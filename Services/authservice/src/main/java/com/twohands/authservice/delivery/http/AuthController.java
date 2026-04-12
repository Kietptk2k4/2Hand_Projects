package com.twohands.authservice.delivery.http;

import com.twohands.authservice.application.auth.register.RegisterCommand;
import com.twohands.authservice.application.auth.register.RegisterResult;
import com.twohands.authservice.application.auth.register.RegisterUseCase;
import com.twohands.authservice.application.auth.ratelimit.RateLimitService;
import com.twohands.authservice.delivery.http.dto.RegisterRequest;
import com.twohands.authservice.delivery.http.dto.RegisterResponse;
import com.twohands.authservice.delivery.http.dto.VerifyRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final RateLimitService rateLimitService;

    public AuthController(RegisterUseCase registerUseCase, RateLimitService rateLimitService) {
        this.registerUseCase = registerUseCase;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest req) {
        RegisterCommand cmd = new RegisterCommand(
                req.getEmail(),
                req.getPassword(),
                req.getConfirmPassword()
        );

        RegisterResult result = registerUseCase.execute(cmd);
        RegisterResponse response = new RegisterResponse(
                result.userId(),
                result.status(),
                "Register success, please verify email"
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(
        @RequestBody VerifyRequest req,
        HttpServletRequest request
    ){
        String ip = getClientIp(request);
        rateLimitService.check("verify",ip);
        return ResponseEntity.ok().build();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
