import { Fragment, useCallback, useEffect, useState } from "react";
import { getWebhookLogsForSupport } from "../../api/orderSupportApi.js";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import { formatDateTime } from "../../../../security/utils/formatDateTime.js";
import {
  AccountCard,
  AccountSkeleton,
  TabPanelHeader,
} from "../../../../../../shared/ui/auth/authUi.jsx";
import { ErrorState } from "../../../../../../shared/ui/PageState.jsx";
import { ORDER_SUPPORT_PERMISSIONS } from "../../constants/orderSupportPermissions.js";
import {
  ORDER_SUPPORT_WEBHOOK_SUBTITLE,
  ORDER_SUPPORT_WEBHOOK_TITLE,
} from "../../constants/orderSupportUiStrings.js";
import { useOrderSupportPermissions } from "../../hooks/useOrderSupportPermissions.js";
import { handleSupportLoadError } from "../../utils/orderSupportTabErrors.js";
import { SupportForbiddenState } from "../SupportForbiddenState.jsx";
import { SupportStatusBadge } from "../SupportStatusBadge.jsx";
import { SupportUnavailableState } from "../SupportUnavailableState.jsx";

const PAYOS_STATUS_OPTIONS = ["", "PROCESSED", "PENDING", "INVALID_SIGNATURE"];
const PAGE_SIZE = 20;

function PayloadSummary({ summary }) {
  if (!summary || typeof summary !== "object") {
    return <span className="text-on-surface-variant">—</span>;
  }
  return (
    <pre className="max-w-md overflow-x-auto rounded bg-surface-container-low p-2 text-xs text-on-surface">
      {JSON.stringify(summary, null, 2)}
    </pre>
  );
}

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

  return (
    <div className="space-y-6">
      <TabPanelHeader title={ORDER_SUPPORT_WEBHOOK_TITLE} subtitle={ORDER_SUPPORT_WEBHOOK_SUBTITLE} />

      {!canReadWebhook ? (
        <SupportForbiddenState message="Tài khoản thiếu quyền WEBHOOK_SUPPORT_READ." />
      ) : null}

      <AccountCard>
        <form onSubmit={handleApplyFilters} className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          <div>
            <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Provider</label>
            <select
              value={draftFilters.provider}
              onChange={(e) => setDraftFilters((prev) => ({ ...prev, provider: e.target.value }))}
              className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
            >
              <option value="">Tất cả</option>
              <option value="PAYOS">PayOS</option>
              <option value="GHN">GHN</option>
            </select>
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Reference ID</label>
            <input
              type="text"
              value={draftFilters.reference_id}
              onChange={(e) => setDraftFilters((prev) => ({ ...prev, reference_id: e.target.value }))}
              placeholder="payos_order_code / ghn_order_code"
              className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
            />
          </div>
          {draftFilters.provider === "PAYOS" || draftFilters.provider === "" ? (
            <div>
              <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Status (PayOS)</label>
              <select
                value={draftFilters.status}
                onChange={(e) => setDraftFilters((prev) => ({ ...prev, status: e.target.value }))}
                className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
              >
                {PAYOS_STATUS_OPTIONS.map((option) => (
                  <option key={option || "all"} value={option}>
                    {option || "Tất cả"}
                  </option>
                ))}
              </select>
            </div>
          ) : null}
          <div>
            <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Từ (ISO)</label>
            <input
              type="datetime-local"
              value={draftFilters.from}
              onChange={(e) => setDraftFilters((prev) => ({ ...prev, from: e.target.value }))}
              className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
            />
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Đến (ISO)</label>
            <input
              type="datetime-local"
              value={draftFilters.to}
              onChange={(e) => setDraftFilters((prev) => ({ ...prev, to: e.target.value }))}
              className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
            />
          </div>
          <div className="flex items-end gap-2 md:col-span-2 lg:col-span-3">
            <button
              type="submit"
              className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white hover:opacity-90"
            >
              Áp dụng bộ lọc
            </button>
            <button
              type="button"
              onClick={handleClearFilters}
              className="rounded-lg border border-outline-variant px-4 py-2 text-sm font-medium text-on-surface-variant hover:bg-surface-container-low"
            >
              Xóa bộ lọc
            </button>
          </div>
        </form>
      </AccountCard>

      {status === "loading" ? <AccountSkeleton /> : null}

      {status === "forbidden" ? <SupportForbiddenState message={errorMessage} /> : null}
      {status === "unavailable" ? <SupportUnavailableState message={errorMessage} /> : null}

      {status === "error" ? (
        <AccountCard className="border-error/30">
          <ErrorState message={errorMessage} />
          <button
            type="button"
            onClick={fetchLogs}
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
              {result.total_elements ?? 0} bản ghi · Trang {result.page}/{totalPages}
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

          {result.logs?.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="w-full min-w-[900px] text-left text-sm">
                <thead>
                  <tr className="border-b border-outline-variant text-on-surface-variant">
                    <th className="py-2 pr-3 font-medium">Provider</th>
                    <th className="py-2 pr-3 font-medium">Reference</th>
                    <th className="py-2 pr-3 font-medium">Event</th>
                    <th className="py-2 pr-3 font-medium">Status</th>
                    <th className="py-2 pr-3 font-medium">Chữ ký</th>
                    <th className="py-2 pr-3 font-medium">Retry</th>
                    <th className="py-2 pr-3 font-medium">Nhận lúc</th>
                    <th className="py-2 font-medium">Payload</th>
                  </tr>
                </thead>
                <tbody>
                  {result.logs.map((log) => (
                    <Fragment key={log.log_id}>
                      <tr className="border-b border-outline-variant/60 align-top">
                        <td className="py-3 pr-3">
                          <span className="rounded-full bg-primary/10 px-2 py-0.5 text-xs font-bold text-primary">
                            {log.provider}
                          </span>
                        </td>
                        <td className="py-3 pr-3 font-mono text-xs">{log.reference_id}</td>
                        <td className="py-3 pr-3">{log.event_type}</td>
                        <td className="py-3 pr-3">
                          <SupportStatusBadge status={log.processing_status} />
                        </td>
                        <td className="py-3 pr-3">
                          {log.signature_valid == null ? "—" : log.signature_valid ? "Hợp lệ" : "Không hợp lệ"}
                        </td>
                        <td className="py-3 pr-3">{log.retry_count ?? 0}</td>
                        <td className="py-3 pr-3">{formatDateTime(log.received_at)}</td>
                        <td className="py-3">
                          <button
                            type="button"
                            onClick={() =>
                              setExpandedLogId((prev) => (prev === log.log_id ? null : log.log_id))
                            }
                            className="text-xs font-medium text-primary hover:underline"
                          >
                            {expandedLogId === log.log_id ? "Thu gọn" : "Xem"}
                          </button>
                        </td>
                      </tr>
                      {expandedLogId === log.log_id ? (
                        <tr>
                          <td colSpan={8} className="bg-surface-container-low px-3 py-3">
                            <PayloadSummary summary={log.payload_summary} />
                          </td>
                        </tr>
                      ) : null}
                    </Fragment>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-sm text-on-surface-variant">Không có webhook log phù hợp bộ lọc.</p>
          )}
        </AccountCard>
      ) : null}
    </div>
  );
}
