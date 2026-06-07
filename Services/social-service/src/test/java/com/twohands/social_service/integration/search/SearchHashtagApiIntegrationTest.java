package com.twohands.social_service.integration.search;

import com.twohands.social_service.application.search.searchhashtag.SearchHashtagResult;
import com.twohands.social_service.application.search.searchhashtag.SearchHashtagUseCase;
import com.twohands.social_service.application.search.searchpost.SearchPostUseCase;
import com.twohands.social_service.application.search.viewtrendinghashtags.ViewTrendingHashtagsCommand;
import com.twohands.social_service.application.search.viewtrendinghashtags.ViewTrendingHashtagsResult;
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
class SearchHashtagApiIntegrationTest {

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
        mockMvc.perform(get("/api/v1/social/search/hashtags/travel"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldReturn200WithHashtagSearchResults() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = buildAccessToken(userId);

        SearchHashtagResult result = SearchHashtagResult.from(
                "travel",
                new com.twohands.social_service.domain.post.PageResult<>(
                        List.of(new SearchHashtagResult.SearchHashtagPostItem(
                                "507f1f77bcf86cd799439011",
                                UUID.randomUUID().toString(),
                                "Trip",
                                List.of(),
                                "PUBLIC",
                                2L,
                                0L,
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
        when(searchHashtagUseCase.execute(any())).thenReturn(result);
        when(searchHashtagUseCase.successMessage()).thenReturn("Tim kiem hashtag thanh cong.");

        mockMvc.perform(get("/api/v1/social/search/hashtags/{hashtag}", "travel")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tim kiem hashtag thanh cong."))
                .andExpect(jsonPath("$.data.hashtag").value("travel"))
                .andExpect(jsonPath("$.data.items[0].caption").value("Trip"));
    }

    @Test
    void shouldReturn200WithTrendingHashtags() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = buildAccessToken(userId);

        when(viewTrendingHashtagsUseCase.execute(any(ViewTrendingHashtagsCommand.class)))
                .thenReturn(new ViewTrendingHashtagsResult(List.of(
                        new ViewTrendingHashtagsResult.TrendingHashtagItem(
                                "LegalTech",
                                3L,
                                20L,
                                5L,
                                25L,
                                55L
                        )
                )));

        mockMvc.perform(get("/api/v1/social/search/trending-hashtags")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].tag").value("LegalTech"))
                .andExpect(jsonPath("$.data.items[0].postCount").value(3))
                .andExpect(jsonPath("$.data.items[0].score").value(55));
    }

    @Test
    void shouldReturn400WhenHashtagIsInvalid() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = buildAccessToken(userId);

        when(searchHashtagUseCase.execute(any()))
                .thenThrow(new AppException(ErrorCode.BAD_REQUEST, "Hashtag khong hop le."));

        mockMvc.perform(get("/api/v1/social/search/hashtags/{hashtag}", " ")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isBadRequest())
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
