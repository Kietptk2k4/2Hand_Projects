package com.twohands.social_service.domain.admin;

import com.twohands.social_service.domain.post.PageResult;

public interface AdminPostListRepository {
    PageResult<AdminPostListItem> findPage(AdminPostListCriteria criteria);
}
