import { useCallback, useEffect, useState } from "react";
import { getEnforcementHistory } from "../../api/userInvestigationApi.js";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import {
  INVESTIGATION_ENFORCEMENT_HISTORY_SUBTITLE,
  INVESTIGATION_ENFORCEMENT_HISTORY_TITLE,
} from "../../constants/userInvestigationUiStrings.js";
import { INVESTIGATION_PERMISSIONS } from "../../constants/investigationPermissions.js";
import { useInvestigationPermissions } from "../../hooks/useInvestigationPermissions.js";
import { handleInvestigationLoadError } from "../../utils/investigationTabErrors.js";
import { InvestigationEnforcementHistoryTabView } from "./InvestigationEnforcementHistoryTabView.jsx";

const PAGE_SIZE = 20;

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

  const viewStatus = !userId ? "no-user" : status === "loading" ? "loading" : status;

  return (
    <InvestigationEnforcementHistoryTabView
      title={INVESTIGATION_ENFORCEMENT_HISTORY_TITLE}
      subtitle={INVESTIGATION_ENFORCEMENT_HISTORY_SUBTITLE}
      userId={userId}
      status={viewStatus}
      errorMessage={errorMessage}
      canReadEnforcement={canReadEnforcement}
      permissionNotice="Tài khoản của bạn thiếu quyền USER_ENFORCEMENT_READ để xem lịch sử enforcement."
      items={items}
      expandedId={expandedId}
      hasMore={page < totalPages}
      loadMoreStatus={loadMoreStatus}
      inlineError={errorMessage && status === "ready" ? errorMessage : ""}
      onToggle={(id) => setExpandedId((prev) => (prev === id ? null : id))}
      onLoadMore={onLoadMore}
      onRetry={() => fetchPage(1, false)}
    />
  );
}
