package com.twohands.auth_service.unit.common.media;

import com.twohands.auth_service.common.media.StoredMediaUrlRewriter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StoredMediaUrlRewriterTest {

  private static final String GATEWAY = "https://demo.example.com";
  private static final String AUTH_PUBLIC = GATEWAY + "/2hands-avatar";
  private static final String SOCIAL_PUBLIC = GATEWAY + "/2hands-social-post";
  private static final String COMMERCE_PUBLIC = GATEWAY;

  @Test
  void shouldRewriteLocalhostAvatarUrlToConfiguredPublicOrigin() {
    String stored = "http://localhost:9000/2hands-avatar/users/u1/avatar.jpg";
    assertThat(StoredMediaUrlRewriter.rewrite(stored, AUTH_PUBLIC))
        .isEqualTo("https://demo.example.com/2hands-avatar/users/u1/avatar.jpg");
  }

  @Test
  void shouldRewriteLocalhostSocialPostMediaToConfiguredPublicOrigin() {
    String stored = "http://localhost:9000/2hands-social-post/posts/u1/video.mp4";
    assertThat(StoredMediaUrlRewriter.rewrite(stored, SOCIAL_PUBLIC))
        .isEqualTo("https://demo.example.com/2hands-social-post/posts/u1/video.mp4");
  }

  @Test
  void shouldRewriteLocalhostCommerceProductMediaToConfiguredPublicOrigin() {
    String stored = "http://localhost:9000/2hands-commerce-product/products/a/b/images/1.jpg";
    assertThat(StoredMediaUrlRewriter.rewrite(stored, COMMERCE_PUBLIC))
        .isEqualTo("https://demo.example.com/2hands-commerce-product/products/a/b/images/1.jpg");
  }

  @Test
  void shouldLeaveNonObjectStorageUrlsUnchanged() {
    String external = "https://cdn.2hands.vn/other/image.png";
    assertThat(StoredMediaUrlRewriter.rewrite(external, AUTH_PUBLIC)).isEqualTo(external);
  }

  @Test
  void shouldLeaveAlreadyGatewayUrlsUnchangedWhenOriginMatches() {
    String gatewayUrl = "https://demo.example.com/2hands-social-post/posts/u1/a.jpg";
    assertThat(StoredMediaUrlRewriter.rewrite(gatewayUrl, SOCIAL_PUBLIC)).isEqualTo(gatewayUrl);
  }
}
