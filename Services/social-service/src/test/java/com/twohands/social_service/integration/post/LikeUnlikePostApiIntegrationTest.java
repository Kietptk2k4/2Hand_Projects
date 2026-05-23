package com.twohands.social_service.integration.post;

import com.twohands.social_service.application.post.createpost.CreatePostUseCase;
import com.twohands.social_service.application.post.deletepost.DeletePostUseCase;
import com.twohands.social_service.application.post.editpost.EditPostUseCase;
import com.twohands.social_service.application.post.likeunlikepost.LikeUnlikePostResult;
import com.twohands.social_service.application.post.likeunlikepost.LikeUnlikePostUseCase;
import com.twohands.social_service.application.comment.commentpost.CommentPostUseCase;
import com.twohands.social_service.application.post.saveunsavepost.SaveUnsavePostUseCase;
import com.twohands.social_service.application.post.viewsavedposts.ViewSavedPostsUseCase;
import com.twohands.social_service.config.SecurityConfig;
import com.twohands.social_service.delivery.http.post.PostController;
import com.twohands.social_service.delivery.http.post.mapper.ViewSavedPostsHttpMapper;
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

@WebMvcTest(controllers = PostController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtTokenProvider.class,
        RestAuthenticationEntryPoint.class,
        GlobalExceptionHandler.class,
        ViewSavedPostsHttpMapper.class
})
@TestPropertySource(properties = {
        "jwt.access-secret=abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
})
class LikeUnlikePostApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreatePostUseCase createPostUseCase;

    @MockBean
    private com.twohands.social_service.application.post.uploadpostmedia.UploadPostMediaUseCase uploadPostMediaUseCase;

    @MockBean
    private EditPostUseCase editPostUseCase;

    @MockBean
    private DeletePostUseCase deletePostUseCase;

    @MockBean
    private LikeUnlikePostUseCase likeUnlikePostUseCase;

    @MockBean
    private SaveUnsavePostUseCase saveUnsavePostUseCase;

    @MockBean
    private CommentPostUseCase commentPostUseCase;

    @MockBean
    private ViewSavedPostsUseCase viewSavedPostsUseCase;

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/social/posts/507f1f77bcf86cd799439011/like"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldReturn200WhenPostIsLiked() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = buildAccessToken(userId);

        LikeUnlikePostResult result = new LikeUnlikePostResult("507f1f77bcf86cd799439011", true, 10L);
        when(likeUnlikePostUseCase.execute(any())).thenReturn(result);
        when(likeUnlikePostUseCase.successMessage(true)).thenReturn("Like bai viet thanh cong.");

        mockMvc.perform(post("/api/v1/social/posts/507f1f77bcf86cd799439011/like")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Like bai viet thanh cong."))
                .andExpect(jsonPath("$.data.postId").value("507f1f77bcf86cd799439011"))
                .andExpect(jsonPath("$.data.liked").value(true))
                .andExpect(jsonPath("$.data.likeCount").value(10));
    }

    @Test
    void shouldReturn200WhenPostIsUnliked() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = buildAccessToken(userId);

        LikeUnlikePostResult result = new LikeUnlikePostResult("507f1f77bcf86cd799439011", false, 9L);
        when(likeUnlikePostUseCase.execute(any())).thenReturn(result);
        when(likeUnlikePostUseCase.successMessage(false)).thenReturn("Unlike bai viet thanh cong.");

        mockMvc.perform(post("/api/v1/social/posts/507f1f77bcf86cd799439011/like")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Unlike bai viet thanh cong."))
                .andExpect(jsonPath("$.data.liked").value(false));
    }

    @Test
    void shouldReturn404WhenPostNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = buildAccessToken(userId);

        when(likeUnlikePostUseCase.execute(any()))
                .thenThrow(new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Bai viet khong ton tai."));

        mockMvc.perform(post("/api/v1/social/posts/missing/like")
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
