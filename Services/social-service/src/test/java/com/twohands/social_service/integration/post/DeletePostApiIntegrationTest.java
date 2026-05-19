package com.twohands.social_service.integration.post;

import com.twohands.social_service.application.post.createpost.CreatePostUseCase;
import com.twohands.social_service.application.post.deletepost.DeletePostResult;
import com.twohands.social_service.application.post.deletepost.DeletePostUseCase;
import com.twohands.social_service.application.post.editpost.EditPostUseCase;
import com.twohands.social_service.application.post.likeunlikepost.LikeUnlikePostUseCase;
import com.twohands.social_service.application.post.saveunsavepost.SaveUnsavePostUseCase;
import com.twohands.social_service.config.SecurityConfig;
import com.twohands.social_service.delivery.http.post.PostController;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PostController.class)
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
class DeletePostApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreatePostUseCase createPostUseCase;

    @MockBean
    private EditPostUseCase editPostUseCase;

    @MockBean
    private DeletePostUseCase deletePostUseCase;

    @MockBean
    private LikeUnlikePostUseCase likeUnlikePostUseCase;

    @MockBean
    private SaveUnsavePostUseCase saveUnsavePostUseCase;

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(delete("/api/v1/social/posts/507f1f77bcf86cd799439011"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldReturn200WhenPostIsDeletedSuccessfully() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = buildAccessToken(userId, List.of("USER"));
        Instant now = Instant.now();

        DeletePostResult result = new DeletePostResult(
                "507f1f77bcf86cd799439011",
                "DELETED",
                now.toString(),
                now.toString()
        );
        when(deletePostUseCase.execute(any())).thenReturn(result);
        when(deletePostUseCase.successMessage()).thenReturn("Xoa bai viet thanh cong.");

        mockMvc.perform(delete("/api/v1/social/posts/507f1f77bcf86cd799439011")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Xoa bai viet thanh cong."))
                .andExpect(jsonPath("$.data.postId").value("507f1f77bcf86cd799439011"))
                .andExpect(jsonPath("$.data.status").value("DELETED"));
    }

    @Test
    void shouldReturn403WhenUserHasNoPermission() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = buildAccessToken(userId, List.of("USER"));

        when(deletePostUseCase.execute(any()))
                .thenThrow(new AppException(ErrorCode.FORBIDDEN, "Ban khong co quyen xoa bai viet nay."));

        mockMvc.perform(delete("/api/v1/social/posts/507f1f77bcf86cd799439011")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void shouldReturn404WhenPostNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = buildAccessToken(userId, List.of("USER"));

        when(deletePostUseCase.execute(any()))
                .thenThrow(new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Bai viet khong ton tai."));

        mockMvc.perform(delete("/api/v1/social/posts/missing-id")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(404));
    }

    private String buildAccessToken(UUID userId, List<String> roles) {
        SecretKey secretKey = Keys.hmacShaKeyFor(
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
                        .getBytes(StandardCharsets.UTF_8)
        );
        return Jwts.builder()
                .subject(userId.toString())
                .claim("roles", roles)
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + 60_000))
                .signWith(secretKey)
                .compact();
    }
}
