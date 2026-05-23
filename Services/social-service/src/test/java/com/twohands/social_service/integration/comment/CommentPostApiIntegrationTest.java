package com.twohands.social_service.integration.comment;

import com.twohands.social_service.application.comment.commentpost.CommentPostResult;
import com.twohands.social_service.application.comment.commentpost.CommentPostUseCase;
import com.twohands.social_service.application.post.createpost.CreatePostUseCase;
import com.twohands.social_service.application.post.deletepost.DeletePostUseCase;
import com.twohands.social_service.application.post.editpost.EditPostUseCase;
import com.twohands.social_service.application.post.likeunlikepost.LikeUnlikePostUseCase;
import com.twohands.social_service.application.post.saveunsavepost.SaveUnsavePostUseCase;
import com.twohands.social_service.application.post.viewpostdetail.ViewPostDetailUseCase;
import com.twohands.social_service.application.post.viewsavedposts.ViewSavedPostsUseCase;
import com.twohands.social_service.delivery.http.post.mapper.ViewPostDetailHttpMapper;
import com.twohands.social_service.delivery.http.post.mapper.ViewSavedPostsHttpMapper;
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
class CommentPostApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentPostUseCase commentPostUseCase;

    @MockBean
    private com.twohands.social_service.application.comment.listpostcomments.ListPostCommentsUseCase listPostCommentsUseCase;

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
    private ViewSavedPostsUseCase viewSavedPostsUseCase;

    @MockBean
    private ViewPostDetailUseCase viewPostDetailUseCase;

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/social/posts/507f1f77bcf86cd799439011/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentText\":\"Great post!\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldReturn201WhenCommentIsCreatedSuccessfully() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = buildAccessToken(userId);
        Instant now = Instant.now();

        CommentPostResult result = new CommentPostResult(
                "comment-id",
                "507f1f77bcf86cd799439011",
                null,
                userId.toString(),
                "Great post!",
                List.of(),
                "ACTIVE",
                now.toString(),
                now.toString()
        );
        when(commentPostUseCase.execute(any())).thenReturn(result);
        when(commentPostUseCase.successMessage()).thenReturn("Tao binh luan thanh cong.");

        mockMvc.perform(post("/api/v1/social/posts/507f1f77bcf86cd799439011/comments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentText\":\"Great post!\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.data.commentId").value("comment-id"))
                .andExpect(jsonPath("$.data.parentCommentId").doesNotExist());
    }

    @Test
    void shouldReturn403WhenCommentsDisabled() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = buildAccessToken(userId);

        when(commentPostUseCase.execute(any()))
                .thenThrow(new AppException(ErrorCode.FORBIDDEN, "Bai viet da tat binh luan."));

        mockMvc.perform(post("/api/v1/social/posts/507f1f77bcf86cd799439011/comments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentText\":\"Great post!\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(403));
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
