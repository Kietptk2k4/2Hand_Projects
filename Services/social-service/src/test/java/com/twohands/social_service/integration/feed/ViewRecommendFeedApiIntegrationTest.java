package com.twohands.social_service.integration.feed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.social_service.application.feed.recommendposts.RecommendPostsUseCase;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
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
class ViewRecommendFeedApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ViewGlobalFeedUseCase viewGlobalFeedUseCase;

    @MockBean
    private ViewFollowingFeedUseCase viewFollowingFeedUseCase;

    @MockBean
    private RecommendPostsUseCase recommendPostsUseCase;

    @MockBean
    private com.twohands.social_service.config.SocialObjectStorageProperties socialObjectStorageProperties;

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/social/feed/for-you")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldReturnRecommendFeedWhenAuthenticated() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = buildAccessToken(userId);
        String productId = UUID.randomUUID().toString();
        
        ViewGlobalFeedResult result = new ViewGlobalFeedResult(
                List.of(new ViewGlobalFeedResult.FeedPostItem(
                        "recPost1",
                        UUID.randomUUID().toString(),
                        "recommend content",
                        List.of(new ViewGlobalFeedResult.MediaItemData("https://cdn/rec1.jpg", "IMAGE", null, null)),
                        "PUBLIC",
                        50,
                        10,
                        false,
                        List.of("recs"),
                        List.of(new com.twohands.social_service.application.post.common.ProductTagSnapshotData(
                                productId, new BigDecimal("250000"), "Sneaker", "https://cdn/s.jpg", "Shoes", true)),
                        true,
                        Instant.parse("2026-05-18T10:15:30Z").toString(),
                        Instant.parse("2026-05-18T10:20:30Z").toString()
                )),
                new ViewGlobalFeedResult.PageResultMeta(0, 20, 1, 1, false)
        );
        
        when(recommendPostsUseCase.execute(eq(userId), eq(0), eq(20))).thenReturn(result);
        when(recommendPostsUseCase.successMessage()).thenReturn("Lay recommend feed thanh cong.");

        mockMvc.perform(get("/api/v1/social/feed/for-you")
                        .param("page", "0")
                        .param("size", "20")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Lay recommend feed thanh cong."))
                .andExpect(jsonPath("$.data.items[0].postId").value("recPost1"))
                .andExpect(jsonPath("$.data.items[0].likeCount").value(50))
                .andExpect(jsonPath("$.data.items[0].productTags[0].name").value("Sneaker"));
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
