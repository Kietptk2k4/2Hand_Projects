import { useCallback, useEffect, useState } from "react";
import { getWebhookLogsForSupport } from "../../api/orderSupportApi.js";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import { formatDateTime } from "../../../../security/utils/formatDateTime.js";
import { ORDER_SUPPORT_PERMISSIONS } from "../../constants/orderSupportPermissions.js";
import { useOrderSupportPermissions } from "../../hooks/useOrderSupportPermissions.js";
import { handleSupportLoadError } from "../../utils/orderSupportTabErrors.js";
import { WebhookLogsSupportTabView } from "./WebhookLogsSupportTabView.jsx";

const PAGE_SIZE = 20;

export function WebhookLogsSupportTab({ webhookFilters, onFiltersChange }) {
  const { showSessionExpired } = useAuthSession();
  const { canReadWebhook } = useOrderSupportPermissions();
  const [result, setResult] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [expandedLogId, setExpandedLogId] = useState(null);

  const [draftFilters, setDraftFilters] = useState({
    provider: webhookFilters.provider || "",
    reference_id: webhookFilters.reference_id || "",
    status: webhookFilters.status || "",
    from: webhookFilters.from || "",
    to: webhookFilters.to || "",
  });

  useEffect(() => {
    setDraftFilters({
      provider: webhookFilters.provider || "",
      reference_id: webhookFilters.reference_id || "",
      status: webhookFilters.status || "",
      from: webhookFilters.from || "",
      to: webhookFilters.to || "",
    });
  }, [webhookFilters]);

  const fetchLogs = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await getWebhookLogsForSupport({
        provider: webhookFilters.provider || undefined,
        reference_id: webhookFilters.reference_id || undefined,
        status: webhookFilters.status || undefined,
        from: webhookFilters.from || undefined,
        to: webhookFilters.to || undefined,
        page: Number(webhookFilters.page) || 1,
        size: Number(webhookFilters.size) || PAGE_SIZE,
      });
      setResult(data);
      setStatus("ready");
    } catch (error) {
      handleSupportLoadError(error, {
        showSessionExpired,
        setStatus,
        setErrorMessage,
        permissionCode: ORDER_SUPPORT_PERMISSIONS.READ_WEBHOOK,
        actionLabel: "xem nhật ký webhook",
        fallbackMessage: "Không tải được nhật ký webhook.",
      });
    }
  }, [webhookFilters, showSessionExpired]);

  useEffect(() => {
    fetchLogs();
  }, [fetchLogs]);

  const handleApplyFilters = (event) => {
    event.preventDefault();
    onFiltersChange?.({
      ...draftFilters,
      page: 1,
      size: PAGE_SIZE,
    });
  };

  const handleClearFilters = () => {
    const cleared = {
      provider: "",
      reference_id: "",
      status: "",
      from: "",
      to: "",
      page: 1,
      size: PAGE_SIZE,
    };
    setDraftFilters(cleared);
    onFiltersChange?.(cleared);
  };

  const currentPage = Number(webhookFilters.page) || 1;
  const totalPages = result?.total_pages || 1;

  const handlePageChange = (nextPage) => {
    onFiltersChange?.({
      ...webhookFilters,
      page: nextPage,
      size: PAGE_SIZE,
    });
  };

  const handleToggleExpand = (logId) => {
    setExpandedLogId((prev) => (prev === logId ? null : logId));
  };

  return (
    <WebhookLogsSupportTabView
      canReadWebhook={canReadWebhook}
      status={status}
      errorMessage={errorMessage}
      result={result}
      draftFilters={draftFilters}
      onDraftFiltersChange={setDraftFilters}
      onApplyFilters={handleApplyFilters}
      onClearFilters={handleClearFilters}
      onRetry={fetchLogs}
      expandedLogId={expandedLogId}
      onToggleExpand={handleToggleExpand}
      currentPage={currentPage}
      totalPages={totalPages}
      onPageChange={handlePageChange}
      formatDateTime={formatDateTime}
    />
  );
}
