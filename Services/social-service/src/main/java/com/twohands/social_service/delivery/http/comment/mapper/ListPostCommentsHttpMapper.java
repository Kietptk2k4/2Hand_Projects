package com.twohands.social_service.delivery.http.comment.mapper;

import com.twohands.social_service.application.comment.listpostcomments.ListPostCommentsResult;
import com.twohands.social_service.delivery.http.comment.response.ListPostCommentsResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListPostCommentsHttpMapper {

    public ListPostCommentsResponse toResponse(ListPostCommentsResult result) {
        List<ListPostCommentsResponse.CommentItemResponse> items = result.items().stream()
                .map(this::toItem)
                .toList();
        ListPostCommentsResponse.PageMetaResponse meta = new ListPostCommentsResponse.PageMetaResponse(
                result.meta().page(),
                result.meta().size(),
                result.meta().totalElements(),
                result.meta().totalPages(),
                result.meta().hasNext()
        );
        return new ListPostCommentsResponse(items, meta);
    }

    private ListPostCommentsResponse.CommentItemResponse toItem(ListPostCommentsResult.CommentItem item) {
        List<ListPostCommentsResponse.MediaItemResponse> media = item.media().stream()
                .map(m -> new ListPostCommentsResponse.MediaItemResponse(m.url(), m.type()))
                .toList();
        ListPostCommentsResponse.AuthorResponse author = new ListPostCommentsResponse.AuthorResponse(
                item.author().userId(),
                item.author().displayName(),
                item.author().avatarUrl()
        );
        return new ListPostCommentsResponse.CommentItemResponse(
                item.commentId(),
                item.postId(),
                item.parentCommentId(),
                author,
                item.contentText(),
                media,
                item.likeCount(),
                item.likedByMe(),
                item.replyCount(),
                item.createdAt(),
                item.updatedAt()
        );
    }
}
