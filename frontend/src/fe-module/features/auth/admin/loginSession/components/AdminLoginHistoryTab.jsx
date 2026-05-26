import { useCallback, useEffect, useState } from "react";
import { getAdminLoginHistory } from "../../../api/authApi.js";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import {
  AccountCard,
  AccountSkeleton,
  PrimaryButton,
  TabPanelHeader,
} from "../../../../../shared/ui/auth/authUi.jsx";
import { EmptyState, ErrorState } from "../../../../../shared/ui/PageState.jsx";

const PAGE_LIMIT = 20;

const LOGIN_METHOD_LABELS = {
  EMAIL: "Email",
  GOOGLE: "Google",
  FACEBOOK: "Facebook",
};

function HistoryRow({ item }) {
  const success = Boolean(item.success);

  return (
    <li className="flex flex-col gap-3 border-b border-outline-variant/50 py-4 last:border-0 sm:flex-row sm:items-center sm:justify-between">
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-2">
          <span className="font-medium text-on-surface">
            {LOGIN_METHOD_LABELS[item.login_method] || item.login_method || "Dang nhap"}
          </span>
          <span
            className={[
              "inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold",
              success ? "bg-green-50 text-green-800" : "bg-red-50 text-red-800",
            ].join(" ")}
          >
            {success ? "Thanh cong" : "That bai"}
          </span>
        </div>
        <p className="mt-1 text-sm text-on-surface-variant">{formatDateTime(item.created_at)}</p>
        <p className="mt-1 text-sm text-on-surface-variant">IP: {item.ip_address || "—"}</p>
        {item.user_agent ? (
          <p className="mt-1 break-all text-xs text-on-surface-variant">{item.user_agent}</p>
        ) : null}
      </div>
    </li>
  );
}

const EMPTY_USER_MESSAGE = "Vui long chon nguoi dung de xem du lieu.";

export function AdminLoginHistoryTab({ userId, onNotify }) {
  const { showSessionExpired } = useAuthSession();
  const [items, setItems] = useState([]);
  const [page, setPage] = useState(1);
  const [hasNext, setHasNext] = useState(false);
  const [status, setStatus] = useState("idle");
  const [loadMoreStatus, setLoadMoreStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const [successDraft, setSuccessDraft] = useState("");
  const [fromDraft, setFromDraft] = useState("");
  const [toDraft, setToDraft] = useState("");
  const [appliedFilters, setAppliedFilters] = useState({ success: "", from: "", to: "" });

  const fetchPage = useCallback(
    async (targetPage, append = false, filters = appliedFilters) => {
      if (!userId) return;

      if (!append) {
        setStatus("loading");
      } else {
        setLoadMoreStatus("loading");
      }
      setErrorMessage("");

      const params = { page: targetPage, limit: PAGE_LIMIT };
      if (filters.success === "true" || filters.success === "false") {
        params.success = filters.success;
      }
      if (filters.from) params.from = filters.from;
      if (filters.to) params.to = filters.to;

      try {
        const data = await getAdminLoginHistory(userId, params);
        const nextItems = data?.items || [];
        const pagination = data?.pagination || {};

        setItems((prev) => (append ? [...prev, ...nextItems] : nextItems));
        setPage(pagination.page ?? targetPage);

        const totalPages = pagination.total_pages;
        const nextHasNext =
          pagination.has_next === true ||
          (typeof totalPages === "number" && targetPage < totalPages);
        setHasNext(nextHasNext);
        setStatus("ready");
      } catch (error) {
        if (error?.code === 401) {
          showSessionExpired(error?.message);
          return;
        }
        if (error?.code === 403) {
          onNotify?.({ variant: "error", message: error?.message || "Ban khong co quyen truy cap." });
        }
        if (!append) {
          setStatus("error");
        }
        setErrorMessage(error?.message || "Khong tai duoc lich su dang nhap.");
      } finally {
        setLoadMoreStatus("idle");
      }
    },
    [userId, appliedFilters, showSessionExpired, onNotify]
  );

  useEffect(() => {
    if (!userId) {
      setItems([]);
      setPage(1);
      setHasNext(false);
      setStatus("idle");
      setErrorMessage("");
      return;
    }
    fetchPage(1, false, appliedFilters);
  }, [userId, appliedFilters, fetchPage]);

  const onApplyFilters = () => {
    setAppliedFilters({
      success: successDraft,
      from: fromDraft,
      to: toDraft,
    });
  };

  const onClearFilters = () => {
    setSuccessDraft("");
    setFromDraft("");
    setToDraft("");
    setAppliedFilters({ success: "", from: "", to: "" });
  };

  const onLoadMore = () => {
    if (loadMoreStatus === "loading" || !hasNext) return;
    fetchPage(page + 1, true);
  };

  if (!userId) {
    return (
      <div>
        <TabPanelHeader
          title="Lich su dang nhap"
          subtitle="Xem lich su dang nhap cua nguoi dung duoc chon."
        />
        <EmptyState message={EMPTY_USER_MESSAGE} />
      </div>
    );
  }

  if (status === "loading") {
    return (
      <div>
        <TabPanelHeader
          title="Lich su dang nhap"
          subtitle="Xem lich su dang nhap cua nguoi dung duoc chon."
        />
        <AccountSkeleton />
      </div>
    );
  }

  if (status === "error") {
    return (
      <div>
        <TabPanelHeader
          title="Lich su dang nhap"
          subtitle="Xem lich su dang nhap cua nguoi dung duoc chon."
        />
        <AccountCard className="border-error/30">
          <ErrorState message={errorMessage} />
          <button
            type="button"
            onClick={() => fetchPage(1, false)}
            className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white hover:opacity-90"
          >
            Thu lai
          </button>
        </AccountCard>
      </div>
    );
  }

  return (
    <div>
      <TabPanelHeader
        title="Lich su dang nhap"
        subtitle="Xem lich su dang nhap cua nguoi dung duoc chon."
      />

      <AccountCard className="mb-6">
        <p className="mb-3 text-sm font-semibold text-on-surface">Bo loc</p>
        <div className="grid gap-4 sm:grid-cols-3">
          <div>
            <label htmlFor="history-success" className="mb-1 block text-xs text-on-surface-variant">
              Ket qua
            </label>
            <select
              id="history-success"
              value={successDraft}
              onChange={(e) => setSuccessDraft(e.target.value)}
              className="w-full rounded-lg border border-outline-variant px-3 py-2 text-sm outline-none focus:border-primary"
            >
              <option value="">Tat ca</option>
              <option value="true">Thanh cong</option>
              <option value="false">That bai</option>
            </select>
          </div>
          <div>
            <label htmlFor="history-from" className="mb-1 block text-xs text-on-surface-variant">
              Tu ngay
            </label>
            <input
              id="history-from"
              type="datetime-local"
              value={fromDraft}
              onChange={(e) => setFromDraft(e.target.value)}
              className="w-full rounded-lg border border-outline-variant px-3 py-2 text-sm outline-none focus:border-primary"
            />
          </div>
          <div>
            <label htmlFor="history-to" className="mb-1 block text-xs text-on-surface-variant">
              Den ngay
            </label>
            <input
              id="history-to"
              type="datetime-local"
              value={toDraft}
              onChange={(e) => setToDraft(e.target.value)}
              className="w-full rounded-lg border border-outline-variant px-3 py-2 text-sm outline-none focus:border-primary"
            />
          </div>
        </div>
        <div className="mt-4 flex flex-wrap gap-2">
          <button
            type="button"
            onClick={onApplyFilters}
            className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white hover:opacity-90"
          >
            Ap dung
          </button>
          <button
            type="button"
            onClick={onClearFilters}
            className="rounded-lg border border-outline-variant px-4 py-2 text-sm font-medium text-on-surface hover:bg-account-surface-low"
          >
            Xoa loc
          </button>
        </div>
      </AccountCard>

      {items.length === 0 ? (
        <EmptyState message="Chua co lich su dang nhap." />
      ) : (
        <AccountCard>
          <ul>
            {items.map((item, index) => (
              <HistoryRow
                key={`${item.created_at}-${item.ip_address}-${index}`}
                item={item}
              />
            ))}
          </ul>

          {hasNext ? (
            <div className="mt-6 flex justify-center border-t border-outline-variant pt-6">
              <PrimaryButton
                type="button"
                onClick={onLoadMore}
                loading={loadMoreStatus === "loading"}
                className="!min-w-[160px]"
              >
                Tai them
              </PrimaryButton>
            </div>
          ) : null}
        </AccountCard>
      )}

      {errorMessage && status === "ready" ? (
        <p className="mt-4 text-sm text-error">{errorMessage}</p>
      ) : null}
    </div>
  );
}
