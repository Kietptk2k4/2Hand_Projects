import { formatVndPrice } from "../../../../social/utils/formatPrice";
import { AdminMetricCard, AdminPageHeader, AdminSurfaceCard } from "../../components/ui";
import { PlatformTrendChart } from "./PlatformTrendChart.jsx";

const GRANULARITY_OPTIONS = [
  { value: "DAY", label: "Theo ngày" },
  { value: "WEEK", label: "Theo tuần" },
  { value: "MONTH", label: "Theo tháng" },
];

function formatPeriodRange(from, to) {
  if (!from || !to) return null;
  const fromDate = new Date(from);
  const toDate = new Date(to);
  if (Number.isNaN(fromDate.getTime()) || Number.isNaN(toDate.getTime())) return null;

  const formatter = new Intl.DateTimeFormat("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
  return `${formatter.format(fromDate)} – ${formatter.format(toDate)}`;
}

function MetricCardsSkeleton() {
  return (
    <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
      {Array.from({ length: 5 }, (_, index) => (
        <AdminSurfaceCard key={index} padding="md" className="animate-pulse">
          <div className="h-3 w-28 rounded bg-admin-surface-muted" />
          <div className="mt-4 h-8 w-36 rounded bg-admin-surface-muted" />
          <div className="mt-2 h-3 w-20 rounded bg-admin-surface-muted" />
        </AdminSurfaceCard>
      ))}
    </div>
  );
}

export function AdminFinanceOverviewView({
  summary,
  trend,
  isLoading,
  granularity,
  onGranularityChange,
  onRetry,
}) {
  const periodLabel = formatPeriodRange(summary?.from, summary?.to);
  const granularityLabel =
    granularity === "MONTH" ? "tháng" : granularity === "WEEK" ? "tuần" : "ngày";

  return (
    <div className="max-w-full min-w-0 space-y-6">
      <AdminPageHeader
        eyebrow="Commerce finance"
        title="Tổng quan tài chính sàn"
        subtitle={
          periodLabel
            ? `GMV đã ghi nhận, phí sàn, pipeline COD và payout · ${periodLabel}`
            : "GMV đã ghi nhận, phí sàn, pipeline COD và payout (mặc định 30 ngày)."
        }
        actions={
          <button
            type="button"
            onClick={onRetry}
            disabled={isLoading}
            className="inline-flex w-full items-center justify-center gap-2 rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-sm font-medium text-admin-text-secondary transition-colors hover:bg-admin-surface-muted hover:text-admin-text focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft disabled:cursor-not-allowed disabled:opacity-60 sm:w-auto sm:justify-start"
          >
            <span className="material-symbols-outlined text-base" aria-hidden="true">
              refresh
            </span>
            Làm mới
          </button>
        }
      />

      {isLoading ? (
        <MetricCardsSkeleton />
      ) : (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
          <AdminMetricCard
            label="GMV đã ghi nhận"
            value={formatVndPrice(summary?.recognizedGmv ?? 0)}
            hint={`${summary?.recognizedItemCount ?? 0} mặt hàng`}
          />
          <AdminMetricCard
            label="Tổng phí sàn"
            value={formatVndPrice(summary?.totalPlatformFee ?? 0)}
          />
          <AdminMetricCard
            label="COD đang pipeline"
            value={formatVndPrice(summary?.codPipelineAmount ?? 0)}
          />
          <AdminMetricCard
            label="Payout chờ duyệt"
            value={formatVndPrice(summary?.pendingPayoutAmount ?? 0)}
            hint={`${summary?.pendingPayoutCount ?? 0} yêu cầu`}
          />
          <AdminMetricCard
            label="Payout đã trả (kỳ)"
            value={formatVndPrice(summary?.paidPayoutAmount ?? 0)}
          />
        </div>
      )}

      <AdminSurfaceCard padding="lg" className="max-w-full min-w-0">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
          <div className="min-w-0">
            <h2 className="text-base font-semibold text-admin-text">GMV & phí sàn đã ghi nhận</h2>
            <p className="mt-1 text-sm text-admin-text-secondary">
              So sánh theo {granularityLabel} · số tiền từ API (VND)
            </p>
          </div>
          <div
            className="flex w-full max-w-full flex-wrap gap-2 sm:w-auto"
            role="group"
            aria-label="Chọn khoảng thời gian biểu đồ"
          >
            {GRANULARITY_OPTIONS.map((option) => (
              <button
                key={option.value}
                type="button"
                onClick={() => onGranularityChange(option.value)}
                aria-pressed={granularity === option.value}
                className={[
                  "min-w-0 flex-1 rounded-lg px-3 py-2 text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent focus-visible:ring-offset-2 focus-visible:ring-offset-admin-surface sm:flex-none sm:px-3.5",
                  granularity === option.value
                    ? "bg-admin-accent text-white"
                    : "border border-admin-border text-admin-text-secondary hover:bg-admin-surface-muted hover:text-admin-text",
                ].join(" ")}
              >
                {option.label}
              </button>
            ))}
          </div>
        </div>

        <PlatformTrendChart trend={trend} isLoading={isLoading} granularity={granularity} />
      </AdminSurfaceCard>
    </div>
  );
}
