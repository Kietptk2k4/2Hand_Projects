package com.twohands.social_service.application.admin.viewpostlistformoderation;

import com.twohands.social_service.application.admin.common.AdminModerationAccessPolicy;
import com.twohands.social_service.application.admin.common.AdminModerationListQueryPolicy;
import com.twohands.social_service.domain.admin.AdminModerationListSortField;
import com.twohands.social_service.domain.admin.AdminPostListCriteria;
import com.twohands.social_service.domain.admin.AdminPostListRepository;
import com.twohands.social_service.domain.post.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ViewPostListForModerationUseCase {

    private static final String SUCCESS_MESSAGE = "Lay danh sach bai viet dieu tra thanh cong.";
    private static final int MAX_SIZE = 50;

    private final AdminPostListRepository adminPostListRepository;

    public ViewPostListForModerationUseCase(AdminPostListRepository adminPostListRepository) {
        this.adminPostListRepository = adminPostListRepository;
    }

    @Transactional(readOnly = true)
    public ViewPostListForModerationResult execute(ViewPostListForModerationCommand command) {
        AdminModerationAccessPolicy.ensureCanViewPostList(command.actor());

        int page = AdminModerationListQueryPolicy.validatePage(command.page());
        int size = AdminModerationListQueryPolicy.validateSize(command.size(), MAX_SIZE);
        AdminModerationListSortField sortField = AdminModerationListQueryPolicy.parsePostSortField(command.sort());

        PageResult<com.twohands.social_service.domain.admin.AdminPostListItem> pageResult =
                adminPostListRepository.findPage(new AdminPostListCriteria(
                        Optional.ofNullable(AdminModerationListQueryPolicy.normalizePostStatus(command.status())),
                        Optional.ofNullable(AdminModerationListQueryPolicy.normalizePostModerationStatus(command.moderationStatus())),
                        Optional.ofNullable(AdminModerationListQueryPolicy.normalizeQuery(command.query())),
                        sortField,
                        page,
                        size
                ));

        return new ViewPostListForModerationResult(
                pageResult.items().stream()
                        .map(item -> new ViewPostListForModerationResult.Item(
                                item.id(),
                                item.authorId(),
                                item.captionPreview(),
                                item.status(),
                                item.moderationStatus(),
                                item.likeCount(),
                                item.createdAt(),
                                item.updatedAt()
                        ))
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
}
