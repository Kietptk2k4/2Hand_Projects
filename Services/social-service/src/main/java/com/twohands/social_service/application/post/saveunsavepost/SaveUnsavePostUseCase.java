package com.twohands.social_service.application.post.saveunsavepost;

import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.post.PostSaveRepository;
import com.twohands.social_service.domain.post.PostStatus;
import com.twohands.social_service.application.user.common.UserWriteGuard;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaveUnsavePostUseCase {

    private final PostRepository postRepository;
    private final PostSaveRepository postSaveRepository;
    private final UserWriteGuard userWriteGuard;

    public SaveUnsavePostUseCase(
            PostRepository postRepository,
            PostSaveRepository postSaveRepository,
            UserWriteGuard userWriteGuard
    ) {
        this.postRepository = postRepository;
        this.postSaveRepository = postSaveRepository;
        this.userWriteGuard = userWriteGuard;
    }

    @Transactional
    public SaveUnsavePostResult execute(SaveUnsavePostCommand command) {
        userWriteGuard.assertCanWrite(command.userId());

        Post post = postRepository.findById(command.postId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Bai viet khong ton tai."));

        if (post.status() == PostStatus.DELETED) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Bai viet khong ton tai hoac da bi xoa.");
        }

        boolean alreadySaved = postSaveRepository.existsByPostIdAndUserId(command.postId(), command.userId());

        if (alreadySaved) {
            postSaveRepository.deleteByPostIdAndUserId(command.postId(), command.userId());
        } else {
            postSaveRepository.save(command.postId(), command.userId());
        }

        return new SaveUnsavePostResult(command.postId(), !alreadySaved);
    }

    public String successMessage(boolean saved) {
        return saved ? "Luu bai viet thanh cong." : "Bo luu bai viet thanh cong.";
    }

}
