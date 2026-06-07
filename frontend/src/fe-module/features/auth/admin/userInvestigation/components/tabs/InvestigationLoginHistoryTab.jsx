import { useCallback, useEffect, useState } from "react";
import { getInvestigationLoginHistory } from "../../api/userInvestigationApi.js";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import { formatDateTime } from "../../../../security/utils/formatDateTime.js";
import {
  AccountCard,
  AccountSkeleton,
  PrimaryButton,
  TabPanelHeader,
} from "../../../../../../shared/ui/auth/authUi.jsx";
import { EmptyState, ErrorState } from "../../../../../../shared/ui/PageState.jsx";
import {
  INVESTIGATION_LOGIN_HISTORY_SUBTITLE,
  INVESTIGATION_LOGIN_HISTORY_TITLE,
} from "../../constants/userInvestigationUiStrings.js";
import { INVESTIGATION_PERMISSIONS } from "../../constants/investigationPermissions.js";
import { useInvestigationPermissions } from "../../hooks/useInvestigationPermissions.js";
import { handleInvestigationLoadError } from "../../utils/investigationTabErrors.js";
import { InvestigationEmptyState } from "../InvestigationEmptyState.jsx";
import { InvestigationForbiddenState } from "../InvestigationForbiddenState.jsx";
import { InvestigationPermissionNotice } from "../InvestigationPermissionNotice.jsx";

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
            {LOGIN_METHOD_LABELS[item.login_method] || item.login_method || "Đăng nhập"}
          </span>
          <span
            className={[
              "inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold",
              success ? "bg-green-50 text-green-800" : "bg-red-50 text-red-800",
            ].join(" ")}
          >
            {success ? "Thành công" : "Thất bại"}
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

export function InvestigationLoginHistoryTab({ userId, onNotify }) {
  const { showSessionExpired } = useAuthSession();
  const { canReadProfile } = useInvestigationPermissions();
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
        const data = await getInvestigationLoginHistory(userId, params);
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
        handleInvestigationLoadError(error, {
          showSessionExpired,
          setStatus,
          setErrorMessage,
          permissionCode: INVESTIGATION_PERMISSIONS.READ_PROFILE,
          actionLabel: "xem lịch sử đăng nhập",
          fallbackMessage: "Không tải được lịch sử đăng nhập.",
          preserveStatusOnForbidden: append,
          preserveStatusOnError: append,
        });
      } finally {
        setLoadMoreStatus("idle");
      }
    },
    [userId, appliedFilters, showSessionExpired]
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
          title={INVESTIGATION_LOGIN_HISTORY_TITLE}
          subtitle={INVESTIGATION_LOGIN_HISTORY_SUBTITLE}
        />
        <InvestigationEmptyState />
      </div>
    );
  }

  if (status === "loading") {
    return (
      <div>
        <TabPanelHeader
          title={INVESTIGATION_LOGIN_HISTORY_TITLE}
          subtitle={INVESTIGATION_LOGIN_HISTORY_SUBTITLE}
        />
        <AccountSkeleton />
      </div>
    );
  }

  if (status === "forbidden") {
    return (
      <div>
        <TabPanelHeader
          title={INVESTIGATION_LOGIN_HISTORY_TITLE}
          subtitle={INVESTIGATION_LOGIN_HISTORY_SUBTITLE}
        />
        <InvestigationForbiddenState message={errorMessage} />
      </div>
    );
  }

  if (status === "error") {
    return (
      <div>
        <TabPanelHeader
          title={INVESTIGATION_LOGIN_HISTORY_TITLE}
          subtitle={INVESTIGATION_LOGIN_HISTORY_SUBTITLE}
        />
        <AccountCard className="border-error/30">
          <ErrorState message={errorMessage} />
          <button
            type="button"
            onClick={() => fetchPage(1, false)}
            className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white hover:opacity-90"
          >
            Thử lại
          </button>
        </AccountCard>
      </div>
    );
  }

  return (
    <div>
      <TabPanelHeader
        title={INVESTIGATION_LOGIN_HISTORY_TITLE}
        subtitle={INVESTIGATION_LOGIN_HISTORY_SUBTITLE}
      />

      {!canReadProfile ? (
        <InvestigationPermissionNotice message="Tài khoản của bạn thiếu quyền USER_INVESTIGATION_READ để xem lịch sử đăng nhập." />
      ) : null}

      <AccountCard className="mb-6">
        <p className="mb-3 text-sm font-semibold text-on-surface">Bộ lọc</p>
        <div className="grid gap-4 sm:grid-cols-3">
          <div>
            <label htmlFor="history-success" className="mb-1 block text-xs text-on-surface-variant">
              Kết quả
            </label>
            <select
              id="history-success"
              value={successDraft}
              onChange={(e) => setSuccessDraft(e.target.value)}
              className="w-full rounded-lg border border-outline-variant px-3 py-2 text-sm outline-none focus:border-primary"
            >
              <option value="">Tất cả</option>
              <option value="true">Thành công</option>
              <option value="false">Thất bại</option>
            </select>
          </div>
          <div>
            <label htmlFor="history-from" className="mb-1 block text-xs text-on-surface-variant">
              Từ ngày
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
              Đến ngày
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
            Áp dụng
          </button>
          <button
            type="button"
            onClick={onClearFilters}
            className="rounded-lg border border-outline-variant px-4 py-2 text-sm font-medium text-on-surface hover:bg-account-surface-low"
          >
            Xóa lọc
          </button>
        </div>
      </AccountCard>

      {items.length === 0 ? (
        <EmptyState message="Chưa có lịch sử đăng nhập." />
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
                Tải thêm
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
