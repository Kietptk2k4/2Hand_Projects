package com.twohands.social_service.delivery.http.post.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CreatePostRequest(
        @Size(max = 2000, message = "Caption khong duoc vuot qua 2000 ky tu.")
        String caption,

        @Size(max = 10, message = "Khong duoc upload qua 10 media items.")
        @Valid
        List<MediaItemRequest> media,

        @Size(max = 10, message = "Khong duoc tag qua 10 san pham.")
        @Valid
        List<ProductTagRequest> productTags,

        @NotBlank(message = "Visibility khong duoc de trong.")
        String visibility,

        boolean allowComments,

        @Size(max = 30, message = "Khong duoc co qua 30 hashtags.")
        List<String> hashtags,

        boolean publish
) {
    public record MediaItemRequest(
            @NotBlank(message = "URL media khong duoc de trong.")
            String url,

            @NotNull(message = "Media type khong duoc de trong.")
            String type,

            Integer width,

            Integer height
    ) {
    }

    public record ProductTagRequest(
            @NotBlank(message = "product_id khong duoc de trong.")
            String productId,

            BigDecimal price
    ) {
    }
}
