package com.twohands.social_service.integration.user;

import com.twohands.social_service.application.user.followuser.FollowUserResult;
import com.twohands.social_service.application.user.followuser.FollowUserUseCase;
import com.twohands.social_service.application.user.unfollowuser.UnfollowUserUseCase;
import com.twohands.social_service.config.SecurityConfig;
import com.twohands.social_service.delivery.http.user.UserController;
import com.twohands.social_service.domain.follow.FollowStatus;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import com.twohands.social_service.exception.GlobalExceptionHandler;
import com.twohands.social_service.security.RestAuthenticationEntryPoint;
import com.twohands.social_service.security.jwt.JwtAuthenticationFilter;
import com.twohands.social_service.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtTokenProvider.class,
        RestAuthenticationEntryPoint.class,
        GlobalExceptionHandler.class
})
@TestPropertySource(properties = {
        "jwt.access-secret=abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
})
class FollowUserApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FollowUserUseCase followUserUseCase;

    @MockBean
    private UnfollowUserUseCase unfollowUserUseCase;

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        UUID followeeId = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/social/users/{userId}/follow", followeeId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldReturn201WhenFollowIsCreated() throws Exception {
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();
        String token = buildAccessToken(followerId);
        Instant now = Instant.now();

        FollowUserResult result = new FollowUserResult(followeeId, FollowStatus.ACCEPTED, now, true);
        when(followUserUseCase.execute(any())).thenReturn(result);
        when(followUserUseCase.successMessage()).thenReturn("Theo doi nguoi dung thanh cong.");

        mockMvc.perform(post("/api/v1/social/users/{userId}/follow", followeeId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("Theo doi nguoi dung thanh cong."))
                .andExpect(jsonPath("$.data.followeeId").value(followeeId.toString()))
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
    }

    @Test
    void shouldReturn200WhenFollowAlreadyExists() throws Exception {
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();
        String token = buildAccessToken(followerId);

        FollowUserResult result = new FollowUserResult(followeeId, FollowStatus.ACCEPTED, Instant.now(), false);
        when(followUserUseCase.execute(any())).thenReturn(result);
        when(followUserUseCase.successMessage()).thenReturn("Theo doi nguoi dung thanh cong.");

        mockMvc.perform(post("/api/v1/social/users/{userId}/follow", followeeId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldReturn400WhenSelfFollow() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = buildAccessToken(userId);

        when(followUserUseCase.execute(any()))
                .thenThrow(new AppException(ErrorCode.BAD_REQUEST, "Khong the tu theo doi chinh minh."));

        mockMvc.perform(post("/api/v1/social/users/{userId}/follow", userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldReturn404WhenFolloweeNotFound() throws Exception {
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();
        String token = buildAccessToken(followerId);

        when(followUserUseCase.execute(any()))
                .thenThrow(new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Nguoi dung khong ton tai."));

        mockMvc.perform(post("/api/v1/social/users/{userId}/follow", followeeId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(404));
    }

    private String buildAccessToken(UUID userId) {
        SecretKey secretKey = Keys.hmacShaKeyFor(
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
                        .getBytes(StandardCharsets.UTF_8)
        );
        return Jwts.builder()
                .subject(userId.toString())
                .claim("roles", List.of("USER"))
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + 60_000))
                .signWith(secretKey)
                .compact();
    }
}
