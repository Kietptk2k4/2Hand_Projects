import { useCallback, useEffect, useState } from "react";
import { getInvestigationUserSessions } from "../../api/userInvestigationApi.js";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import { formatDateTime } from "../../../../security/utils/formatDateTime.js";
import {
  AccountCard,
  AccountSkeleton,
  PrimaryButton,
  TabPanelHeader,
} from "../../../../../../shared/ui/auth/authUi.jsx";
import { EmptyState, ErrorState } from "../../../../../../shared/ui/PageState.jsx";
import { getSessionStatusLabel } from "../../../../constants/authUiStrings";
import {
  INVESTIGATION_SESSIONS_SUBTITLE,
  INVESTIGATION_SESSIONS_TITLE,
} from "../../constants/userInvestigationUiStrings.js";
import { INVESTIGATION_PERMISSIONS } from "../../constants/investigationPermissions.js";
import { useInvestigationPermissions } from "../../hooks/useInvestigationPermissions.js";
import { getInvestigationUserLabel } from "../../utils/investigationUserLabel.js";
import {
  handleInvestigationLoadError,
  resolveInvestigationForbiddenMessage,
} from "../../utils/investigationTabErrors.js";
import { isAdminInvestigationTarget } from "../../utils/investigationTarget.js";
import { RevokeAdminSessionModal } from "../modals/RevokeAdminSessionModal.jsx";
import { InvestigationEmptyState } from "../InvestigationEmptyState.jsx";
import { InvestigationForbiddenState } from "../InvestigationForbiddenState.jsx";
import { InvestigationPermissionNotice } from "../InvestigationPermissionNotice.jsx";

const PAGE_LIMIT = 20;

const STATUS_OPTIONS = [
  { value: "ACTIVE", label: "Đang hoạt động" },
  { value: "LOGGED_OUT", label: "Đã đăng xuất" },
  { value: "REVOKED", label: "Đã thu hồi" },
  { value: "EXPIRED", label: "Hết hạn" },
  { value: "ALL", label: "Tất cả" },
];

const STATUS_BADGE_CLASS = {
  ACTIVE: "bg-green-50 text-green-800",
  LOGGED_OUT: "bg-gray-100 text-gray-800",
  REVOKED: "bg-red-50 text-red-800",
  EXPIRED: "bg-amber-50 text-amber-900",
};

function SessionCard({ session, canRevokeSession, onRevokeSession }) {
  const status = session.status || "ACTIVE";
  const badgeClass = STATUS_BADGE_CLASS[status] || "bg-gray-100 text-gray-800";
  const showRevoke = canRevokeSession && status === "ACTIVE";

  return (
    <li className="rounded-lg border border-outline-variant bg-account-surface-low p-4">
      <div className="flex flex-wrap items-start justify-between gap-2">
        <div className="min-w-0">
          <p className="font-medium text-on-surface">{session.device_id || "Thiết bị không xác định"}</p>
          <p className="mt-1 break-all text-xs text-on-surface-variant">
            Mã phiên: {session.session_id || "—"}
          </p>
        </div>
        <span className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${badgeClass}`}>
          {getSessionStatusLabel(status)}
        </span>
      </div>
      <dl className="mt-3 grid gap-2 text-sm sm:grid-cols-2">
        <div>
          <dt className="text-on-surface-variant">IP</dt>
          <dd className="text-on-surface">{session.ip_address || "—"}</dd>
        </div>
        <div>
          <dt className="text-on-surface-variant">Tạo lúc</dt>
          <dd className="text-on-surface">{formatDateTime(session.created_at)}</dd>
        </div>
        <div>
          <dt className="text-on-surface-variant">Cập nhật</dt>
          <dd className="text-on-surface">{formatDateTime(session.updated_at)}</dd>
        </div>
        <div className="sm:col-span-2">
          <dt className="text-on-surface-variant">Trình duyệt / ứng dụng</dt>
          <dd className="break-all text-on-surface">{session.user_agent || "—"}</dd>
        </div>
      </dl>
      {showRevoke ? (
        <div className="mt-4 flex justify-end border-t border-outline-variant/50 pt-3">
          <button
            type="button"
            onClick={() => onRevokeSession?.(session)}
            className="rounded-md px-3 py-1.5 text-sm font-medium text-error transition-colors hover:bg-error-container hover:text-on-error-container"
          >
            Thu hồi phiên admin
          </button>
        </div>
      ) : null}
    </li>
  );
}

export function InvestigationUserSessionsTab({ userId, targetUser, onNotify }) {
  const { showSessionExpired } = useAuthSession();
  const { canReadProfile, canRevokeAdminSession } = useInvestigationPermissions();
  const [sessions, setSessions] = useState([]);
  const [revokeSessionTarget, setRevokeSessionTarget] = useState(null);
  const [page, setPage] = useState(1);
  const [hasNext, setHasNext] = useState(false);
  const [statusFilter, setStatusFilter] = useState("ACTIVE");
  const [status, setStatus] = useState("idle");
  const [loadMoreStatus, setLoadMoreStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const fetchPage = useCallback(
    async (targetPage, append = false, filterStatus = statusFilter) => {
      if (!userId) return;

      if (!append) {
        setStatus("loading");
      } else {
        setLoadMoreStatus("loading");
      }
      setErrorMessage("");

      try {
        const data = await getInvestigationUserSessions(userId, {
          page: targetPage,
          limit: PAGE_LIMIT,
          status: filterStatus,
        });
        const nextSessions = data?.sessions || [];
        const pagination = data?.pagination || {};

        setSessions((prev) => (append ? [...prev, ...nextSessions] : nextSessions));
        setPage(pagination.page ?? targetPage);
        setHasNext(pagination.has_next === true);
        setStatus("ready");
      } catch (error) {
        handleInvestigationLoadError(error, {
          showSessionExpired,
          setStatus,
          setErrorMessage,
          permissionCode: INVESTIGATION_PERMISSIONS.READ_PROFILE,
          actionLabel: "xem phiên đăng nhập",
          fallbackMessage: "Không tải được danh sách phiên đăng nhập.",
          preserveStatusOnForbidden: append,
          preserveStatusOnError: append,
        });
      } finally {
        setLoadMoreStatus("idle");
      }
    },
    [userId, statusFilter, showSessionExpired]
  );

  useEffect(() => {
    if (!userId) {
      setSessions([]);
      setPage(1);
      setHasNext(false);
      setStatus("idle");
      setErrorMessage("");
      return;
    }
    fetchPage(1, false, statusFilter);
  }, [userId, statusFilter, fetchPage]);

  const onLoadMore = () => {
    if (loadMoreStatus === "loading" || !hasNext) return;
    fetchPage(page + 1, true);
  };

  const handleApiError = (error) => {
    if (error?.code === 401) {
      showSessionExpired(error?.message);
      return;
    }
    if (error?.code === 403) {
      onNotify?.({
        variant: "error",
        message: resolveInvestigationForbiddenMessage(error, {
          actionLabel: "thực hiện thao tác này",
        }),
      });
      return;
    }
    onNotify?.({ variant: "error", message: error?.message || "Có lỗi xảy ra." });
  };

  const isAdminTarget = isAdminInvestigationTarget(targetUser);
  const canRevokeSession = canRevokeAdminSession && isAdminTarget;
  const userLabel = getInvestigationUserLabel(userId, null, targetUser);

  if (!userId) {
    return (
      <div>
        <TabPanelHeader
          title={INVESTIGATION_SESSIONS_TITLE}
          subtitle={INVESTIGATION_SESSIONS_SUBTITLE}
        />
        <InvestigationEmptyState />
      </div>
    );
  }

  if (status === "loading") {
    return (
      <div>
        <TabPanelHeader
          title={INVESTIGATION_SESSIONS_TITLE}
          subtitle={INVESTIGATION_SESSIONS_SUBTITLE}
        />
        <AccountSkeleton />
      </div>
    );
  }

  if (status === "forbidden") {
    return (
      <div>
        <TabPanelHeader
          title={INVESTIGATION_SESSIONS_TITLE}
          subtitle={INVESTIGATION_SESSIONS_SUBTITLE}
        />
        <InvestigationForbiddenState message={errorMessage} />
      </div>
    );
  }

  if (status === "error") {
    return (
      <div>
        <TabPanelHeader
          title={INVESTIGATION_SESSIONS_TITLE}
          subtitle={INVESTIGATION_SESSIONS_SUBTITLE}
        />
        <AccountCard className="border-error/30 bg-error-container/30">
          <ErrorState message={errorMessage} />
          <p className="mt-2 text-sm text-on-surface-variant">
            Hệ thống tạm thời không phản hồi. Vui lòng thử lại sau vài phút.
          </p>
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
        title={INVESTIGATION_SESSIONS_TITLE}
        subtitle={INVESTIGATION_SESSIONS_SUBTITLE}
      />

      {!canReadProfile ? (
        <InvestigationPermissionNotice message="Tài khoản của bạn thiếu quyền USER_INVESTIGATION_READ để xem phiên người dùng." />
      ) : null}

      {isAdminTarget && !canRevokeAdminSession ? (
        <InvestigationPermissionNotice message="Thiếu quyền ADMIN_SESSION_REVOKE để thu hồi phiên admin." />
      ) : null}

      <AccountCard className="mb-6">
        <label htmlFor="session-status" className="mb-1 block text-xs font-semibold text-on-surface">
          Trạng thái phiên
        </label>
        <select
          id="session-status"
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="w-full max-w-xs rounded-lg border border-outline-variant px-3 py-2 text-sm outline-none focus:border-primary"
        >
          {STATUS_OPTIONS.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>
      </AccountCard>

      {sessions.length === 0 ? (
        <EmptyState message="Không có phiên đăng nhập phù hợp bộ lọc." />
      ) : (
        <AccountCard className="!p-0">
          <ul className="divide-y divide-outline-variant/50 p-2 sm:p-4">
            {sessions.map((session) => (
              <SessionCard
                key={session.session_id}
                session={session}
                canRevokeSession={canRevokeSession}
                onRevokeSession={setRevokeSessionTarget}
              />
            ))}
          </ul>

          {hasNext ? (
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
        <p className="mt-4 text-sm text-error">{errorMessage}</p>
      ) : null}

      <RevokeAdminSessionModal
        open={Boolean(revokeSessionTarget)}
        session={revokeSessionTarget}
        userLabel={userLabel}
        onClose={() => setRevokeSessionTarget(null)}
        onSuccess={(result) => {
          const count = result?.revoked_session_count ?? 1;
          onNotify?.({
            variant: "success",
            message: `Đã thu hồi ${count} phiên admin.`,
          });
          fetchPage(1, false, statusFilter);
        }}
        onError={handleApiError}
      />
    </div>
  );
}
