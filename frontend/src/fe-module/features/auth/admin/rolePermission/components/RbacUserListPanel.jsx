import { useCallback, useEffect, useState } from "react";
import { getUsersForRbac } from "../../../api/authApi";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { AccountCard, AccountSkeleton } from "../../../../../shared/ui/auth/authUi.jsx";
import { ErrorState } from "../../../../../shared/ui/PageState.jsx";
import {
  RBAC_USER_LIST_PAGE_SIZE,
  RBAC_USER_LIST_SORT_OPTIONS,
  RBAC_USER_LIST_STATUS_OPTIONS,
} from "../constants/rbacUserListConstants.js";

function sortColumnLabel(sortField) {
  const option = RBAC_USER_LIST_SORT_OPTIONS.find((item) => item.value === sortField);
  return option?.label || "Email (A-Z)";
}

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
    <div className="space-y-4">
      <AccountCard>
        <form onSubmit={handleApplyFilters} className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <div className="lg:col-span-2">
            <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Tìm kiếm</label>
            <input
              type="search"
              value={draftFilters.q}
              onChange={(e) => setDraftFilters((prev) => ({ ...prev, q: e.target.value }))}
              placeholder="Email hoặc tên hiển thị..."
              className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
            />
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Trạng thái</label>
            <select
              value={draftFilters.status}
              onChange={(e) => setDraftFilters((prev) => ({ ...prev, status: e.target.value }))}
              className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
            >
              {RBAC_USER_LIST_STATUS_OPTIONS.map((option) => (
                <option key={option.value || "all"} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Sắp xếp theo</label>
            <select
              value={draftFilters.sort}
              onChange={(e) => setDraftFilters((prev) => ({ ...prev, sort: e.target.value }))}
              className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
            >
              {RBAC_USER_LIST_SORT_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
          <div className="flex items-end gap-2 md:col-span-2 lg:col-span-4">
            <button
              type="submit"
              className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white hover:opacity-90"
            >
              Áp dụng
            </button>
            <button
              type="button"
              onClick={handleClearFilters}
              className="rounded-lg border border-outline-variant px-4 py-2 text-sm font-medium text-on-surface-variant hover:bg-surface-container-low"
            >
              Xóa lọc
            </button>
          </div>
        </form>
      </AccountCard>

      {status === "loading" ? <AccountSkeleton /> : null}
      {status === "forbidden" ? <ErrorState message={errorMessage} /> : null}

      {status === "error" ? (
        <AccountCard className="border-error/30">
          <ErrorState message={errorMessage} />
          <button
            type="button"
            onClick={fetchList}
            className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white hover:opacity-90"
          >
            Thử lại
          </button>
        </AccountCard>
      ) : null}

      {status === "ready" ? (
        <AccountCard>
          <div className="mb-4 flex flex-wrap items-center justify-between gap-2">
            <p className="text-sm text-on-surface-variant">
              {pagination?.total_items ?? 0} người dùng · Sắp xếp: {sortColumnLabel(activeSort)} · Trang{" "}
              {pagination?.page ?? currentPage}/{totalPages}
            </p>
            <div className="flex gap-2">
              <button
                type="button"
                disabled={currentPage <= 1}
                onClick={() => handlePageChange(currentPage - 1)}
                className="rounded-lg border border-outline-variant px-3 py-1.5 text-sm disabled:opacity-40"
              >
                Trước
              </button>
              <button
                type="button"
                disabled={currentPage >= totalPages}
                onClick={() => handlePageChange(currentPage + 1)}
                className="rounded-lg border border-outline-variant px-3 py-1.5 text-sm disabled:opacity-40"
              >
                Sau
              </button>
            </div>
          </div>

          {items.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="w-full min-w-[720px] text-left text-sm">
                <thead>
                  <tr className="border-b border-outline-variant text-on-surface-variant">
                    <th className="py-2 pr-3 font-medium">Email</th>
                    <th className="py-2 pr-3 font-medium">Tên hiển thị</th>
                    <th className="py-2 pr-3 font-medium">Trạng thái</th>
                    <th className="py-2 pr-3 font-medium">Vai trò</th>
                    <th className="py-2 font-medium">Ngày tạo</th>
                  </tr>
                </thead>
                <tbody>
                  {items.map((row) => {
                    const isSelected = selectedUserId === row.id;
                    return (
                      <tr
                        key={row.id}
                        className={`cursor-pointer border-b border-outline-variant/60 align-top hover:bg-surface-container-low ${
                          isSelected ? "bg-primary/5" : ""
                        }`}
                        onClick={() => onUserSelect?.(row.id, row)}
                      >
                        <td className="py-3 pr-3">{row.email}</td>
                        <td className="py-3 pr-3">{row.display_name || "—"}</td>
                        <td className="py-3 pr-3">
                          <span className="inline-flex rounded-full bg-account-surface-low px-2 py-0.5 text-xs font-semibold">
                            {row.status}
                          </span>
                        </td>
                        <td className="py-3 pr-3">
                          {row.role_codes?.length > 0 ? row.role_codes.join(", ") : "—"}
                        </td>
                        <td className="py-3">{formatDateTime(row.created_at)}</td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-sm text-on-surface-variant">Không có người dùng phù hợp bộ lọc.</p>
          )}
        </AccountCard>
      ) : null}
    </div>
  );
}
