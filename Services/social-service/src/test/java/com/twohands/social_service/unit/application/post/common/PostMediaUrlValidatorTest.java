package com.twohands.social_service.unit.application.post.common;

import com.twohands.social_service.application.post.common.PostMediaUrlValidator;
import com.twohands.social_service.config.SocialObjectStorageProperties;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostMediaUrlValidatorTest {

    @Test
    void shouldAcceptUrlWithAllowedPrefixWhenStorageEnabled() {
        SocialObjectStorageProperties properties = new SocialObjectStorageProperties();
        properties.setEnabled(true);
        properties.setPublicUrl("https://cdn.2hands.vn");
        properties.setPublicPathPrefix("social");
        PostMediaUrlValidator validator = new PostMediaUrlValidator(properties);
        UUID userId = UUID.randomUUID();

        assertThatCode(() -> validator.validateMediaUrls(
                userId,
                List.of("https://cdn.2hands.vn/social/posts/" + userId + "/abc.jpg")
        )).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectUrlOutsideAllowedPrefix() {
        SocialObjectStorageProperties properties = new SocialObjectStorageProperties();
        properties.setEnabled(true);
        properties.setPublicUrl("https://cdn.2hands.vn");
        properties.setPublicPathPrefix("social");
        PostMediaUrlValidator validator = new PostMediaUrlValidator(properties);
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> validator.validateMediaUrls(
                userId,
                List.of("https://evil.example.com/posts/" + userId + "/abc.jpg")
        ))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR));
    }

    @Test
    void shouldAcceptUrlWithoutPathPrefixForDirectMinioAccess() {
        SocialObjectStorageProperties properties = new SocialObjectStorageProperties();
        properties.setEnabled(true);
        properties.setPublicUrl("http://localhost:9000/2hands-social-post");
        properties.setPublicPathPrefix("");
        PostMediaUrlValidator validator = new PostMediaUrlValidator(properties);
        UUID userId = UUID.randomUUID();

        assertThatCode(() -> validator.validateMediaUrls(
                userId,
                List.of("http://localhost:9000/2hands-social-post/posts/" + userId + "/abc.jpg")
        )).doesNotThrowAnyException();
    }

    @Test
    void shouldAcceptLegacySocialSegmentInLocalMinioUrl() {
        SocialObjectStorageProperties properties = new SocialObjectStorageProperties();
        properties.setEnabled(true);
        properties.setPublicUrl("http://localhost:9000/2hands-social-post");
        properties.setPublicPathPrefix("");
        PostMediaUrlValidator validator = new PostMediaUrlValidator(properties);
        UUID userId = UUID.randomUUID();

        assertThatCode(() -> validator.validateMediaUrls(
                userId,
                List.of("http://localhost:9000/2hands-social-post/social/posts/" + userId + "/abc.jpg")
        )).doesNotThrowAnyException();
    }

    @Test
    void shouldAcceptLanHostUrlWithSameObjectPath() {
        SocialObjectStorageProperties properties = new SocialObjectStorageProperties();
        properties.setEnabled(true);
        properties.setPublicUrl("http://localhost:9000/2hands-social-post");
        properties.setPublicPathPrefix("");
        PostMediaUrlValidator validator = new PostMediaUrlValidator(properties);
        UUID userId = UUID.randomUUID();

        assertThatCode(() -> validator.validateMediaUrls(
                userId,
                List.of("http://192.168.1.52:9000/2hands-social-post/posts/" + userId + "/abc.jpg")
        )).doesNotThrowAnyException();
    }

    @Test
    void shouldSkipValidationWhenStorageDisabled() {
        SocialObjectStorageProperties properties = new SocialObjectStorageProperties();
        properties.setEnabled(false);
        PostMediaUrlValidator validator = new PostMediaUrlValidator(properties);

        assertThatCode(() -> validator.validateMediaUrls(
                UUID.randomUUID(),
                List.of("https://any.example.com/file.jpg")
        )).doesNotThrowAnyException();
    }

    @Test
    void buildPublicObjectUrlShouldUseCanonicalHostWithoutClientUploadOrigin() {
        SocialObjectStorageProperties properties = new SocialObjectStorageProperties();
        properties.setPublicUrl("http://localhost:9000/2hands-social-post");
        properties.setPublicPathPrefix("");

        String objectKey = "posts/user-1/video.mp4";
        assertThat(properties.buildPublicObjectUrl(objectKey))
                .isEqualTo("http://localhost:9000/2hands-social-post/posts/user-1/video.mp4");
        assertThat(properties.buildPublicObjectUrl(objectKey, "http://192.168.1.4:9000"))
                .isEqualTo("http://192.168.1.4:9000/2hands-social-post/posts/user-1/video.mp4");
    }
}
