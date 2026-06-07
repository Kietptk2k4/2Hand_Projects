package com.twohands.social_service.integration.post;

import com.twohands.social_service.application.comment.commentpost.CommentPostUseCase;
import com.twohands.social_service.application.comment.listpostcomments.ListPostCommentsResult;
import com.twohands.social_service.application.comment.listpostcomments.ListPostCommentsUseCase;
import com.twohands.social_service.application.post.createpost.CreatePostUseCase;
import com.twohands.social_service.application.post.deletepost.DeletePostUseCase;
import com.twohands.social_service.application.post.editpost.EditPostUseCase;
import com.twohands.social_service.application.post.likeunlikepost.LikeUnlikePostUseCase;
import com.twohands.social_service.application.post.saveunsavepost.SaveUnsavePostUseCase;
import com.twohands.social_service.application.post.viewpostdetail.ViewPostDetailUseCase;
import com.twohands.social_service.application.post.viewsavedposts.ViewSavedPostsUseCase;
import com.twohands.social_service.config.SecurityConfig;
import com.twohands.social_service.delivery.http.comment.mapper.ListPostCommentsHttpMapper;
import com.twohands.social_service.delivery.http.post.PostController;
import com.twohands.social_service.delivery.http.post.mapper.ViewPostDetailHttpMapper;
import com.twohands.social_service.delivery.http.post.mapper.ViewSavedPostsHttpMapper;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PostController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtTokenProvider.class,
        RestAuthenticationEntryPoint.class,
        ViewSavedPostsHttpMapper.class,
        ViewPostDetailHttpMapper.class,
        ListPostCommentsHttpMapper.class,
        GlobalExceptionHandler.class
})
@TestPropertySource(properties = {
        "jwt.access-secret=abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
})
class ListPostCommentsApiIntegrationTest {

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
    private ListPostCommentsUseCase listPostCommentsUseCase;

    @MockBean
    private ViewSavedPostsUseCase viewSavedPostsUseCase;

    @MockBean
    private ViewPostDetailUseCase viewPostDetailUseCase;

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/social/posts/507f1f77bcf86cd799439011/comments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldReturnCommentsWhenAuthenticated() throws Exception {
        UUID viewerId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";
        String commentId = "507f1f77bcf86cd799439012";
        String token = buildAccessToken(viewerId);

        ListPostCommentsResult result = new ListPostCommentsResult(
                List.of(new ListPostCommentsResult.CommentItem(
                        commentId,
                        postId,
                        null,
                        new ListPostCommentsResult.AuthorSummary(
                                authorId.toString(),
                                "User A",
                                "https://cdn/avatar.png"
                        ),
                        "Hay qua!",
                        List.of(),
                        3L,
                        true,
                        1L,
                        "2026-05-21T10:00:00Z",
                        "2026-05-21T10:00:00Z"
                )),
                new ListPostCommentsResult.PageResultMeta(0, 20, 1, 1, false)
        );
        when(listPostCommentsUseCase.execute(
                eq(viewerId),
                eq(postId),
                eq(0),
                eq(20),
                isNull(),
                eq("created_at_asc")
        )).thenReturn(result);
        when(listPostCommentsUseCase.successMessage()).thenReturn("Lay danh sach binh luan thanh cong.");

        mockMvc.perform(get("/api/v1/social/posts/{postId}/comments", postId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Lay danh sach binh luan thanh cong."))
                .andExpect(jsonPath("$.data.items[0].commentId").value(commentId))
                .andExpect(jsonPath("$.data.items[0].author.displayName").value("User A"))
                .andExpect(jsonPath("$.data.items[0].likedByMe").value(true))
                .andExpect(jsonPath("$.data.items[0].replyCount").value(1))
                .andExpect(jsonPath("$.data.meta.totalElements").value(1));
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
