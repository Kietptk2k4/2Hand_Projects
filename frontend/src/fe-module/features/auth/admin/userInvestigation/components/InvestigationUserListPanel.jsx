import { useCallback, useEffect, useState } from "react";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { getUsersForInvestigation } from "../api/userInvestigationApi.js";
import {
  INVESTIGATION_USER_LIST_PAGE_SIZE,
  INVESTIGATION_USER_LIST_SORT_OPTIONS,
  INVESTIGATION_USER_LIST_STATUS_OPTIONS,
} from "../constants/investigationUserListConstants.js";
import { mapAdminUserListItemToInvestigationTarget } from "../utils/adminUserListMapper.js";
import { getSortColumnLabel } from "./InvestigationUserFilterBar.jsx";
import { InvestigationUserListView } from "./InvestigationUserListView.jsx";

export function InvestigationUserListPanel({
  userListFilters,
  onFiltersChange,
  selectedUserId,
  targetUser,
  onUserSelect,
  onSelectedUserSync,
}) {
  const { showSessionExpired } = useAuthSession();
  const [result, setResult] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const filterStatus = userListFilters.status || "";
  const filterQ = userListFilters.q || "";
  const filterSort = userListFilters.sort || "created_at";
  const filterPage = Number(userListFilters.page) || 1;
  const filterSize = Number(userListFilters.size) || INVESTIGATION_USER_LIST_PAGE_SIZE;

  const [draftFilters, setDraftFilters] = useState({
    status: filterStatus,
    q: filterQ,
    sort: filterSort,
  });

  useEffect(() => {
    setDraftFilters({
      status: filterStatus,
      q: filterQ,
      sort: filterSort,
    });
  }, [filterStatus, filterQ, filterSort]);

  const fetchList = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await getUsersForInvestigation({
        status: filterStatus || undefined,
        q: filterQ || undefined,
        sort: filterSort,
        page: filterPage,
        size: filterSize,
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
  }, [filterStatus, filterQ, filterSort, filterPage, filterSize, showSessionExpired]);

  useEffect(() => {
    fetchList();
  }, [fetchList]);

  useEffect(() => {
    if (!selectedUserId || !result?.items?.length || !onSelectedUserSync) return;
    if (targetUser?.user_id === selectedUserId) return;

    const row = result.items.find((item) => item.id === selectedUserId);
    if (!row) return;

    onSelectedUserSync(mapAdminUserListItemToInvestigationTarget(row));
  }, [selectedUserId, targetUser?.user_id, result?.items, onSelectedUserSync]);

  const handleApplyFilters = (event) => {
    event.preventDefault();
    onFiltersChange?.({
      ...draftFilters,
      page: 1,
      size: INVESTIGATION_USER_LIST_PAGE_SIZE,
    });
  };

  const handleClearFilters = () => {
    const cleared = {
      status: "",
      q: "",
      sort: "created_at",
      page: 1,
      size: INVESTIGATION_USER_LIST_PAGE_SIZE,
    };
    setDraftFilters(cleared);
    onFiltersChange?.(cleared);
  };

  const pagination = result?.pagination;
  const currentPage = filterPage || pagination?.page || 1;
  const totalPages = pagination?.total_pages || 1;
  const items = result?.items || [];

  const handlePageChange = (nextPage) => {
    onFiltersChange?.({
      ...userListFilters,
      page: nextPage,
      size: INVESTIGATION_USER_LIST_PAGE_SIZE,
    });
  };

  const handleRowSelect = (row) => {
    onUserSelect?.(row.id, mapAdminUserListItemToInvestigationTarget(row));
  };

  return (
    <InvestigationUserListView
      status={status}
      errorMessage={errorMessage}
      draftFilters={draftFilters}
      statusOptions={INVESTIGATION_USER_LIST_STATUS_OPTIONS}
      sortOptions={INVESTIGATION_USER_LIST_SORT_OPTIONS}
      items={items}
      pagination={pagination}
      currentPage={currentPage}
      totalPages={totalPages}
      activeSortLabel={getSortColumnLabel(filterSort)}
      selectedUserId={selectedUserId}
      onDraftChange={(patch) => setDraftFilters((prev) => ({ ...prev, ...patch }))}
      onApplyFilters={handleApplyFilters}
      onClearFilters={handleClearFilters}
      onPageChange={handlePageChange}
      onUserSelect={handleRowSelect}
      onRetry={fetchList}
    />
  );
}
