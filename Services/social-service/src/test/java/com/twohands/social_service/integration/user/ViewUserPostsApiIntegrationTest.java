package com.twohands.social_service.integration.user;

import com.twohands.social_service.application.user.followuser.FollowUserUseCase;
import com.twohands.social_service.application.user.unfollowuser.UnfollowUserUseCase;
import com.twohands.social_service.application.user.viewfollowersfollowinglist.ViewFollowersFollowingListUseCase;
import com.twohands.social_service.application.user.viewsocialprofile.ViewSocialProfileUseCase;
import com.twohands.social_service.application.user.viewuserposts.ViewUserPostsResult;
import com.twohands.social_service.application.user.viewuserposts.ViewUserPostsUseCase;
import com.twohands.social_service.config.SecurityConfig;
import com.twohands.social_service.delivery.http.user.UserController;
import com.twohands.social_service.delivery.http.user.mapper.ViewUserPostsHttpMapper;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
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
        ViewUserPostsHttpMapper.class,
        GlobalExceptionHandler.class
})
@TestPropertySource(properties = {
        "jwt.access-secret=abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
})
class ViewUserPostsApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FollowUserUseCase followUserUseCase;

    @MockBean
    private UnfollowUserUseCase unfollowUserUseCase;

    @MockBean
    private ViewSocialProfileUseCase viewSocialProfileUseCase;

    @MockBean
    private ViewUserPostsUseCase viewUserPostsUseCase;

    @MockBean
    private ViewFollowersFollowingListUseCase viewFollowersFollowingListUseCase;

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        UUID targetId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/social/users/{userId}/posts", targetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldReturnUserPostsWhenAuthenticated() throws Exception {
        UUID viewerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        String token = buildAccessToken(viewerId);
        String postId = "507f1f77bcf86cd799439011";

        ViewUserPostsResult result = new ViewUserPostsResult(
                List.of(new ViewUserPostsResult.UserPostItem(
                        postId,
                        "Preview caption",
                        List.of(new ViewUserPostsResult.MediaItemData("https://cdn/1.jpg", "IMAGE", null, null)),
                        "PUBLIC",
                        5L,
                        1L,
                        List.of("tag"),
                        "2026-05-21T09:00:00Z"
                )),
                new ViewUserPostsResult.PageResultMeta(0, 20, 1, 1, false)
        );
        when(viewUserPostsUseCase.execute(eq(viewerId), eq(targetId), eq(0), eq(20), eq("published")))
                .thenReturn(result);
        when(viewUserPostsUseCase.successMessage()).thenReturn("Lay danh sach bai viet thanh cong.");

        mockMvc.perform(get("/api/v1/social/users/{userId}/posts", targetId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Lay danh sach bai viet thanh cong."))
                .andExpect(jsonPath("$.data.items[0].postId").value(postId))
                .andExpect(jsonPath("$.data.items[0].likeCount").value(5))
                .andExpect(jsonPath("$.data.meta.totalElements").value(1));
    }

    private String buildAccessToken(UUID userId) {
        SecretKey key = Keys.hmacShaKeyFor(
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
                        .getBytes(StandardCharsets.UTF_8)
        );
        return Jwts.builder()
                .subject(userId.toString())
                .claim("roles", List.of("USER"))
                .signWith(key)
                .compact();
    }
}
