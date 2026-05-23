package com.twohands.social_service.delivery.http.comment.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CommentPostRequest(
        @NotBlank(message = "Noi dung binh luan khong duoc de trong.")
        @Size(max = 2000, message = "Noi dung binh luan khong duoc vuot qua 2000 ky tu.")
        String contentText,

        @Size(max = 5, message = "Khong duoc dinh kem qua 5 media items.")
        @Valid
        List<MediaItemRequest> media
) {
    public record MediaItemRequest(
            @NotBlank(message = "URL media khong duoc de trong.")
            String url,

            @NotBlank(message = "Media type khong duoc de trong.")
            String type
    ) {
    }
}
