package com.twohands.social_service.domain.admin;

import com.twohands.social_service.domain.post.PageResult;

public interface AdminCommentListRepository {
    PageResult<AdminCommentListItem> findPage(AdminCommentListCriteria criteria);
}
