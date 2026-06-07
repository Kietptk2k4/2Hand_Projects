import { useCallback, useEffect, useState } from "react";
import { getEnforcementHistory } from "../../api/userInvestigationApi.js";
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
  INVESTIGATION_ENFORCEMENT_HISTORY_SUBTITLE,
  INVESTIGATION_ENFORCEMENT_HISTORY_TITLE,
} from "../../constants/userInvestigationUiStrings.js";
import { getActorTypeLabel } from "../../utils/investigationLabels.js";
import {
  EnforcementActionBadge,
  EnforcementStatusBadge,
} from "../EnforcementBadges.jsx";
import { INVESTIGATION_PERMISSIONS } from "../../constants/investigationPermissions.js";
import { useInvestigationPermissions } from "../../hooks/useInvestigationPermissions.js";
import { handleInvestigationLoadError } from "../../utils/investigationTabErrors.js";
import { InvestigationEmptyState } from "../InvestigationEmptyState.jsx";
import { InvestigationForbiddenState } from "../InvestigationForbiddenState.jsx";
import { InvestigationPermissionNotice } from "../InvestigationPermissionNotice.jsx";

const PAGE_SIZE = 20;

function EnforcementLogTimeline({ logs = [] }) {
  if (logs.length === 0) {
    return <p className="text-sm text-on-surface-variant">Không có log chuyển trạng thái.</p>;
  }

  return (
    <div className="relative ml-2 space-y-4 border-l-2 border-outline-variant pl-4">
      {logs.map((log) => (
        <div key={log.log_id || `${log.created_at}-${log.new_status}`} className="relative">
          <span className="absolute -left-[21px] top-1.5 h-3 w-3 rounded-full border-2 border-primary bg-surface" />
          <p className="text-sm font-medium text-on-surface">
            {log.old_status ? `${log.old_status} → ${log.new_status}` : log.new_status}
          </p>
          <p className="mt-1 text-xs text-on-surface-variant">
            {formatDateTime(log.created_at)} · {getActorTypeLabel(log.actor_type)}
            {log.admin_id ? ` · ${log.admin_id.slice(0, 8)}…` : ""}
          </p>
          {log.note ? <p className="mt-1 text-sm text-on-surface-variant">{log.note}</p> : null}
        </div>
      ))}
    </div>
  );
}

function HistoryRow({ item, expanded, onToggle }) {
  return (
    <>
      <tr
        className="cursor-pointer hover:bg-surface-container-low/50"
        onClick={onToggle}
      >
        <td className="px-4 py-4 text-center text-on-surface-variant sm:px-6">
          <span className="inline-block transition-transform" style={{ transform: expanded ? "rotate(180deg)" : "" }}>
            ▼
          </span>
        </td>
        <td className="px-4 py-4 sm:px-6">
          <div className="font-mono text-xs text-on-surface">{item.enforcement_id?.slice(0, 12)}…</div>
          <div className="mt-1 text-xs text-on-surface-variant">
            Admin: {item.enforced_by?.slice(0, 8) || "—"}…
          </div>
        </td>
        <td className="px-4 py-4 sm:px-6">
          <EnforcementActionBadge actionType={item.action_type} />
        </td>
        <td className="max-w-[200px] px-4 py-4 sm:px-6">
          <div className="truncate text-sm font-medium text-on-surface">{item.reason_code}</div>
          <div className="mt-1 truncate text-xs text-on-surface-variant">{item.description}</div>
        </td>
        <td className="px-4 py-4 sm:px-6">
          <EnforcementStatusBadge status={item.status} />
        </td>
        <td className="px-4 py-4 text-sm text-on-surface sm:px-6">
          {formatDateTime(item.updated_at || item.created_at)}
        </td>
      </tr>
      {expanded ? (
        <tr className="bg-surface-container-lowest">
          <td colSpan={6} className="px-4 py-4 sm:px-6">
            <h4 className="mb-3 text-sm font-semibold text-on-surface">Dòng thời gian audit</h4>
            <EnforcementLogTimeline logs={item.logs || []} />
          </td>
        </tr>
      ) : null}
    </>
  );
}

export function InvestigationEnforcementHistoryTab({ userId, onNotify }) {
  const { showSessionExpired } = useAuthSession();
  const { canReadEnforcement } = useInvestigationPermissions();
  const [items, setItems] = useState([]);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [expandedId, setExpandedId] = useState(null);
  const [status, setStatus] = useState("idle");
  const [loadMoreStatus, setLoadMoreStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const fetchPage = useCallback(
    async (targetPage, append = false) => {
      if (!userId) return;

      if (!append) {
        setStatus("loading");
      } else {
        setLoadMoreStatus("loading");
      }
      setErrorMessage("");

      try {
        const data = await getEnforcementHistory(userId, { page: targetPage, size: PAGE_SIZE });
        const nextItems = data?.enforcements || [];
        setItems((prev) => (append ? [...prev, ...nextItems] : nextItems));
        setPage(data?.page ?? targetPage);
        setTotalPages(data?.total_pages ?? 1);
        setStatus("ready");
      } catch (error) {
        handleInvestigationLoadError(error, {
          showSessionExpired,
          setStatus,
          setErrorMessage,
          permissionCode: INVESTIGATION_PERMISSIONS.READ_ENFORCEMENT,
          actionLabel: "xem lịch sử enforcement",
          fallbackMessage: "Không tải được lịch sử enforcement.",
          preserveStatusOnForbidden: append,
          preserveStatusOnError: append,
        });
      } finally {
        setLoadMoreStatus("idle");
      }
    },
    [userId, showSessionExpired],
  );

  useEffect(() => {
    if (!userId) {
      setItems([]);
      setPage(1);
      setTotalPages(1);
      setExpandedId(null);
      setStatus("idle");
      setErrorMessage("");
      return;
    }
    fetchPage(1, false);
  }, [userId, fetchPage]);

  const onLoadMore = () => {
    if (loadMoreStatus === "loading" || page >= totalPages) return;
    fetchPage(page + 1, true);
  };

  if (!userId) {
    return (
      <div>
        <TabPanelHeader
          title={INVESTIGATION_ENFORCEMENT_HISTORY_TITLE}
          subtitle={INVESTIGATION_ENFORCEMENT_HISTORY_SUBTITLE}
        />
        <InvestigationEmptyState />
      </div>
    );
  }

  if (status === "loading") {
    return (
      <div>
        <TabPanelHeader
          title={INVESTIGATION_ENFORCEMENT_HISTORY_TITLE}
          subtitle={INVESTIGATION_ENFORCEMENT_HISTORY_SUBTITLE}
        />
        <AccountSkeleton />
      </div>
    );
  }

  if (status === "forbidden") {
    return (
      <div>
        <TabPanelHeader
          title={INVESTIGATION_ENFORCEMENT_HISTORY_TITLE}
          subtitle={INVESTIGATION_ENFORCEMENT_HISTORY_SUBTITLE}
        />
        <InvestigationForbiddenState message={errorMessage} />
      </div>
    );
  }

  if (status === "error") {
    return (
      <div>
        <TabPanelHeader
          title={INVESTIGATION_ENFORCEMENT_HISTORY_TITLE}
          subtitle={INVESTIGATION_ENFORCEMENT_HISTORY_SUBTITLE}
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
    <div className="space-y-6">
      <header className="mb-8">
        <h1 className="text-2xl font-semibold text-on-surface md:text-3xl">
          {INVESTIGATION_ENFORCEMENT_HISTORY_TITLE}
        </h1>
        <p className="mt-2 text-base text-on-surface-variant">
          {INVESTIGATION_ENFORCEMENT_HISTORY_SUBTITLE}{" "}
          <span className="font-mono text-sm">userId: {userId}</span>
        </p>
      </header>

      {!canReadEnforcement ? (
        <InvestigationPermissionNotice message="Tài khoản của bạn thiếu quyền USER_ENFORCEMENT_READ để xem lịch sử enforcement." />
      ) : null}

      {items.length === 0 ? (
        <EmptyState message="Chưa có lịch sử enforcement." />
      ) : (
        <AccountCard className="!p-0 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full min-w-[900px] border-collapse text-left text-sm">
              <thead>
                <tr className="border-b border-outline-variant bg-surface-container-low text-xs font-semibold uppercase tracking-wide text-on-surface-variant">
                  <th className="w-12 px-4 py-3 sm:px-6" />
                  <th className="px-4 py-3 sm:px-6">ID thực thi</th>
                  <th className="px-4 py-3 sm:px-6">Loại</th>
                  <th className="px-4 py-3 sm:px-6">Lý do</th>
                  <th className="px-4 py-3 sm:px-6">Trạng thái</th>
                  <th className="px-4 py-3 sm:px-6">Cập nhật lúc</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-outline-variant/50">
                {items.map((item) => (
                  <HistoryRow
                    key={item.enforcement_id}
                    item={item}
                    expanded={expandedId === item.enforcement_id}
                    onToggle={() =>
                      setExpandedId((prev) =>
                        prev === item.enforcement_id ? null : item.enforcement_id,
                      )
                    }
                  />
                ))}
              </tbody>
            </table>
          </div>

          {page < totalPages ? (
            <div className="flex justify-center border-t border-outline-variant px-4 py-6">
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
        <p className="text-sm text-error">{errorMessage}</p>
      ) : null}
    </div>
  );
}
