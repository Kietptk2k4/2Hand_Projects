package com.twohands.social_service.application.admin.viewcommentlistformoderation;

import com.twohands.social_service.application.admin.common.AdminModerationAccessPolicy;
import com.twohands.social_service.application.admin.common.AdminModerationAuthorResolver;
import com.twohands.social_service.application.admin.common.AdminModerationListQueryPolicy;
import com.twohands.social_service.domain.admin.AdminCommentListCriteria;
import com.twohands.social_service.domain.admin.AdminCommentListItem;
import com.twohands.social_service.domain.admin.AdminCommentListRepository;
import com.twohands.social_service.domain.admin.AdminCommentListSortField;
import com.twohands.social_service.domain.post.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ViewCommentListForModerationUseCase {

    private static final String SUCCESS_MESSAGE = "Lay danh sach binh luan dieu tra thanh cong.";
    private static final int MAX_SIZE = 50;

    private final AdminCommentListRepository adminCommentListRepository;
    private final AdminModerationAuthorResolver authorResolver;

    public ViewCommentListForModerationUseCase(
            AdminCommentListRepository adminCommentListRepository,
            AdminModerationAuthorResolver authorResolver
    ) {
        this.adminCommentListRepository = adminCommentListRepository;
        this.authorResolver = authorResolver;
    }

    @Transactional(readOnly = true)
    public ViewCommentListForModerationResult execute(ViewCommentListForModerationCommand command) {
        AdminModerationAccessPolicy.ensureCanViewCommentList(command.actor());

        int page = AdminModerationListQueryPolicy.validatePage(command.page());
        int size = AdminModerationListQueryPolicy.validateSize(command.size(), MAX_SIZE);
        AdminCommentListSortField sortField = AdminModerationListQueryPolicy.parseCommentSortField(command.sort());

        PageResult<AdminCommentListItem> pageResult = adminCommentListRepository.findPage(new AdminCommentListCriteria(
                Optional.ofNullable(AdminModerationListQueryPolicy.normalizeCommentStatus(command.status())),
                Optional.ofNullable(AdminModerationListQueryPolicy.normalizeCommentModerationStatus(command.moderationStatus())),
                Optional.ofNullable(AdminModerationListQueryPolicy.normalizePostId(command.postId())),
                Optional.ofNullable(AdminModerationListQueryPolicy.normalizeQuery(command.query())),
                sortField,
                page,
                size
        ));

        Map<String, AdminModerationAuthorResolver.AuthorSummary> authors = authorResolver.resolveAuthors(
                pageResult.items().stream().map(AdminCommentListItem::authorId).collect(Collectors.toSet())
        );

        return new ViewCommentListForModerationResult(
                pageResult.items().stream()
                        .map(item -> toItem(item, authors.get(item.authorId())))
                        .toList(),
                new ViewCommentListForModerationResult.Pagination(
                        (int) pageResult.page(),
                        (int) pageResult.size(),
                        pageResult.totalElements(),
                        (int) pageResult.totalPages(),
                        pageResult.hasNext()
                )
        );
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }

    private ViewCommentListForModerationResult.Item toItem(
            AdminCommentListItem item,
            AdminModerationAuthorResolver.AuthorSummary author
    ) {
        String authorDisplayName = author != null ? author.displayName() : item.authorDisplayName();
        String authorAvatarUrl = author != null ? author.avatarUrl() : item.authorAvatarUrl();

        return new ViewCommentListForModerationResult.Item(
                item.id(),
                item.postId(),
                item.authorId(),
                authorDisplayName,
                authorAvatarUrl,
                item.parentCommentId(),
                item.contentPreview(),
                item.status(),
                item.moderationStatus(),
                item.mediaCount(),
                item.likeCount(),
                item.createdAt(),
                item.updatedAt()
        );
    }
}
