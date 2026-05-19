package com.twohands.social_service.integration.comment;

import com.twohands.social_service.application.comment.deleteowncomment.DeleteOwnCommentUseCase;
import com.twohands.social_service.application.comment.likecomment.LikeCommentResult;
import com.twohands.social_service.application.comment.likecomment.LikeCommentUseCase;
import com.twohands.social_service.application.comment.replycomment.ReplyCommentUseCase;
import com.twohands.social_service.config.SecurityConfig;
import com.twohands.social_service.delivery.http.comment.CommentController;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CommentController.class)
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
class LikeCommentApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReplyCommentUseCase replyCommentUseCase;

    @MockBean
    private DeleteOwnCommentUseCase deleteOwnCommentUseCase;

    @MockBean
    private LikeCommentUseCase likeCommentUseCase;

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/social/comments/comment-id/like"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldReturn200WhenCommentIsLiked() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = buildAccessToken(userId);

        LikeCommentResult result = new LikeCommentResult("comment-id", true, 5L);
        when(likeCommentUseCase.execute(any())).thenReturn(result);
        when(likeCommentUseCase.successMessage(true)).thenReturn("Like comment thanh cong.");

        mockMvc.perform(post("/api/v1/social/comments/comment-id/like")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Like comment thanh cong."))
                .andExpect(jsonPath("$.data.commentId").value("comment-id"))
                .andExpect(jsonPath("$.data.liked").value(true))
                .andExpect(jsonPath("$.data.likeCount").value(5));
    }

    @Test
    void shouldReturn200WhenCommentIsUnliked() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = buildAccessToken(userId);

        LikeCommentResult result = new LikeCommentResult("comment-id", false, 4L);
        when(likeCommentUseCase.execute(any())).thenReturn(result);
        when(likeCommentUseCase.successMessage(false)).thenReturn("Unlike comment thanh cong.");

        mockMvc.perform(post("/api/v1/social/comments/comment-id/like")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Unlike comment thanh cong."))
                .andExpect(jsonPath("$.data.liked").value(false));
    }

    @Test
    void shouldReturn404WhenCommentNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = buildAccessToken(userId);

        when(likeCommentUseCase.execute(any()))
                .thenThrow(new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Comment khong ton tai."));

        mockMvc.perform(post("/api/v1/social/comments/missing/like")
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
