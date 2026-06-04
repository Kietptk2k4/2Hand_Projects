package com.twohands.social_service.application.post.likeunlikepost;

import com.twohands.social_service.application.post.common.PostLikedOutboxService;
import com.twohands.social_service.domain.outbox.OutboxEventRepository;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostLikeRepository;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.post.PostStatus;
import com.twohands.social_service.application.user.common.UserWriteGuard;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class LikeUnlikePostUseCase {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final PostLikedOutboxService postLikedOutboxService;
    private final UserWriteGuard userWriteGuard;

    public LikeUnlikePostUseCase(
            PostRepository postRepository,
            PostLikeRepository postLikeRepository,
            OutboxEventRepository outboxEventRepository,
            PostLikedOutboxService postLikedOutboxService,
            UserWriteGuard userWriteGuard
    ) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.postLikedOutboxService = postLikedOutboxService;
        this.userWriteGuard = userWriteGuard;
    }

    @Transactional
    public LikeUnlikePostResult execute(LikeUnlikePostCommand command) {
        userWriteGuard.assertCanWrite(command.userId());

        Post post = postRepository.findById(command.postId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Bai viet khong ton tai."));

        if (post.status() != PostStatus.ACTIVE) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Bai viet khong ton tai hoac chua duoc xuat ban.");
        }

        boolean alreadyLiked = postLikeRepository.existsByPostIdAndUserId(command.postId(), command.userId());
        Instant now = Instant.now();

        if (alreadyLiked) {
            postLikeRepository.deleteByPostIdAndUserId(command.postId(), command.userId());
            postRepository.decrementLikeCount(command.postId());
        } else {
            postLikeRepository.save(command.postId(), command.userId());
            postRepository.incrementLikeCount(command.postId());
            outboxEventRepository.save(postLikedOutboxService.build(
                    command.postId(),
                    command.userId(),
                    post.authorId(),
                    now
            ));
        }

        Post updated = postRepository.findById(command.postId()).orElse(post);

        return new LikeUnlikePostResult(
                updated.id(),
                !alreadyLiked,
                updated.likeCount()
        );
    }

    public String successMessage(boolean liked) {
        return liked ? "Like bai viet thanh cong." : "Unlike bai viet thanh cong.";
    }

}
