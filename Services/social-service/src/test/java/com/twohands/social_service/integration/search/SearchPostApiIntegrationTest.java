package com.twohands.social_service.integration.search;

import com.twohands.social_service.application.search.searchhashtag.SearchHashtagUseCase;
import com.twohands.social_service.application.search.searchpost.SearchPostResult;
import com.twohands.social_service.application.search.searchpost.SearchPostUseCase;
import com.twohands.social_service.application.search.viewtrendinghashtags.ViewTrendingHashtagsUseCase;
import com.twohands.social_service.config.SecurityConfig;
import com.twohands.social_service.delivery.http.search.SearchController;
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

@WebMvcTest(controllers = SearchController.class)
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
class SearchPostApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SearchPostUseCase searchPostUseCase;

    @MockBean
    private SearchHashtagUseCase searchHashtagUseCase;

    @MockBean
    private ViewTrendingHashtagsUseCase viewTrendingHashtagsUseCase;

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/social/search/posts").param("q", "travel"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldReturn200WithSearchResults() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = buildAccessToken(userId);

        SearchPostResult result = SearchPostResult.from(
                "travel",
                new com.twohands.social_service.domain.post.PageResult<>(
                        List.of(new SearchPostResult.SearchPostItem(
                                "507f1f77bcf86cd799439011",
                                UUID.randomUUID().toString(),
                                "My travel post",
                                List.of(),
                                "PUBLIC",
                                5L,
                                1L,
                                List.of("travel"),
                                true,
                                "2026-05-19T10:00:00Z",
                                "2026-05-19T10:00:00Z"
                        )),
                        0,
                        20,
                        1,
                        1,
                        false
                )
        );
        when(searchPostUseCase.execute(any())).thenReturn(result);
        when(searchPostUseCase.successMessage()).thenReturn("Tim kiem bai viet thanh cong.");

        mockMvc.perform(get("/api/v1/social/search/posts")
                        .param("q", "travel")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Tim kiem bai viet thanh cong."))
                .andExpect(jsonPath("$.data.keyword").value("travel"))
                .andExpect(jsonPath("$.data.items[0].caption").value("My travel post"))
                .andExpect(jsonPath("$.data.meta.totalElements").value(1));
    }

    @Test
    void shouldReturn400WhenKeywordIsBlank() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = buildAccessToken(userId);

        when(searchPostUseCase.execute(any()))
                .thenThrow(new AppException(ErrorCode.BAD_REQUEST, "Tu khoa tim kiem khong hop le."));

        mockMvc.perform(get("/api/v1/social/search/posts")
                        .param("q", "   ")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400));
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
