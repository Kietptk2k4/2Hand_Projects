package com.twohands.social_service.integration.user;

import com.twohands.social_service.application.user.followuser.FollowUserUseCase;
import com.twohands.social_service.application.user.unfollowuser.UnfollowUserUseCase;
import com.twohands.social_service.application.user.viewfollowersfollowinglist.ViewFollowersFollowingListResult;
import com.twohands.social_service.application.user.viewfollowersfollowinglist.ViewFollowersFollowingListUseCase;
import com.twohands.social_service.application.user.viewsocialprofile.ViewSocialProfileUseCase;
import com.twohands.social_service.config.SecurityConfig;
import com.twohands.social_service.delivery.http.user.UserController;
import com.twohands.social_service.domain.follow.RelationListType;
import com.twohands.social_service.domain.post.PageResult;
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
class ViewFollowersFollowingListApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FollowUserUseCase followUserUseCase;

    @MockBean
    private UnfollowUserUseCase unfollowUserUseCase;

    @MockBean
    private ViewSocialProfileUseCase viewSocialProfileUseCase;

    @MockBean
    private ViewFollowersFollowingListUseCase viewFollowersFollowingListUseCase;

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        UUID targetId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/social/users/{userId}/relations", targetId)
                        .param("type", "followers"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldReturn200WithFollowersList() throws Exception {
        UUID viewerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        String token = buildAccessToken(viewerId);

        ViewFollowersFollowingListResult result = new ViewFollowersFollowingListResult(
                targetId.toString(),
                RelationListType.FOLLOWERS,
                new PageResult<>(
                        List.of(new ViewFollowersFollowingListResult.RelationUserItem(
                                UUID.randomUUID().toString(),
                                "Follower",
                                "https://avatar",
                                Instant.now()
                        )),
                        0,
                        20,
                        1,
                        1,
                        false
                )
        );
        when(viewFollowersFollowingListUseCase.execute(any())).thenReturn(result);
        when(viewFollowersFollowingListUseCase.successMessage()).thenReturn("Lay danh sach quan he thanh cong.");

        mockMvc.perform(get("/api/v1/social/users/{userId}/relations", targetId)
                        .param("type", "followers")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.type").value("followers"))
                .andExpect(jsonPath("$.data.items[0].displayName").value("Follower"))
                .andExpect(jsonPath("$.data.meta.totalElements").value(1));
    }

    @Test
    void shouldReturn400ForInvalidType() throws Exception {
        UUID viewerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        String token = buildAccessToken(viewerId);

        mockMvc.perform(get("/api/v1/social/users/{userId}/relations", targetId)
                        .param("type", "invalid")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldReturn403ForPrivateProfile() throws Exception {
        UUID viewerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        String token = buildAccessToken(viewerId);

        when(viewFollowersFollowingListUseCase.execute(any()))
                .thenThrow(new AppException(ErrorCode.FORBIDDEN, "Khong co quyen xem danh sach quan he cua tai khoan rieng tu."));

        mockMvc.perform(get("/api/v1/social/users/{userId}/relations", targetId)
                        .param("type", "following")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
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
