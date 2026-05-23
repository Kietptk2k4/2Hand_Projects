package com.twohands.social_service.application.comment.likecomment;

import com.twohands.social_service.domain.comment.Comment;
import com.twohands.social_service.domain.comment.CommentReactionRepository;
import com.twohands.social_service.domain.comment.CommentRepository;
import com.twohands.social_service.domain.comment.CommentStatus;
import com.twohands.social_service.application.user.common.UserWriteGuard;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeCommentUseCase {

    private final CommentRepository commentRepository;
    private final CommentReactionRepository commentReactionRepository;
    private final UserWriteGuard userWriteGuard;

    public LikeCommentUseCase(
            CommentRepository commentRepository,
            CommentReactionRepository commentReactionRepository,
            UserWriteGuard userWriteGuard
    ) {
        this.commentRepository = commentRepository;
        this.commentReactionRepository = commentReactionRepository;
        this.userWriteGuard = userWriteGuard;
    }

    @Transactional
    public LikeCommentResult execute(LikeCommentCommand command) {
        userWriteGuard.assertCanWrite(command.userId());

        Comment comment = commentRepository.findById(command.commentId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Comment khong ton tai."));

        if (comment.status() != CommentStatus.ACTIVE) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Comment khong ton tai hoac da bi xoa.");
        }

        boolean alreadyLiked = commentReactionRepository.existsByCommentIdAndUserId(
                command.commentId(),
                command.userId()
        );

        if (alreadyLiked) {
            commentReactionRepository.deleteByCommentIdAndUserId(command.commentId(), command.userId());
            commentRepository.decrementLikeCount(command.commentId());
        } else {
            commentReactionRepository.save(command.commentId(), command.userId());
            commentRepository.incrementLikeCount(command.commentId());
        }

        Comment updated = commentRepository.findById(command.commentId())
                .orElse(comment);

        return new LikeCommentResult(
                updated.id(),
                !alreadyLiked,
                updated.likeCount()
        );
    }

    public String successMessage(boolean liked) {
        return liked ? "Like comment thanh cong." : "Unlike comment thanh cong.";
    }

}
