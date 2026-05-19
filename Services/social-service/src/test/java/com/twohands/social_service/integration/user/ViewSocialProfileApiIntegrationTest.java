package com.twohands.social_service.integration.user;

import com.twohands.social_service.application.user.followuser.FollowUserUseCase;
import com.twohands.social_service.application.user.unfollowuser.UnfollowUserUseCase;
import com.twohands.social_service.application.user.viewsocialprofile.ViewSocialProfileResult;
import com.twohands.social_service.application.user.viewsocialprofile.ViewSocialProfileUseCase;
import com.twohands.social_service.config.SecurityConfig;
import com.twohands.social_service.delivery.http.user.UserController;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class ViewSocialProfileApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FollowUserUseCase followUserUseCase;

    @MockBean
    private UnfollowUserUseCase unfollowUserUseCase;

    @MockBean
    private ViewSocialProfileUseCase viewSocialProfileUseCase;

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        UUID targetId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/social/users/{userId}/profile", targetId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldReturn200WithSocialProfile() throws Exception {
        UUID viewerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        String token = buildAccessToken(viewerId);

        ViewSocialProfileResult result = new ViewSocialProfileResult(
                targetId.toString(),
                "User B",
                "https://avatar",
                false,
                10L,
                5L,
                "NONE",
                true
        );
        when(viewSocialProfileUseCase.execute(any())).thenReturn(result);
        when(viewSocialProfileUseCase.successMessage()).thenReturn("Lay social profile thanh cong.");

        mockMvc.perform(get("/api/v1/social/users/{userId}/profile", targetId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Lay social profile thanh cong."))
                .andExpect(jsonPath("$.data.userId").value(targetId.toString()))
                .andExpect(jsonPath("$.data.followerCount").value(10))
                .andExpect(jsonPath("$.data.followingCount").value(5))
                .andExpect(jsonPath("$.data.canViewFullProfile").value(true));
    }

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {
        UUID viewerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        String token = buildAccessToken(viewerId);

        when(viewSocialProfileUseCase.execute(any()))
                .thenThrow(new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Nguoi dung khong ton tai."));

        mockMvc.perform(get("/api/v1/social/users/{userId}/profile", targetId)
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
