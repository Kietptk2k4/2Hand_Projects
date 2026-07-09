import { useCallback, useEffect, useState } from "react";
import { getUsersForRbac } from "../../../api/authApi";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import {
  RBAC_USER_LIST_PAGE_SIZE,
  RBAC_USER_LIST_SORT_OPTIONS,
  RBAC_USER_LIST_STATUS_OPTIONS,
} from "../constants/rbacUserListConstants.js";
import { getRbacSortColumnLabel } from "./RbacUserFilterBar.jsx";
import { RbacUserListView } from "./RbacUserListView.jsx";

export function RbacUserListPanel({
  userListFilters,
  onFiltersChange,
  selectedUserId,
  onUserSelect,
  onSelectedUserSync,
  listRefreshKey = 0,
}) {
  const { showSessionExpired } = useAuthSession();
  const [result, setResult] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const [draftFilters, setDraftFilters] = useState({
    status: userListFilters.status || "",
    q: userListFilters.q || "",
    sort: userListFilters.sort || "email",
  });

  useEffect(() => {
    setDraftFilters({
      status: userListFilters.status || "",
      q: userListFilters.q || "",
      sort: userListFilters.sort || "email",
    });
  }, [userListFilters]);

  const fetchList = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await getUsersForRbac({
        status: userListFilters.status || undefined,
        q: userListFilters.q || undefined,
        sort: userListFilters.sort || "email",
        page: Number(userListFilters.page) || 1,
        size: Number(userListFilters.size) || RBAC_USER_LIST_PAGE_SIZE,
      });
      setResult(data);
      setStatus("ready");
    } catch (error) {
      if (error?.code === 401) {
        showSessionExpired(error?.message);
        return;
      }
      if (error?.code === 403) {
        setStatus("forbidden");
        setErrorMessage(error?.message || "Bạn không có quyền truy cập.");
        return;
      }
      setStatus("error");
      setErrorMessage(error?.message || "Không tải được danh sách người dùng.");
    }
  }, [userListFilters, showSessionExpired]);

  useEffect(() => {
    fetchList();
  }, [fetchList, listRefreshKey]);

  useEffect(() => {
    if (!selectedUserId || !result?.items) return;
    const row = result.items.find((item) => item.id === selectedUserId);
    if (row) onSelectedUserSync?.(row);
  }, [selectedUserId, result, onSelectedUserSync]);

  const handleApplyFilters = (event) => {
    event.preventDefault();
    onFiltersChange?.({
      ...draftFilters,
      page: 1,
      size: RBAC_USER_LIST_PAGE_SIZE,
    });
  };

  const handleClearFilters = () => {
    const cleared = {
      status: "",
      q: "",
      sort: "email",
      page: 1,
      size: RBAC_USER_LIST_PAGE_SIZE,
    };
    setDraftFilters(cleared);
    onFiltersChange?.(cleared);
  };

  const pagination = result?.pagination;
  const currentPage = Number(userListFilters.page) || pagination?.page || 1;
  const totalPages = pagination?.total_pages || 1;
  const activeSort = userListFilters.sort || "email";
  const items = result?.items || [];

  const handlePageChange = (nextPage) => {
    onFiltersChange?.({
      ...userListFilters,
      page: nextPage,
      size: RBAC_USER_LIST_PAGE_SIZE,
    });
  };

  return (
    <RbacUserListView
      status={status}
      errorMessage={errorMessage}
      draftFilters={draftFilters}
      statusOptions={RBAC_USER_LIST_STATUS_OPTIONS}
      sortOptions={RBAC_USER_LIST_SORT_OPTIONS}
      items={items}
      pagination={pagination}
      currentPage={currentPage}
      totalPages={totalPages}
      activeSortLabel={getRbacSortColumnLabel(activeSort)}
      selectedUserId={selectedUserId}
      onDraftChange={(patch) => setDraftFilters((prev) => ({ ...prev, ...patch }))}
      onApplyFilters={handleApplyFilters}
      onClearFilters={handleClearFilters}
      onPageChange={handlePageChange}
      onUserSelect={onUserSelect}
      onRetry={fetchList}
    />
  );
}
