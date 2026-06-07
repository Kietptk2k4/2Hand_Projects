package com.twohands.social_service.integration.comment;

import com.twohands.social_service.application.comment.deleteowncomment.DeleteOwnCommentUseCase;
import com.twohands.social_service.application.comment.likecomment.LikeCommentUseCase;
import com.twohands.social_service.application.comment.common.CommentAuthorSummary;
import com.twohands.social_service.application.comment.replycomment.ReplyCommentResult;
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
import org.springframework.http.MediaType;
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
class ReplyCommentApiIntegrationTest {

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
        mockMvc.perform(post("/api/v1/social/comments/parent-id/replies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentText\":\"Thanks!\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldReturn201WhenReplyIsCreatedSuccessfully() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = buildAccessToken(userId);
        Instant now = Instant.now();

        ReplyCommentResult result = new ReplyCommentResult(
                "reply-id",
                "507f1f77bcf86cd799439011",
                "parent-id",
                userId.toString(),
                new CommentAuthorSummary(userId.toString(), "Test User", "https://cdn/avatar.jpg"),
                "Thanks!",
                List.of(),
                "ACTIVE",
                now.toString(),
                now.toString()
        );
        when(replyCommentUseCase.execute(any())).thenReturn(result);
        when(replyCommentUseCase.successMessage()).thenReturn("Tra loi comment thanh cong.");

        mockMvc.perform(post("/api/v1/social/comments/parent-id/replies")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentText\":\"Thanks!\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("Tra loi comment thanh cong."))
                .andExpect(jsonPath("$.data.commentId").value("reply-id"))
                .andExpect(jsonPath("$.data.parentCommentId").value("parent-id"))
                .andExpect(jsonPath("$.data.author.displayName").value("Test User"))
                .andExpect(jsonPath("$.data.author.avatarUrl").value("https://cdn/avatar.jpg"));
    }

    @Test
    void shouldReturn404WhenParentCommentNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = buildAccessToken(userId);

        when(replyCommentUseCase.execute(any()))
                .thenThrow(new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Comment cha khong ton tai."));

        mockMvc.perform(post("/api/v1/social/comments/missing/replies")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentText\":\"Thanks!\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void shouldReturn400WhenContentTextIsMissing() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = buildAccessToken(userId);

        mockMvc.perform(post("/api/v1/social/comments/parent-id/replies")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
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
