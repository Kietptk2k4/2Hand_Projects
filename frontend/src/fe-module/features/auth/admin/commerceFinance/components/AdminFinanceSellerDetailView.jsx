import { AdminPageHeader } from "../../components/ui";
import { CodPipelineStageStrip } from "./CodPipelineStageStrip.jsx";
import { FinanceDateRangeToolbar } from "./FinanceDateRangeToolbar.jsx";
import { LedgerTable } from "./LedgerTable.jsx";
import { SellerDetailHeader } from "./SellerDetailHeader.jsx";
import { SellerDetailHeroStrip } from "./SellerDetailHeroStrip.jsx";
import { SellerDetailPicker } from "./SellerDetailPicker.jsx";
import { CommerceFinanceRetryPanel } from "./ui/CommerceFinanceRetryPanel.jsx";

function formatPeriodLabel(from, to) {
  if (!from || !to) return null;
  const fromDate = new Date(from);
  const toDate = new Date(to);
  if (Number.isNaN(fromDate.getTime()) || Number.isNaN(toDate.getTime())) return null;
  const inclusiveEnd = new Date(toDate);
  inclusiveEnd.setUTCDate(inclusiveEnd.getUTCDate() - 1);
  const formatter = new Intl.DateTimeFormat("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
  return `${formatter.format(fromDate)} – ${formatter.format(inclusiveEnd)}`;
}

export function AdminFinanceSellerDetailView({
  sellerId,
  sellerShop,
  status,
  errorMessage,
  summary,
  heroMetrics,
  pipeline,
  bucketShares,
  ledgerItems,
  ledgerPagination,
  from,
  to,
  activeRangeId,
  onRangeChange,
  onRetry,
  onLedgerPageChange,
  onSubmitSellerId,
  onOpenTopSellers,
  onBackToTopSellers,
}) {
  const isLoading = status === "loading" || status === "idle";
  const periodLabel = formatPeriodLabel(from, to);

  if (!sellerId) {
    return (
      <div className="mb-6 max-w-full min-w-0 space-y-6">
        <AdminPageHeader
          eyebrow="Tài chính thương mại"
          title="Chi tiết tài chính seller"
          subtitle="Theo dõi số dư, pipeline fulfillment và sổ cái theo seller."
        />
        <SellerDetailPicker
          onSubmitSellerId={onSubmitSellerId}
          onOpenTopSellers={onOpenTopSellers}
        />
      </div>
    );
  }

  if (status === "error" && !summary) {
    return (
      <div className="mb-6 max-w-full min-w-0 space-y-4">
        <SellerDetailHeader
          sellerId={sellerId}
          shopName={sellerShop}
          onBackToTopSellers={onBackToTopSellers}
        />
        <CommerceFinanceRetryPanel message={errorMessage} onRetry={onRetry} />
      </div>
    );
  }

  return (
    <div className="mb-6 max-w-full min-w-0 space-y-6">
      <div>
        <p className="mb-1 text-[11px] font-medium uppercase tracking-[0.08em] text-admin-text-muted">
          Tài chính thương mại
        </p>
        <SellerDetailHeader
          sellerId={sellerId}
          shopName={sellerShop}
          onBackToTopSellers={onBackToTopSellers}
        />
        {periodLabel ? (
          <p className="mt-2 text-sm text-admin-text-secondary">
            Pipeline theo kỳ · {periodLabel}
          </p>
        ) : null}
      </div>

      <FinanceDateRangeToolbar
        activeRangeId={activeRangeId}
        onRangeChange={onRangeChange}
        onRefresh={onRetry}
        isLoading={isLoading}
        showGranularity={false}
      />

      <SellerDetailHeroStrip metrics={heroMetrics} isLoading={isLoading} />

      <CodPipelineStageStrip
        pipeline={pipeline}
        shares={bucketShares}
        isLoading={isLoading}
      />

      <LedgerTable
        items={ledgerItems}
        pagination={ledgerPagination}
        onPageChange={onLedgerPageChange}
        isLoading={isLoading}
      />
    </div>
  );
}
