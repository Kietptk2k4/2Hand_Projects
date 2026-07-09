import { AdminPageHeader, AdminSurfaceCard } from "../../components/ui";
import { PayoutQueueFilterBar } from "./PayoutQueueFilterBar.jsx";
import { PayoutQueueTable } from "./PayoutQueueTable.jsx";
import { CommerceFinanceEmptyState } from "./ui/CommerceFinanceEmptyState.jsx";
import { CommerceFinanceListSkeleton } from "./ui/CommerceFinanceListSkeleton.jsx";
import { CommerceFinanceRetryPanel } from "./ui/CommerceFinanceRetryPanel.jsx";

export function AdminFinancePayoutQueueView({
  title,
  subtitle,
  statusFilter,
  loadStatus,
  errorMessage,
  items,
  totalItems,
  actionId,
  onStatusChange,
  onRefresh,
  onAction,
  onRetry,
}) {
  return (
    <div className="max-w-full min-w-0 space-y-4">
      <AdminPageHeader title={title} subtitle={subtitle} />

      <AdminSurfaceCard padding="md" className="max-w-full min-w-0">
        <PayoutQueueFilterBar
          statusFilter={statusFilter}
          loading={loadStatus === "loading"}
          onStatusChange={onStatusChange}
          onRefresh={onRefresh}
        />
      </AdminSurfaceCard>

      {loadStatus === "loading" ? <CommerceFinanceListSkeleton rows={4} /> : null}
      {loadStatus === "error" ? (
        <CommerceFinanceRetryPanel message={errorMessage} onRetry={onRetry} />
      ) : null}

      {loadStatus === "ready" ? (
        <AdminSurfaceCard padding="md" className="max-w-full min-w-0">
          {items?.length ? (
            <>
              <PayoutQueueTable items={items} actionId={actionId} onAction={onAction} />
              <p className="mt-4 text-sm text-admin-text-secondary">Tổng {totalItems} yêu cầu</p>
            </>
          ) : (
            <CommerceFinanceEmptyState message="Không có yêu cầu rút tiền." />
          )}
        </AdminSurfaceCard>
      ) : null}
    </div>
  );
}
