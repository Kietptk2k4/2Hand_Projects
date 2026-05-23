package com.twohands.social_service.integration.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.social_service.application.comment.commentpost.CommentPostUseCase;
import com.twohands.social_service.application.post.createpost.CreatePostUseCase;
import com.twohands.social_service.application.post.deletepost.DeletePostUseCase;
import com.twohands.social_service.application.post.editpost.EditPostUseCase;
import com.twohands.social_service.application.post.likeunlikepost.LikeUnlikePostUseCase;
import com.twohands.social_service.application.post.saveunsavepost.SaveUnsavePostUseCase;
import com.twohands.social_service.application.post.uploadpostmedia.UploadPostMediaResult;
import com.twohands.social_service.application.post.uploadpostmedia.UploadPostMediaUseCase;
import com.twohands.social_service.application.post.viewpostdetail.ViewPostDetailUseCase;
import com.twohands.social_service.application.post.viewsavedposts.ViewSavedPostsUseCase;
import com.twohands.social_service.config.SecurityConfig;
import com.twohands.social_service.delivery.http.post.PostController;
import com.twohands.social_service.delivery.http.post.mapper.ViewPostDetailHttpMapper;
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

@WebMvcTest(controllers = PostController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtTokenProvider.class,
        RestAuthenticationEntryPoint.class,
        GlobalExceptionHandler.class,
        ViewSavedPostsHttpMapper.class,
        ViewPostDetailHttpMapper.class
})
@TestPropertySource(properties = {
        "jwt.access-secret=abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
})
class UploadPostMediaApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UploadPostMediaUseCase uploadPostMediaUseCase;

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

    @MockBean
    private CommentPostUseCase commentPostUseCase;

    @MockBean
    private ViewSavedPostsUseCase viewSavedPostsUseCase;

    @MockBean
    private ViewPostDetailUseCase viewPostDetailUseCase;

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/social/posts/media/upload-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content_type":"image/jpeg","file_size_bytes":1024,"media_kind":"IMAGE"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldReturnUploadUrlForAuthenticatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        when(uploadPostMediaUseCase.execute(any())).thenReturn(new UploadPostMediaResult(
                "https://minio.local/presigned",
                "posts/" + userId + "/file.jpg",
                "https://cdn.2hands.vn/social/posts/" + userId + "/file.jpg",
                "IMAGE",
                Instant.parse("2026-05-21T10:15:00Z"),
                10_485_760L,
                List.of("image/jpeg", "image/png", "image/webp", "video/mp4")
        ));
        when(uploadPostMediaUseCase.successMessage()).thenReturn("Tao link upload media thanh cong.");

        mockMvc.perform(post("/api/v1/social/posts/media/upload-url")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content_type":"image/jpeg","file_size_bytes":1048576,"media_kind":"IMAGE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tao link upload media thanh cong."))
                .andExpect(jsonPath("$.data.upload_url").exists())
                .andExpect(jsonPath("$.data.media_url").value("https://cdn.2hands.vn/social/posts/" + userId + "/file.jpg"))
                .andExpect(jsonPath("$.data.media_kind").value("IMAGE"));
    }

    @Test
    void shouldReturn403WhenUserSuspended() throws Exception {
        UUID userId = UUID.randomUUID();
        when(uploadPostMediaUseCase.execute(any()))
                .thenThrow(new AppException(ErrorCode.ACCOUNT_SUSPENDED,
                        ErrorCode.ACCOUNT_SUSPENDED.defaultMessage()));

        mockMvc.perform(post("/api/v1/social/posts/media/upload-url")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content_type":"image/jpeg","file_size_bytes":1024,"media_kind":"IMAGE"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(403));
    }

    private String bearerTokenFor(UUID userId) {
        SecretKey key = Keys.hmacShaKeyFor(
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
                        .getBytes(StandardCharsets.UTF_8)
        );
        String token = Jwts.builder()
                .subject(userId.toString())
                .claim("roles", List.of("USER"))
                .signWith(key)
                .compact();
        return "Bearer " + token;
    }
}
