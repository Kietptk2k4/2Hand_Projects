import { formatVndPrice } from "../../../../social/utils/formatPrice";
import { FinanceChartPanel } from "./FinanceChartPanel.jsx";
import { formatSharePercent } from "../utils/codPipelineHelpers.js";

const FUNNEL_STEPS = [
  {
    key: "inTransit",
    label: "Đang vận chuyển",
    color: "#8b7355",
    widthClass: "w-full",
  },
  {
    key: "pendingConfirm",
    label: "Chờ xác nhận",
    color: "#c4a574",
    widthClass: "w-[82%]",
  },
  {
    key: "recognized",
    label: "Đã ghi nhận",
    color: "#5b7c6a",
    widthClass: "w-[64%]",
  },
];

export function CodPipelineConversionPanel({
  metrics,
  status,
  errorMessage,
  onRetry,
}) {
  const empty = !metrics || metrics.isEmpty;

  return (
    <FinanceChartPanel
      title="Funnel & conversion"
      subtitle="Ước lượng snapshot — không phải cohort theo thời gian"
      status={status}
      errorMessage={errorMessage}
      onRetry={onRetry}
      empty={empty}
      emptyMessage="Chưa có dòng đơn trong pipeline fulfillment."
    >
      <div className="space-y-5">
        <div className="flex flex-col items-center gap-2">
          {FUNNEL_STEPS.map((step) => {
            const bucket = metrics?.[step.key] ?? { amount: 0, itemCount: 0 };
            const share = metrics?.shares?.[step.key] ?? 0;
            return (
              <div key={step.key} className={`${step.widthClass} max-w-full`}>
                <div
                  className="rounded-lg px-3 py-2.5 text-white shadow-sm"
                  style={{ backgroundColor: step.color }}
                >
                  <div className="flex items-center justify-between gap-2">
                    <span className="text-xs font-medium opacity-90">{step.label}</span>
                    <span className="text-xs font-semibold tabular-nums">
                      {formatSharePercent(share)}
                    </span>
                  </div>
                  <p className="mt-1 text-sm font-semibold tabular-nums">
                    {formatVndPrice(bucket.amount)}
                  </p>
                  <p className="text-[11px] tabular-nums opacity-80">
                    {bucket.itemCount} dòng đơn
                  </p>
                </div>
              </div>
            );
          })}
        </div>

        <dl className="grid grid-cols-1 gap-3 sm:grid-cols-3">
          <div className="rounded-xl bg-admin-surface-muted/70 px-3 py-3">
            <dt className="text-[11px] font-medium uppercase tracking-[0.06em] text-admin-text-muted">
              Đang pipeline
            </dt>
            <dd className="mt-1 text-lg font-semibold tabular-nums text-admin-text">
              {formatSharePercent(metrics?.conversion?.inFlightShareOfTotal)}
            </dd>
            <p className="mt-0.5 text-xs text-admin-text-secondary">
              (VC + chờ XN) / tổng
            </p>
          </div>
          <div className="rounded-xl bg-admin-surface-muted/70 px-3 py-3">
            <dt className="text-[11px] font-medium uppercase tracking-[0.06em] text-admin-text-muted">
              Chờ XN trong pipeline
            </dt>
            <dd className="mt-1 text-lg font-semibold tabular-nums text-admin-text">
              {formatSharePercent(metrics?.conversion?.pendingShareOfPipeline)}
            </dd>
            <p className="mt-0.5 text-xs text-admin-text-secondary">
              chờ XN / đang pipeline
            </p>
          </div>
          <div className="rounded-xl bg-admin-surface-muted/70 px-3 py-3">
            <dt className="text-[11px] font-medium uppercase tracking-[0.06em] text-admin-text-muted">
              Đã ghi nhận
            </dt>
            <dd className="mt-1 text-lg font-semibold tabular-nums text-admin-text">
              {formatSharePercent(metrics?.conversion?.recognizedShareOfTotal)}
            </dd>
            <p className="mt-0.5 text-xs text-admin-text-secondary">đã ghi nhận / tổng</p>
          </div>
        </dl>
      </div>
    </FinanceChartPanel>
  );
}
