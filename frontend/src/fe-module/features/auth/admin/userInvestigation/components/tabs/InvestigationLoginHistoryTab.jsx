import { useCallback, useEffect, useState } from "react";
import { getInvestigationLoginHistory } from "../../api/userInvestigationApi.js";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import {
  INVESTIGATION_LOGIN_HISTORY_SUBTITLE,
  INVESTIGATION_LOGIN_HISTORY_TITLE,
} from "../../constants/userInvestigationUiStrings.js";
import { INVESTIGATION_PERMISSIONS } from "../../constants/investigationPermissions.js";
import { useInvestigationPermissions } from "../../hooks/useInvestigationPermissions.js";
import { handleInvestigationLoadError } from "../../utils/investigationTabErrors.js";
import { InvestigationLoginHistoryTabView } from "./InvestigationLoginHistoryTabView.jsx";

const PAGE_LIMIT = 20;

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
    [userId, appliedFilters, showSessionExpired],
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

  const viewStatus = !userId
    ? "no-user"
    : status === "loading"
      ? "loading"
      : status;

  return (
    <InvestigationLoginHistoryTabView
      title={INVESTIGATION_LOGIN_HISTORY_TITLE}
      subtitle={INVESTIGATION_LOGIN_HISTORY_SUBTITLE}
      status={viewStatus}
      errorMessage={errorMessage}
      canReadProfile={canReadProfile}
      permissionNotice="Tài khoản của bạn thiếu quyền USER_INVESTIGATION_READ để xem lịch sử đăng nhập."
      successDraft={successDraft}
      fromDraft={fromDraft}
      toDraft={toDraft}
      items={items}
      hasNext={hasNext}
      loadMoreStatus={loadMoreStatus}
      inlineError={errorMessage && status === "ready" ? errorMessage : ""}
      onSuccessChange={setSuccessDraft}
      onFromChange={setFromDraft}
      onToChange={setToDraft}
      onApplyFilters={onApplyFilters}
      onClearFilters={onClearFilters}
      onLoadMore={onLoadMore}
      onRetry={() => fetchPage(1, false)}
    />
  );
}
