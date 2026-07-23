import { formatVndPrice } from "../../../../social/utils/formatPrice";
import { AdminPageHeader, AdminSurfaceCard } from "../../components/ui";
import { computeCodPipelineMetrics } from "../utils/codPipelineHelpers.js";
import { CodPipelineConversionPanel } from "./CodPipelineConversionPanel.jsx";
import { CodPipelineStageStrip } from "./CodPipelineStageStrip.jsx";
import { FinanceCodDonutChart } from "./FinanceCodDonutChart.jsx";
import { CommerceFinanceRetryPanel } from "./ui/CommerceFinanceRetryPanel.jsx";

function HeroTotals({ metrics, isLoading }) {
  if (isLoading) {
    return (
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
        <AdminSurfaceCard padding="md" className="animate-pulse">
          <div className="h-3 w-28 rounded bg-admin-surface-muted" />
          <div className="mt-4 h-8 w-40 rounded bg-admin-surface-muted" />
        </AdminSurfaceCard>
        <AdminSurfaceCard padding="md" className="animate-pulse">
          <div className="h-3 w-28 rounded bg-admin-surface-muted" />
          <div className="mt-4 h-8 w-40 rounded bg-admin-surface-muted" />
        </AdminSurfaceCard>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
      <AdminSurfaceCard padding="md" className="min-w-0 border-admin-accent-border/40">
        <p className="text-[11px] font-medium uppercase tracking-[0.08em] text-admin-text-muted">
          Đang pipeline
        </p>
        <p className="mt-2 break-words text-2xl font-semibold tabular-nums tracking-tight text-admin-text sm:text-3xl">
          {formatVndPrice(metrics.pipelineAmount)}
        </p>
        <p className="mt-1 text-sm text-admin-text-secondary">
          {metrics.pipelineItemCount} dòng · vận chuyển + chờ xác nhận
        </p>
      </AdminSurfaceCard>
      <AdminSurfaceCard padding="md" className="min-w-0">
        <p className="text-[11px] font-medium uppercase tracking-[0.08em] text-admin-text-muted">
          Đã ghi nhận
        </p>
        <p className="mt-2 break-words text-2xl font-semibold tabular-nums tracking-tight text-admin-text sm:text-3xl">
          {formatVndPrice(metrics.recognized.amount)}
        </p>
        <p className="mt-1 text-sm text-admin-text-secondary">
          {metrics.recognized.itemCount} dòng · COMPLETED + PAID
        </p>
      </AdminSurfaceCard>
    </div>
  );
}

export function AdminFinanceCodPipelineView({
  status,
  errorMessage,
  pipeline,
  onRetry,
  onStageClick,
  onOpenOrderSupport,
}) {
  const isLoading = status === "loading" || status === "idle";
  const metrics = computeCodPipelineMetrics(pipeline);
  const panelStatus = isLoading ? "loading" : status === "error" ? "error" : "ready";

  if (status === "error" && !pipeline) {
    return (
      <div className="mb-6 max-w-full min-w-0 space-y-4">
        <AdminPageHeader
          eyebrow="Tài chính thương mại"
          title="COD pipeline toàn sàn"
          subtitle="Pipeline ghi nhận doanh thu theo fulfillment (PROCESSING → SHIPPED → DELIVERED → COMPLETED)."
        />
        <CommerceFinanceRetryPanel message={errorMessage} onRetry={onRetry} />
      </div>
    );
  }

  return (
    <div className="mb-6 max-w-full min-w-0 space-y-6">
      <AdminPageHeader
        eyebrow="Tài chính thương mại"
        title="COD pipeline toàn sàn"
        subtitle="Pipeline ghi nhận doanh thu theo fulfillment (PROCESSING → SHIPPED → DELIVERED → COMPLETED) — không chỉ đơn COD."
        actions={
          <div className="flex flex-wrap items-center gap-2">
            {typeof onOpenOrderSupport === "function" ? (
              <button
                type="button"
                onClick={onOpenOrderSupport}
                className="inline-flex min-h-10 items-center gap-2 rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-sm font-medium text-admin-text-secondary transition-colors hover:bg-admin-surface-muted hover:text-admin-text focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
              >
                <span className="material-symbols-outlined text-base" aria-hidden="true">
                  receipt_long
                </span>
                Order Support
              </button>
            ) : null}
            <button
              type="button"
              onClick={onRetry}
              disabled={isLoading}
              className="inline-flex min-h-10 items-center gap-2 rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-sm font-medium text-admin-text-secondary transition-colors hover:bg-admin-surface-muted hover:text-admin-text focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft disabled:cursor-not-allowed disabled:opacity-60"
            >
              <span className="material-symbols-outlined text-base" aria-hidden="true">
                refresh
              </span>
              Làm mới
            </button>
          </div>
        }
      />

      <HeroTotals metrics={metrics} isLoading={isLoading} />

      <CodPipelineStageStrip
        pipeline={pipeline}
        shares={metrics.shares}
        isLoading={isLoading}
        onStageClick={onStageClick}
      />

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-2">
        <FinanceCodDonutChart
          codPipeline={pipeline}
          status={panelStatus}
          errorMessage={errorMessage}
          onRetry={onRetry}
        />
        <CodPipelineConversionPanel
          metrics={metrics}
          status={panelStatus}
          errorMessage={errorMessage}
          onRetry={onRetry}
        />
      </div>
    </div>
  );
}
