package com.twohands.social_service.application.admin.viewcommentlistformoderation;

import com.twohands.social_service.application.admin.common.AdminModerationAccessPolicy;
import com.twohands.social_service.application.admin.common.AdminModerationListQueryPolicy;
import com.twohands.social_service.domain.admin.AdminCommentListCriteria;
import com.twohands.social_service.domain.admin.AdminCommentListRepository;
import com.twohands.social_service.domain.admin.AdminCommentListSortField;
import com.twohands.social_service.domain.post.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ViewCommentListForModerationUseCase {

    private static final String SUCCESS_MESSAGE = "Lay danh sach binh luan dieu tra thanh cong.";
    private static final int MAX_SIZE = 50;

    private final AdminCommentListRepository adminCommentListRepository;

    public ViewCommentListForModerationUseCase(AdminCommentListRepository adminCommentListRepository) {
        this.adminCommentListRepository = adminCommentListRepository;
    }

    @Transactional(readOnly = true)
    public ViewCommentListForModerationResult execute(ViewCommentListForModerationCommand command) {
        AdminModerationAccessPolicy.ensureCanViewCommentList(command.actor());

        int page = AdminModerationListQueryPolicy.validatePage(command.page());
        int size = AdminModerationListQueryPolicy.validateSize(command.size(), MAX_SIZE);
        AdminCommentListSortField sortField = AdminModerationListQueryPolicy.parseCommentSortField(command.sort());

        PageResult<com.twohands.social_service.domain.admin.AdminCommentListItem> pageResult =
                adminCommentListRepository.findPage(new AdminCommentListCriteria(
                        Optional.ofNullable(AdminModerationListQueryPolicy.normalizeCommentStatus(command.status())),
                        Optional.ofNullable(AdminModerationListQueryPolicy.normalizePostId(command.postId())),
                        Optional.ofNullable(AdminModerationListQueryPolicy.normalizeQuery(command.query())),
                        sortField,
                        page,
                        size
                ));

        return new ViewCommentListForModerationResult(
                pageResult.items().stream()
                        .map(item -> new ViewCommentListForModerationResult.Item(
                                item.id(),
                                item.postId(),
                                item.authorId(),
                                item.contentPreview(),
                                item.status(),
                                item.likeCount(),
                                item.createdAt(),
                                item.updatedAt()
                        ))
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
}
