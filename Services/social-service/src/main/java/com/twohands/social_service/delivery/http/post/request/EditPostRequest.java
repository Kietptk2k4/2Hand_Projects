package com.twohands.social_service.delivery.http.post.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class EditPostRequest {

    private Optional<String> caption = Optional.empty();
    private Optional<List<MediaItemRequest>> media = Optional.empty();
    private Optional<List<ProductTagRequest>> productTags = Optional.empty();
    private Optional<String> visibility = Optional.empty();
    private Optional<Boolean> allowComments = Optional.empty();
    private Optional<List<String>> hashtags = Optional.empty();

    public Optional<String> caption() {
        return caption;
    }

    @JsonSetter("caption")
    public void setCaption(String caption) {
        this.caption = Optional.ofNullable(caption);
    }

    public Optional<List<MediaItemRequest>> media() {
        return media;
    }

    @JsonSetter("media")
    public void setMedia(List<MediaItemRequest> media) {
        this.media = Optional.ofNullable(media != null ? media : List.of());
    }

    public Optional<List<ProductTagRequest>> productTags() {
        return productTags;
    }

    @JsonSetter("productTags")
    public void setProductTags(List<ProductTagRequest> productTags) {
        this.productTags = Optional.ofNullable(productTags != null ? productTags : List.of());
    }

    public Optional<String> visibility() {
        return visibility;
    }

    @JsonSetter("visibility")
    public void setVisibility(String visibility) {
        this.visibility = Optional.ofNullable(visibility);
    }

    public Optional<Boolean> allowComments() {
        return allowComments;
    }

    @JsonSetter("allowComments")
    public void setAllowComments(Boolean allowComments) {
        this.allowComments = Optional.ofNullable(allowComments);
    }

    public Optional<List<String>> hashtags() {
        return hashtags;
    }

    @JsonSetter("hashtags")
    public void setHashtags(List<String> hashtags) {
        this.hashtags = Optional.ofNullable(hashtags != null ? hashtags : List.of());
    }

    public record MediaItemRequest(
            @Size(max = 2048)
            String url,
            String type
    ) {
    }

    public record ProductTagRequest(
            String productId,
            BigDecimal price
    ) {
    }
}
