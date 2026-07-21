import {
  AdminPageHeader,
  AdminPagination,
  AdminSurfaceCard,
} from "../../../components/ui";
import {
  ORDER_SUPPORT_WEBHOOK_SUBTITLE,
  ORDER_SUPPORT_WEBHOOK_TITLE,
} from "../../constants/orderSupportUiStrings.js";
import { SupportForbiddenState } from "../SupportForbiddenState.jsx";
import { SupportListSkeleton } from "../ui/SupportListSkeleton.jsx";
import { SupportRetryPanel } from "../ui/SupportRetryPanel.jsx";
import { SupportUnavailableState } from "../SupportUnavailableState.jsx";
import { WebhookLogsFilterBar } from "../WebhookLogsFilterBar.jsx";
import { WebhookLogsTable } from "../WebhookLogsTable.jsx";

export function WebhookLogsSupportTabView({
  canReadWebhook,
  status,
  errorMessage,
  result,
  draftFilters,
  onDraftFiltersChange,
  onApplyFilters,
  onClearFilters,
  onRetry,
  expandedLogId,
  onToggleExpand,
  currentPage,
  totalPages,
  onPageChange,
  formatDateTime,
}) {
  const summary =
    status === "ready"
      ? `${result?.total_elements ?? 0} bản ghi · Trang ${result?.page ?? currentPage}/${totalPages}`
      : "";

  return (
    <div className="w-full min-w-0 space-y-4">
      <AdminPageHeader title={ORDER_SUPPORT_WEBHOOK_TITLE} subtitle={ORDER_SUPPORT_WEBHOOK_SUBTITLE} />

      {!canReadWebhook ? (
        <SupportForbiddenState message="Tài khoản thiếu quyền WEBHOOK_SUPPORT_READ." />
      ) : null}

      <AdminSurfaceCard padding="lg" className="max-w-full min-w-0">
        <WebhookLogsFilterBar
          draftFilters={draftFilters}
          onDraftFiltersChange={onDraftFiltersChange}
          onApply={onApplyFilters}
          onClear={onClearFilters}
        />
      </AdminSurfaceCard>

      {status === "loading" ? <SupportListSkeleton /> : null}
      {status === "forbidden" ? <SupportForbiddenState message={errorMessage} /> : null}
      {status === "unavailable" ? <SupportUnavailableState message={errorMessage} /> : null}
      {status === "error" ? <SupportRetryPanel message={errorMessage} onRetry={onRetry} /> : null}

      {status === "ready" ? (
        <AdminSurfaceCard padding="lg" className="max-w-full min-w-0">
          <AdminPagination
            currentPage={currentPage}
            totalPages={totalPages}
            summary={summary}
            onPrevious={() => onPageChange(currentPage - 1)}
            onNext={() => onPageChange(currentPage + 1)}
            className="mb-4"
          />
          <WebhookLogsTable
            logs={result?.logs ?? []}
            expandedLogId={expandedLogId}
            onToggleExpand={onToggleExpand}
            formatDateTime={formatDateTime}
          />
        </AdminSurfaceCard>
      ) : null}
    </div>
  );
}
