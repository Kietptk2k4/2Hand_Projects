import { useCallback, useEffect, useState } from "react";
import { getInvestigationUserSessions } from "../../api/userInvestigationApi.js";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
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
import { InvestigationUserSessionsTabView } from "./InvestigationUserSessionsTabView.jsx";

const PAGE_LIMIT = 20;

const STATUS_OPTIONS = [
  { value: "ACTIVE", label: "Đang hoạt động" },
  { value: "LOGGED_OUT", label: "Đã đăng xuất" },
  { value: "REVOKED", label: "Đã thu hồi" },
  { value: "EXPIRED", label: "Hết hạn" },
  { value: "ALL", label: "Tất cả" },
];

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
    [userId, statusFilter, showSessionExpired],
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

  const viewStatus = !userId ? "no-user" : status === "loading" ? "loading" : status;

  return (
    <InvestigationUserSessionsTabView
      title={INVESTIGATION_SESSIONS_TITLE}
      subtitle={INVESTIGATION_SESSIONS_SUBTITLE}
      status={viewStatus}
      errorMessage={errorMessage}
      canReadProfile={canReadProfile}
      permissionNotice="Tài khoản của bạn thiếu quyền USER_INVESTIGATION_READ để xem phiên người dùng."
      adminRevokeNotice={
        isAdminTarget && !canRevokeAdminSession
          ? "Thiếu quyền ADMIN_SESSION_REVOKE để thu hồi phiên admin."
          : ""
      }
      statusFilter={statusFilter}
      statusOptions={STATUS_OPTIONS}
      sessions={sessions}
      getStatusLabel={getSessionStatusLabel}
      canRevokeSession={canRevokeSession}
      hasNext={hasNext}
      loadMoreStatus={loadMoreStatus}
      inlineError={errorMessage && status === "ready" ? errorMessage : ""}
      onStatusChange={setStatusFilter}
      onRevokeSession={setRevokeSessionTarget}
      onLoadMore={onLoadMore}
      onRetry={() => fetchPage(1, false)}
      revokeModal={
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
      }
    />
  );
}
