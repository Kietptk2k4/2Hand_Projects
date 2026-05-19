package com.twohands.social_service.application.user.viewfollowersfollowinglist;

import com.twohands.social_service.domain.follow.Follow;
import com.twohands.social_service.domain.follow.FollowRelationEntry;
import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.follow.FollowStatus;
import com.twohands.social_service.domain.follow.RelationListType;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.domain.user.UserProjection;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ViewFollowersFollowingListUseCase {

    private static final int MIN_PAGE = 0;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 50;

    private final UserProjectionRepository userProjectionRepository;
    private final FollowRepository followRepository;

    public ViewFollowersFollowingListUseCase(
            UserProjectionRepository userProjectionRepository,
            FollowRepository followRepository
    ) {
        this.userProjectionRepository = userProjectionRepository;
        this.followRepository = followRepository;
    }

    @Transactional(readOnly = true)
    public ViewFollowersFollowingListResult execute(ViewFollowersFollowingListCommand command) {
        if (command.viewerId() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        validatePagination(command.page(), command.size());

        UserProjection target = userProjectionRepository.findByUserId(command.targetUserId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Nguoi dung khong ton tai."));

        if (target.isDeleted()) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Nguoi dung khong ton tai hoac da bi xoa.");
        }

        if (!canViewRelations(command.viewerId(), command.targetUserId(), target)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Khong co quyen xem danh sach quan he cua tai khoan rieng tu.");
        }

        PageResult<FollowRelationEntry> relationPage = command.type() == RelationListType.FOLLOWERS
                ? followRepository.findAcceptedFollowersPage(command.targetUserId(), command.page(), command.size())
                : followRepository.findAcceptedFollowingPage(command.targetUserId(), command.page(), command.size());

        List<ViewFollowersFollowingListResult.RelationUserItem> items = relationPage.items().stream()
                .map(this::toUserItem)
                .toList();

        PageResult<ViewFollowersFollowingListResult.RelationUserItem> userPage = new PageResult<>(
                items,
                relationPage.page(),
                relationPage.size(),
                relationPage.totalElements(),
                relationPage.totalPages(),
                relationPage.hasNext()
        );

        return new ViewFollowersFollowingListResult(
                command.targetUserId().toString(),
                command.type(),
                userPage
        );
    }

    public String successMessage() {
        return "Lay danh sach quan he thanh cong.";
    }

    private ViewFollowersFollowingListResult.RelationUserItem toUserItem(FollowRelationEntry entry) {
        return userProjectionRepository.findByUserId(entry.userId())
                .filter(user -> !user.isDeleted())
                .map(user -> new ViewFollowersFollowingListResult.RelationUserItem(
                        user.userId(),
                        user.displayName(),
                        user.avatarUrl(),
                        entry.followedAt()
                ))
                .orElse(new ViewFollowersFollowingListResult.RelationUserItem(
                        entry.userId().toString(),
                        null,
                        null,
                        entry.followedAt()
                ));
    }

    private boolean canViewRelations(UUID viewerId, UUID targetUserId, UserProjection target) {
        if (viewerId.equals(targetUserId)) {
            return true;
        }
        if (!target.isPrivateProfile()) {
            return true;
        }
        return followRepository.findByFollowerIdAndFolloweeId(viewerId, targetUserId)
                .map(Follow::status)
                .filter(FollowStatus.ACCEPTED::equals)
                .isPresent();
    }

    private void validatePagination(int page, int size) {
        if (page < MIN_PAGE) {
            throw new AppException(
                    ErrorCode.INVALID_PAGINATION,
                    "Tham so pagination khong hop le.",
                    "page",
                    "MUST_BE_GREATER_THAN_OR_EQUAL_TO_0"
            );
        }
        if (size < MIN_SIZE || size > MAX_SIZE) {
            throw new AppException(
                    ErrorCode.INVALID_PAGINATION,
                    "Tham so pagination khong hop le.",
                    "size",
                    "MUST_BE_BETWEEN_1_AND_50"
            );
        }
    }
}
