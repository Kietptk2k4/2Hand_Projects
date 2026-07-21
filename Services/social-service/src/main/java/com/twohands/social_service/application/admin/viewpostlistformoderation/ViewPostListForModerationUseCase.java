package com.twohands.social_service.application.admin.viewpostlistformoderation;

import com.twohands.social_service.application.admin.common.AdminModerationAccessPolicy;
import com.twohands.social_service.application.admin.common.AdminModerationAuthorResolver;
import com.twohands.social_service.application.admin.common.AdminModerationListQueryPolicy;
import com.twohands.social_service.domain.admin.AdminModerationListSortField;
import com.twohands.social_service.domain.admin.AdminPostListCriteria;
import com.twohands.social_service.domain.admin.AdminPostListItem;
import com.twohands.social_service.domain.admin.AdminPostListRepository;
import com.twohands.social_service.domain.post.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ViewPostListForModerationUseCase {

    private static final String SUCCESS_MESSAGE = "Lay danh sach bai viet dieu tra thanh cong.";
    private static final int MAX_SIZE = 50;

    private final AdminPostListRepository adminPostListRepository;
    private final AdminModerationAuthorResolver authorResolver;

    public ViewPostListForModerationUseCase(
            AdminPostListRepository adminPostListRepository,
            AdminModerationAuthorResolver authorResolver
    ) {
        this.adminPostListRepository = adminPostListRepository;
        this.authorResolver = authorResolver;
    }

    @Transactional(readOnly = true)
    public ViewPostListForModerationResult execute(ViewPostListForModerationCommand command) {
        AdminModerationAccessPolicy.ensureCanViewPostList(command.actor());

        int page = AdminModerationListQueryPolicy.validatePage(command.page());
        int size = AdminModerationListQueryPolicy.validateSize(command.size(), MAX_SIZE);
        AdminModerationListSortField sortField = AdminModerationListQueryPolicy.parsePostSortField(command.sort());

        PageResult<AdminPostListItem> pageResult = adminPostListRepository.findPage(new AdminPostListCriteria(
                Optional.ofNullable(AdminModerationListQueryPolicy.normalizePostStatus(command.status())),
                Optional.ofNullable(AdminModerationListQueryPolicy.normalizePostModerationStatus(command.moderationStatus())),
                Optional.ofNullable(AdminModerationListQueryPolicy.normalizeQuery(command.query())),
                sortField,
                page,
                size
        ));

        Map<String, AdminModerationAuthorResolver.AuthorSummary> authors = authorResolver.resolveAuthors(
                pageResult.items().stream().map(AdminPostListItem::authorId).collect(Collectors.toSet())
        );

        return new ViewPostListForModerationResult(
                pageResult.items().stream()
                        .map(item -> toItem(item, authors.get(item.authorId())))
                        .toList(),
                new ViewPostListForModerationResult.Pagination(
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

    private ViewPostListForModerationResult.Item toItem(
            AdminPostListItem item,
            AdminModerationAuthorResolver.AuthorSummary author
    ) {
        String authorDisplayName = author != null ? author.displayName() : item.authorDisplayName();
        String authorAvatarUrl = author != null ? author.avatarUrl() : item.authorAvatarUrl();

        return new ViewPostListForModerationResult.Item(
                item.id(),
                item.authorId(),
                authorDisplayName,
                authorAvatarUrl,
                item.captionPreview(),
                item.thumbnailUrl(),
                item.mediaCount(),
                item.status(),
                item.moderationStatus(),
                item.likeCount(),
                item.createdAt(),
                item.updatedAt()
        );
    }
}
