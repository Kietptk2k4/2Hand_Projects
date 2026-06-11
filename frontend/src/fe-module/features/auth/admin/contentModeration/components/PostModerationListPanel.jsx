import { useCallback, useEffect, useState } from "react";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { AccountCard, AccountSkeleton } from "../../../../../shared/ui/auth/authUi.jsx";
import { ErrorState } from "../../../../../shared/ui/PageState.jsx";
import { getPostsForModeration } from "../api/socialModerationListApi.js";
import {
  POST_MODERATION_LIST_MODERATION_STATUS_OPTIONS,
  POST_MODERATION_LIST_PAGE_SIZE,
  POST_MODERATION_LIST_SORT_OPTIONS,
  POST_MODERATION_LIST_STATUS_OPTIONS,
} from "../constants/postModerationListConstants.js";

function sortColumnLabel(sortField) {
  const option = POST_MODERATION_LIST_SORT_OPTIONS.find((item) => item.value === sortField);
  return option?.label || "Ngay tao (moi nhat)";
}

function truncateId(id) {
  if (!id || id.length < 12) return id || "-";
  return `${id.slice(0, 8)}...${id.slice(-4)}`;
}

export function PostModerationListPanel({
  listFilters,
  onFiltersChange,
  selectedPostId,
  onPostSelect,
}) {
  const { showSessionExpired } = useAuthSession();
  const [result, setResult] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const filterStatus = listFilters.status || "";
  const filterModerationStatus = listFilters.moderation_status || "";
  const filterQ = listFilters.q || "";
  const filterSort = listFilters.sort || "created_at";
  const filterPage = Number(listFilters.page) || 1;
  const filterSize = Number(listFilters.size) || POST_MODERATION_LIST_PAGE_SIZE;

  const [draftFilters, setDraftFilters] = useState({
    status: filterStatus,
    moderation_status: filterModerationStatus,
    q: filterQ,
    sort: filterSort,
  });

  useEffect(() => {
    setDraftFilters({
      status: filterStatus,
      moderation_status: filterModerationStatus,
      q: filterQ,
      sort: filterSort,
    });
  }, [filterStatus, filterModerationStatus, filterQ, filterSort]);

  const fetchList = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await getPostsForModeration({
        status: filterStatus || undefined,
        moderation_status: filterModerationStatus || undefined,
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
        setErrorMessage(error?.message || "Ban khong co quyen truy cap.");
        return;
      }
      setStatus("error");
      setErrorMessage(error?.message || "Khong tai duoc danh sach bai viet.");
    }
  }, [
    filterStatus,
    filterModerationStatus,
    filterQ,
    filterSort,
    filterPage,
    filterSize,
    showSessionExpired,
  ]);

  useEffect(() => {
    fetchList();
  }, [fetchList]);

  const handleApplyFilters = (event) => {
    event.preventDefault();
    onFiltersChange?.({
      ...draftFilters,
      page: 1,
      size: POST_MODERATION_LIST_PAGE_SIZE,
    });
  };

  const handleClearFilters = () => {
    const cleared = {
      status: "",
      moderation_status: "",
      q: "",
      sort: "created_at",
      page: 1,
      size: POST_MODERATION_LIST_PAGE_SIZE,
    };
    setDraftFilters(cleared);
    onFiltersChange?.(cleared);
  };

  const pagination = result?.pagination;
  const currentPage = filterPage || pagination?.page || 1;
  const totalPages = pagination?.total_pages || 1;
  const activeSort = filterSort;
  const items = result?.items || [];

  const handlePageChange = (nextPage) => {
    onFiltersChange?.({
      ...listFilters,
      page: nextPage,
      size: POST_MODERATION_LIST_PAGE_SIZE,
    });
  };

  const handleRowSelect = (row) => {
    onPostSelect?.(row.id);
  };

  return (
    <div className="mb-6 space-y-4">
      <AccountCard>
        <form onSubmit={handleApplyFilters} className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <div className="lg:col-span-2">
            <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Tim kiem</label>
            <input
              type="search"
              value={draftFilters.q}
              onChange={(e) => setDraftFilters((prev) => ({ ...prev, q: e.target.value }))}
              placeholder="Post ID hoac noi dung..."
              className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
            />
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Trang thai</label>
            <select
              value={draftFilters.status}
              onChange={(e) => setDraftFilters((prev) => ({ ...prev, status: e.target.value }))}
              className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
            >
              {POST_MODERATION_LIST_STATUS_OPTIONS.map((option) => (
                <option key={option.value || "all"} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Kiem duyet</label>
            <select
              value={draftFilters.moderation_status}
              onChange={(e) =>
                setDraftFilters((prev) => ({ ...prev, moderation_status: e.target.value }))
              }
              className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
            >
              {POST_MODERATION_LIST_MODERATION_STATUS_OPTIONS.map((option) => (
                <option key={option.value || "all-mod"} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Sap xep theo</label>
            <select
              value={draftFilters.sort}
              onChange={(e) => setDraftFilters((prev) => ({ ...prev, sort: e.target.value }))}
              className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
            >
              {POST_MODERATION_LIST_SORT_OPTIONS.map((option) => (
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
              Ap dung
            </button>
            <button
              type="button"
              onClick={handleClearFilters}
              className="rounded-lg border border-outline-variant px-4 py-2 text-sm font-medium text-on-surface-variant hover:bg-surface-container-low"
            >
              Xoa loc
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
            Thu lai
          </button>
        </AccountCard>
      ) : null}

      {status === "ready" ? (
        <AccountCard>
          <div className="mb-4 flex flex-wrap items-center justify-between gap-2">
            <p className="text-sm text-on-surface-variant">
              {pagination?.total_items ?? 0} bai viet · Sap xep: {sortColumnLabel(activeSort)} · Trang{" "}
              {pagination?.page ?? currentPage}/{totalPages}
            </p>
            <div className="flex gap-2">
              <button
                type="button"
                disabled={currentPage <= 1}
                onClick={() => handlePageChange(currentPage - 1)}
                className="rounded-lg border border-outline-variant px-3 py-1.5 text-sm disabled:opacity-40"
              >
                Truoc
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
              <table className="w-full min-w-[880px] text-left text-sm">
                <thead>
                  <tr className="border-b border-outline-variant text-on-surface-variant">
                    <th className="py-2 pr-3 font-medium">Post ID</th>
                    <th className="py-2 pr-3 font-medium">Noi dung</th>
                    <th className="py-2 pr-3 font-medium">Trang thai</th>
                    <th className="py-2 pr-3 font-medium">Kiem duyet</th>
                    <th className="py-2 pr-3 font-medium">Thich</th>
                    <th className="py-2 font-medium">Ngay tao</th>
                  </tr>
                </thead>
                <tbody>
                  {items.map((row) => {
                    const isSelected = selectedPostId === row.id;
                    return (
                      <tr
                        key={row.id}
                        className={`cursor-pointer border-b border-outline-variant/60 align-top hover:bg-surface-container-low ${
                          isSelected ? "bg-primary/5" : ""
                        }`}
                        onClick={() => handleRowSelect(row)}
                      >
                        <td className="py-3 pr-3 font-mono text-xs" title={row.id}>
                          {truncateId(row.id)}
                        </td>
                        <td className="py-3 pr-3 max-w-xs truncate">{row.caption_preview || "-"}</td>
                        <td className="py-3 pr-3">
                          <span className="inline-flex rounded-full bg-account-surface-low px-2 py-0.5 text-xs font-semibold">
                            {row.status}
                          </span>
                        </td>
                        <td className="py-3 pr-3">
                          <span className="inline-flex rounded-full bg-account-surface-low px-2 py-0.5 text-xs font-semibold">
                            {row.moderation_status}
                          </span>
                        </td>
                        <td className="py-3 pr-3">{row.like_count ?? 0}</td>
                        <td className="py-3">{formatDateTime(row.created_at)}</td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-sm text-on-surface-variant">Khong co bai viet phu hop bo loc.</p>
          )}
        </AccountCard>
      ) : null}
    </div>
  );
}
