package com.twohands.social_service.integration.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.social_service.application.post.createpost.CreatePostResult;
import com.twohands.social_service.application.post.createpost.CreatePostUseCase;
import com.twohands.social_service.application.post.deletepost.DeletePostUseCase;
import com.twohands.social_service.application.post.editpost.EditPostUseCase;
import com.twohands.social_service.application.post.likeunlikepost.LikeUnlikePostUseCase;
import com.twohands.social_service.application.comment.commentpost.CommentPostUseCase;
import com.twohands.social_service.application.post.saveunsavepost.SaveUnsavePostUseCase;
import com.twohands.social_service.application.post.viewsavedposts.ViewSavedPostsUseCase;
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
import java.math.BigDecimal;
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
        ViewSavedPostsHttpMapper.class
})
@TestPropertySource(properties = {
        "jwt.access-secret=abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
})
class CreatePostApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/social/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"visibility\":\"PUBLIC\",\"allowComments\":true,\"publish\":true}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldReturn201WhenPostIsCreatedSuccessfully() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = buildAccessToken(userId);
        Instant now = Instant.now();

        CreatePostResult result = new CreatePostResult(
                "507f1f77bcf86cd799439011",
                userId.toString(),
                "Hello world",
                List.of(new CreatePostResult.MediaItemData("https://cdn/1.jpg", "IMAGE")),
                List.of(),
                "ACTIVE",
                "PUBLIC",
                true,
                List.of("spring"),
                now.toString(),
                now.toString()
        );
        when(createPostUseCase.execute(any())).thenReturn(result);
        when(createPostUseCase.successMessage()).thenReturn("Tao bai viet thanh cong.");

        String body = """
                {
                    "caption": "Hello world",
                    "media": [{"url": "https://cdn/1.jpg", "type": "IMAGE"}],
                    "productTags": [],
                    "visibility": "PUBLIC",
                    "allowComments": true,
                    "hashtags": ["spring"],
                    "publish": true
                }
                """;

        mockMvc.perform(post("/api/v1/social/posts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("Tao bai viet thanh cong."))
                .andExpect(jsonPath("$.data.postId").value("507f1f77bcf86cd799439011"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.visibility").value("PUBLIC"));
    }

    @Test
    void shouldReturn201WithProductTagsInResponse() throws Exception {
        UUID userId = UUID.randomUUID();
        String productId = UUID.randomUUID().toString();
        String token = buildAccessToken(userId);
        Instant now = Instant.now();

        CreatePostResult result = new CreatePostResult(
                "507f1f77bcf86cd799439011",
                userId.toString(),
                "Selling item",
                List.of(),
                List.of(new CreatePostResult.ProductTagData(productId, new BigDecimal("150000"))),
                "ACTIVE",
                "PUBLIC",
                true,
                List.of(),
                now.toString(),
                now.toString()
        );
        when(createPostUseCase.execute(any())).thenReturn(result);
        when(createPostUseCase.successMessage()).thenReturn("Tao bai viet thanh cong.");

        String body = """
                {
                    "caption": "Selling item",
                    "productTags": [
                        { "productId": "%s", "price": 150000 }
                    ],
                    "visibility": "PUBLIC",
                    "allowComments": true,
                    "publish": true
                }
                """.formatted(productId);

        mockMvc.perform(post("/api/v1/social/posts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.productTags[0].productId").value(productId))
                .andExpect(jsonPath("$.data.productTags[0].price").value(150000));
    }

    @Test
    void shouldReturn400WhenVisibilityIsMissing() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = buildAccessToken(userId);

        String body = """
                {
                    "caption": "Missing visibility",
                    "allowComments": true,
                    "publish": true
                }
                """;

        mockMvc.perform(post("/api/v1/social/posts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldReturn403WhenUserIsSuspended() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = buildAccessToken(userId);

        when(createPostUseCase.execute(any()))
                .thenThrow(new AppException(ErrorCode.ACCOUNT_SUSPENDED,
                        ErrorCode.ACCOUNT_SUSPENDED.defaultMessage()));

        String body = """
                {
                    "caption": "Suspended user",
                    "visibility": "PUBLIC",
                    "allowComments": true,
                    "publish": true
                }
                """;

        mockMvc.perform(post("/api/v1/social/posts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(403));
    }

    private String buildAccessToken(UUID userId) {
        SecretKey secretKey = Keys.hmacShaKeyFor(
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
                        .getBytes(StandardCharsets.UTF_8)
        );
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + 60_000))
                .signWith(secretKey)
                .compact();
    }
}
