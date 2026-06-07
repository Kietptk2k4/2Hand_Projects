package com.twohands.social_service.delivery.http.reaction.mapper;

import com.twohands.social_service.application.reaction.common.ViewLikeUsersResult;
import com.twohands.social_service.delivery.http.reaction.response.ViewLikeUsersResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ViewLikeUsersHttpMapper {

    public ViewLikeUsersResponse toResponse(ViewLikeUsersResult result) {
        List<ViewLikeUsersResponse.LikeUserItemResponse> items = result.items().stream()
                .map(item -> new ViewLikeUsersResponse.LikeUserItemResponse(
                        item.userId(),
                        item.displayName(),
                        item.avatarUrl(),
                        item.likedAt()
                ))
                .toList();

        ViewLikeUsersResult.PageMeta meta = result.meta();
        return new ViewLikeUsersResponse(
                items,
                new ViewLikeUsersResponse.PageMetaResponse(
                        meta.page(),
                        meta.size(),
                        meta.totalElements(),
                        meta.totalPages(),
                        meta.hasNext()
                )
        );
    }
}