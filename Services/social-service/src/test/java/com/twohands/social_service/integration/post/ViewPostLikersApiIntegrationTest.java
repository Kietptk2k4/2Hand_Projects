package com.twohands.social_service.integration.post;

import com.twohands.social_service.application.comment.commentpost.CommentPostUseCase;
import com.twohands.social_service.application.comment.listpostcomments.ListPostCommentsUseCase;
import com.twohands.social_service.application.post.createpost.CreatePostUseCase;
import com.twohands.social_service.application.post.deletepost.DeletePostUseCase;
import com.twohands.social_service.application.post.editpost.EditPostUseCase;
import com.twohands.social_service.application.post.likeunlikepost.LikeUnlikePostUseCase;
import com.twohands.social_service.application.post.saveunsavepost.SaveUnsavePostUseCase;
import com.twohands.social_service.application.post.viewpostdetail.ViewPostDetailUseCase;
import com.twohands.social_service.application.post.viewpostlikers.ViewPostLikersUseCase;
import com.twohands.social_service.application.post.viewsavedposts.ViewSavedPostsUseCase;
import com.twohands.social_service.application.reaction.common.ViewLikeUsersResult;
import com.twohands.social_service.config.SecurityConfig;
import com.twohands.social_service.delivery.http.comment.mapper.ListPostCommentsHttpMapper;
import com.twohands.social_service.delivery.http.post.PostController;
import com.twohands.social_service.delivery.http.post.mapper.ViewPostDetailHttpMapper;
import com.twohands.social_service.delivery.http.post.mapper.ViewSavedPostsHttpMapper;
import com.twohands.social_service.delivery.http.reaction.mapper.ViewLikeUsersHttpMapper;
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

import static org.mockito.ArgumentMatchers.eq;
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
        GlobalExceptionHandler.class,
        ViewSavedPostsHttpMapper.class,
        ViewPostDetailHttpMapper.class,
        ViewLikeUsersHttpMapper.class,
        ListPostCommentsHttpMapper.class
})
@TestPropertySource(properties = {
        "jwt.access-secret=abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
})
class ViewPostLikersApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private CreatePostUseCase createPostUseCase;
    @MockBean private com.twohands.social_service.application.post.uploadpostmedia.UploadPostMediaUseCase uploadPostMediaUseCase;
    @MockBean private EditPostUseCase editPostUseCase;
    @MockBean private DeletePostUseCase deletePostUseCase;
    @MockBean private LikeUnlikePostUseCase likeUnlikePostUseCase;
    @MockBean private SaveUnsavePostUseCase saveUnsavePostUseCase;
    @MockBean private CommentPostUseCase commentPostUseCase;
    @MockBean private ListPostCommentsUseCase listPostCommentsUseCase;
    @MockBean private ViewSavedPostsUseCase viewSavedPostsUseCase;
    @MockBean private ViewPostDetailUseCase viewPostDetailUseCase;
    @MockBean private ViewPostLikersUseCase viewPostLikersUseCase;

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/social/posts/507f1f77bcf86cd799439011/likes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldReturn200WithPostLikersList() throws Exception {
        UUID viewerId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";
        String token = buildAccessToken(viewerId);

        ViewLikeUsersResult result = new ViewLikeUsersResult(
                List.of(new ViewLikeUsersResult.LikeUserItem(
                        UUID.randomUUID().toString(),
                        "Liker",
                        "https://avatar",
                        "2026-01-01T10:00:00Z"
                )),
                new ViewLikeUsersResult.PageMeta(0, 20, 1, 1, false)
        );
        when(viewPostLikersUseCase.execute(eq(viewerId), eq(postId), eq(0), eq(20))).thenReturn(result);
        when(viewPostLikersUseCase.successMessage()).thenReturn("Lay danh sach nguoi thich bai viet thanh cong.");

        mockMvc.perform(get("/api/v1/social/posts/{postId}/likes", postId)
                        .param("page", "0")
                        .param("size", "20")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].displayName").value("Liker"))
                .andExpect(jsonPath("$.data.meta.totalElements").value(1));
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