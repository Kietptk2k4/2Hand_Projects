import { AdminPageHeader } from "../../components/ui";
import { FinanceCodDonutChart } from "./FinanceCodDonutChart.jsx";
import { FinanceDateRangeToolbar } from "./FinanceDateRangeToolbar.jsx";
import { FinanceGmvFeeChart } from "./FinanceGmvFeeChart.jsx";
import { FinanceKpiCards } from "./FinanceKpiCards.jsx";
import { FinancePayoutDonutChart } from "./FinancePayoutDonutChart.jsx";
import { FinanceTopSellersBarChart } from "./FinanceTopSellersBarChart.jsx";

function formatPeriodRange(from, to) {
  if (!from || !to) return null;
  const fromDate = new Date(from);
  const toDate = new Date(to);
  if (Number.isNaN(fromDate.getTime()) || Number.isNaN(toDate.getTime())) return null;

  // Display inclusive end: day before exclusive `to`
  const inclusiveEnd = new Date(toDate);
  inclusiveEnd.setUTCDate(inclusiveEnd.getUTCDate() - 1);

  const formatter = new Intl.DateTimeFormat("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
  return `${formatter.format(fromDate)} – ${formatter.format(inclusiveEnd)}`;
}

export function AdminFinanceOverviewView({
  summary,
  trend,
  deltas,
  feeRate,
  codPipeline,
  topSellers,
  payoutOverview,
  coreStatus = "idle",
  coreErrorMessage = "",
  isLoading,
  activeRangeId,
  granularity,
  onRangeChange,
  onGranularityChange,
  onRetry,
  onRetryCod,
  onRetryTopSellers,
  onRetryPayout,
  onNavigateCod,
  onNavigatePayout,
  onNavigateTopSellers,
}) {
  const trendStatus =
    coreStatus === "ready" ? "ready" : coreStatus === "error" ? "error" : "loading";
  const periodLabel = formatPeriodRange(
    summary?.from || trend?.from,
    summary?.to || trend?.to,
  );
  const sparkPoints = trend?.points ?? [];

  return (
    <div className="mb-6 max-w-full min-w-0 space-y-6">
      <AdminPageHeader
        eyebrow="Tài chính thương mại"
        title="Tổng quan tài chính sàn"
        subtitle={
          periodLabel
            ? `GMV, phí sàn, COD pipeline và payout · ${periodLabel}`
            : "GMV, phí sàn, COD pipeline và payout."
        }
      />

      <FinanceDateRangeToolbar
        activeRangeId={activeRangeId}
        granularity={granularity}
        onRangeChange={onRangeChange}
        onGranularityChange={onGranularityChange}
        onRefresh={onRetry}
        isLoading={isLoading}
      />

      <FinanceKpiCards
        summary={summary}
        deltas={deltas}
        feeRate={feeRate}
        sparkPoints={sparkPoints}
        isLoading={isLoading}
        onNavigateCod={onNavigateCod}
        onNavigatePayout={onNavigatePayout}
        onNavigateTopSellers={onNavigateTopSellers}
      />

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-5">
        <div className="min-w-0 xl:col-span-3">
          <FinanceGmvFeeChart
            trend={trend}
            status={trendStatus}
            errorMessage={coreErrorMessage}
            granularity={granularity}
            onRetry={onRetry}
          />
        </div>
        <div className="min-w-0 xl:col-span-2">
          <FinanceCodDonutChart
            codPipeline={codPipeline?.data}
            status={codPipeline?.status || "idle"}
            errorMessage={codPipeline?.errorMessage}
            onRetry={onRetryCod}
          />
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-2">
        <FinanceTopSellersBarChart
          topSellers={topSellers?.data}
          status={topSellers?.status || "idle"}
          errorMessage={topSellers?.errorMessage}
          onRetry={onRetryTopSellers}
        />
        <FinancePayoutDonutChart
          payoutOverview={payoutOverview?.data}
          status={payoutOverview?.status || "idle"}
          errorMessage={payoutOverview?.errorMessage}
          onRetry={onRetryPayout}
        />
      </div>
    </div>
  );
}
