import { formatVndPrice } from "../../../../social/utils/formatPrice";
import { AdminMetricCard, AdminPageHeader, AdminSurfaceCard } from "../../components/ui";
import { SellerRevenueBucketCards } from "../../../../commerce/components/SellerRevenueBucketCards.jsx";
import { LedgerTable } from "./LedgerTable.jsx";
import { CommerceFinanceEmptyState } from "./ui/CommerceFinanceEmptyState.jsx";
import { CommerceFinanceListSkeleton } from "./ui/CommerceFinanceListSkeleton.jsx";
import { CommerceFinanceRetryPanel } from "./ui/CommerceFinanceRetryPanel.jsx";

export function AdminFinanceSellerDetailView({
  sellerId,
  title,
  subtitle,
  emptyMessage,
  status,
  errorMessage,
  summary,
  ledgerItems,
  onRetry,
}) {
  if (!sellerId) {
    return (
      <div className="max-w-full min-w-0 space-y-4">
        <AdminPageHeader title={title} subtitle={subtitle} />
        <CommerceFinanceEmptyState message={emptyMessage} />
      </div>
    );
  }

  if (status === "error") {
    return (
      <div className="max-w-full min-w-0 space-y-4">
        <AdminPageHeader title={title} subtitle={`Seller ID: ${sellerId}`} />
        <CommerceFinanceRetryPanel message={errorMessage} onRetry={onRetry} />
      </div>
    );
  }

  return (
    <div className="max-w-full min-w-0 space-y-4">
      <AdminPageHeader title={title} subtitle={`Seller ID: ${sellerId}`} />

      {status === "loading" ? <CommerceFinanceListSkeleton rows={4} /> : null}

      {status === "ready" ? (
        <>
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <AdminMetricCard
              label="Số dư khả dụng"
              value={formatVndPrice(summary?.balance?.availableBalance ?? 0)}
            />
            <AdminMetricCard
              label="Payout đang chờ"
              value={formatVndPrice(summary?.balance?.pendingPayoutAmount ?? 0)}
            />
          </div>

          <AdminSurfaceCard padding="md" className="max-w-full min-w-0">
            <SellerRevenueBucketCards summary={summary} isLoading={false} />
          </AdminSurfaceCard>

          <AdminSurfaceCard padding="md" className="max-w-full min-w-0">
            <h3 className="mb-4 text-base font-semibold text-admin-text">Sổ cái (10 gần nhất)</h3>
            <LedgerTable items={ledgerItems} />
          </AdminSurfaceCard>
        </>
      ) : null}
    </div>
  );
}
