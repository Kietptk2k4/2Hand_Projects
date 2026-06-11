package com.twohands.auth_service.domain.rbac;

public interface RbacUserListRepository {

    RbacUserListPagedResult findPage(RbacUserListCriteria criteria);
}
