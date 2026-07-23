import { AdminPageHeader, AdminSurfaceCard } from "../../components/ui";
import { FinanceDateRangeToolbar } from "./FinanceDateRangeToolbar.jsx";
import { FinanceTopSellersBarChart } from "./FinanceTopSellersBarChart.jsx";
import { TopSellersHeroStrip } from "./TopSellersHeroStrip.jsx";
import { TopSellersTable } from "./TopSellersTable.jsx";
import { CommerceFinanceEmptyState } from "./ui/CommerceFinanceEmptyState.jsx";
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

export function AdminFinanceTopSellersView({
  status,
  errorMessage,
  sellers,
  metrics,
  from,
  to,
  activeRangeId,
  limit,
  onRangeChange,
  onLimitChange,
  onRetry,
  onSellerSelect,
}) {
  const isLoading = status === "loading" || status === "idle";
  const periodLabel = formatPeriodLabel(from, to);
  const chartStatus = isLoading ? "loading" : status === "error" ? "error" : "ready";
  const chartSlice = Math.min(sellers?.length || 0, 10);

  if (status === "error" && !sellers?.length) {
    return (
      <div className="mb-6 max-w-full min-w-0 space-y-4">
        <AdminPageHeader
          eyebrow="Tài chính thương mại"
          title="Top sellers"
          subtitle="Xếp hạng theo doanh thu đã ghi nhận trong kỳ."
        />
        <CommerceFinanceRetryPanel message={errorMessage} onRetry={onRetry} />
      </div>
    );
  }

  return (
    <div className="mb-6 max-w-full min-w-0 space-y-6">
      <AdminPageHeader
        eyebrow="Tài chính thương mại"
        title="Top sellers"
        subtitle={
          periodLabel
            ? `Xếp hạng theo GMV đã ghi nhận · ${periodLabel}`
            : "Xếp hạng theo doanh thu đã ghi nhận trong kỳ."
        }
      />

      <FinanceDateRangeToolbar
        activeRangeId={activeRangeId}
        onRangeChange={onRangeChange}
        onRefresh={onRetry}
        isLoading={isLoading}
        showGranularity={false}
        showLimit
        limit={limit}
        onLimitChange={onLimitChange}
      />

      <TopSellersHeroStrip metrics={metrics} isLoading={isLoading} />

      <FinanceTopSellersBarChart
        topSellers={sellers}
        status={chartStatus}
        errorMessage={errorMessage}
        onRetry={onRetry}
        onSellerClick={onSellerSelect}
        chartLimit={10}
        title={`Biểu đồ Top ${chartSlice || Math.min(limit, 10)}`}
        subtitle="GMV đã ghi nhận · bấm cột để mở chi tiết seller"
      />

      {isLoading ? (
        <AdminSurfaceCard padding="md" className="animate-pulse">
          <div className="h-4 w-40 rounded bg-admin-surface-muted" />
          <div className="mt-4 space-y-3">
            {Array.from({ length: 5 }, (_, index) => (
              <div key={index} className="h-10 rounded bg-admin-surface-muted" />
            ))}
          </div>
        </AdminSurfaceCard>
      ) : null}

      {status === "ready" && !sellers?.length ? (
        <CommerceFinanceEmptyState message="Chưa có seller nào có GMV đã ghi nhận trong kỳ này." />
      ) : null}

      {status === "ready" && sellers?.length ? (
        <TopSellersTable sellers={sellers} onSellerSelect={onSellerSelect} />
      ) : null}
    </div>
  );
}
