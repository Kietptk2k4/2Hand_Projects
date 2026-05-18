package com.twohands.social_service.integration.feed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.social_service.application.feed.viewfollowingfeed.ViewFollowingFeedUseCase;
import com.twohands.social_service.application.feed.viewglobalfeed.ViewGlobalFeedResult;
import com.twohands.social_service.application.feed.viewglobalfeed.ViewGlobalFeedUseCase;
import com.twohands.social_service.config.SecurityConfig;
import com.twohands.social_service.delivery.http.feed.FeedController;
import com.twohands.social_service.delivery.http.feed.mapper.ViewGlobalFeedHttpMapper;
import com.twohands.social_service.exception.GlobalExceptionHandler;
import com.twohands.social_service.security.RestAuthenticationEntryPoint;
import com.twohands.social_service.security.jwt.JwtAuthenticationFilter;
import com.twohands.social_service.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FeedController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtTokenProvider.class,
        RestAuthenticationEntryPoint.class,
        ViewGlobalFeedHttpMapper.class,
        GlobalExceptionHandler.class
})
@TestPropertySource(properties = {
        "jwt.access-secret=abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
})
class ViewGlobalFeedApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ViewGlobalFeedUseCase viewGlobalFeedUseCase;

    @MockBean
    private ViewFollowingFeedUseCase viewFollowingFeedUseCase;

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/social/feed/global")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldReturnGlobalFeedWhenAuthenticated() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = buildAccessToken(userId);
        ViewGlobalFeedResult result = new ViewGlobalFeedResult(
                List.of(new ViewGlobalFeedResult.FeedPostItem(
                        "507f1f77bcf86cd799439011",
                        UUID.randomUUID().toString(),
                        "hello",
                        List.of(new ViewGlobalFeedResult.MediaItemData("https://cdn/1.jpg", "IMAGE")),
                        "PUBLIC",
                        12,
                        3,
                        List.of("tag1"),
                        true,
                        Instant.parse("2026-05-18T10:15:30Z").toString(),
                        Instant.parse("2026-05-18T10:20:30Z").toString()
                )),
                new ViewGlobalFeedResult.PageResultMeta(0, 20, 1, 1, false)
        );
        when(viewGlobalFeedUseCase.execute(eq(userId), eq(0), eq(20))).thenReturn(result);
        when(viewGlobalFeedUseCase.successMessage()).thenReturn("Lay global feed thanh cong.");

        mockMvc.perform(get("/api/v1/social/feed/global")
                        .param("page", "0")
                        .param("size", "20")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.items[0].visibility").value("PUBLIC"))
                .andExpect(jsonPath("$.data.meta.totalElements").value(1));
    }

    @Test
    void shouldReturnFollowingFeedWhenAuthenticated() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = buildAccessToken(userId);
        ViewGlobalFeedResult result = new ViewGlobalFeedResult(
                List.of(new ViewGlobalFeedResult.FeedPostItem(
                        "507f1f77bcf86cd799439012",
                        UUID.randomUUID().toString(),
                        "following post",
                        List.of(new ViewGlobalFeedResult.MediaItemData("https://cdn/2.jpg", "IMAGE")),
                        "FOLLOWERS",
                        4,
                        1,
                        List.of("social"),
                        true,
                        Instant.parse("2026-05-18T10:16:30Z").toString(),
                        Instant.parse("2026-05-18T10:20:40Z").toString()
                )),
                new ViewGlobalFeedResult.PageResultMeta(0, 20, 1, 1, false)
        );
        when(viewFollowingFeedUseCase.execute(eq(userId), eq(0), eq(20))).thenReturn(result);
        when(viewFollowingFeedUseCase.successMessage()).thenReturn("Lay following feed thanh cong.");

        mockMvc.perform(get("/api/v1/social/feed/following")
                        .param("page", "0")
                        .param("size", "20")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Lay following feed thanh cong."))
                .andExpect(jsonPath("$.data.items[0].visibility").value("FOLLOWERS"));
    }

    private String buildAccessToken(UUID userId) {
        SecretKey secretKey = Keys.hmacShaKeyFor(
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
                        .getBytes(StandardCharsets.UTF_8)
        );
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + 60_000))
                .signWith(secretKey)
                .compact();
    }
}
