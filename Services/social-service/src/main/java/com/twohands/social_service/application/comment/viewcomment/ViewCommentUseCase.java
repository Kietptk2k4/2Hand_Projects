package com.twohands.social_service.application.comment.viewcomment;

import com.twohands.social_service.domain.comment.Comment;
import com.twohands.social_service.domain.comment.CommentRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViewCommentUseCase {

    private final CommentRepository commentRepository;

    public ViewCommentUseCase(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Transactional(readOnly = true)
    public ViewCommentResult execute(String commentId) {
        validateCommentId(commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Comment khong ton tai."));

        return new ViewCommentResult(
                comment.id(),
                comment.postId(),
                comment.authorId(),
                comment.status().name(),
                comment.moderationStatusOrDefault().name()
        );
    }

    public String successMessage() {
        return "Lay thong tin comment thanh cong.";
    }

    private void validateCommentId(String commentId) {
        if (commentId == null || commentId.isBlank() || !ObjectId.isValid(commentId)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Comment id khong hop le.");
        }
    }
}
