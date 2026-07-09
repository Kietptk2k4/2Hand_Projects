import { useState } from "react";
import { formatVndPrice } from "../../../../social/utils/formatPrice";

function formatPeriodLabel(periodStart, granularity) {
  if (!periodStart) return "";
  const date = new Date(periodStart);
  if (Number.isNaN(date.getTime())) return "";

  if (granularity === "MONTH") {
    return date.toLocaleDateString("vi-VN", { month: "short", year: "numeric" });
  }
  if (granularity === "WEEK") {
    return `Tuần ${date.toLocaleDateString("vi-VN", { day: "2-digit", month: "2-digit" })}`;
  }
  return date.toLocaleDateString("vi-VN", { day: "2-digit", month: "2-digit" });
}

function PlatformTrendTooltip({ point, granularity }) {
  if (!point) {
    return (
      <div className="rounded-lg border border-dashed border-admin-border bg-admin-surface-raised px-3 py-3 text-sm text-admin-text-muted sm:px-4">
        Di chuột hoặc dùng Tab để xem GMV và phí sàn theo kỳ.
      </div>
    );
  }

  return (
    <div className="rounded-lg border border-admin-border bg-admin-surface px-3 py-3 shadow-[var(--shadow-admin-surface)] sm:px-4">
      <p className="text-xs font-medium uppercase tracking-[0.08em] text-admin-text-muted">
        {formatPeriodLabel(point.periodStart, granularity)}
      </p>
      <dl className="mt-3 grid grid-cols-1 gap-3 sm:grid-cols-3 sm:gap-2">
        <div className="min-w-0">
          <dt className="text-xs text-admin-text-muted">GMV</dt>
          <dd className="mt-0.5 break-words text-sm font-semibold tabular-nums text-admin-text">
            {formatVndPrice(point.gmvAmount)}
          </dd>
        </div>
        <div className="min-w-0">
          <dt className="text-xs text-admin-text-muted">Phí sàn</dt>
          <dd className="mt-0.5 break-words text-sm font-semibold tabular-nums text-admin-text">
            {formatVndPrice(point.platformFeeAmount)}
          </dd>
        </div>
        <div className="min-w-0">
          <dt className="text-xs text-admin-text-muted">Mặt hàng</dt>
          <dd className="mt-0.5 text-sm font-semibold tabular-nums text-admin-text">{point.itemCount}</dd>
        </div>
      </dl>
    </div>
  );
}

function ChartLegend() {
  return (
    <div className="flex flex-wrap items-center gap-x-4 gap-y-2 border-t border-admin-border-subtle pt-3 text-xs text-admin-text-secondary">
      <span className="inline-flex items-center gap-2">
        <span className="h-2.5 w-2.5 shrink-0 rounded-sm bg-admin-accent" aria-hidden="true" />
        GMV đã ghi nhận
      </span>
      <span className="inline-flex items-center gap-2">
        <span className="h-2.5 w-2.5 shrink-0 rounded-sm bg-admin-warning" aria-hidden="true" />
        Phí sàn
      </span>
    </div>
  );
}

function ChartSkeleton() {
  return (
    <div className="mt-4 max-w-full min-w-0 space-y-4 sm:mt-5 sm:space-y-5">
      <div className="h-[88px] animate-pulse rounded-lg bg-admin-surface-muted" />
      <div
        className="flex h-52 max-w-full items-end gap-2 overflow-x-auto pb-2 sm:h-60 sm:gap-3 [scrollbar-width:thin]"
        aria-hidden="true"
      >
        {Array.from({ length: 8 }, (_, index) => (
          <div key={index} className="flex w-12 shrink-0 flex-col items-center gap-2 sm:w-14">
            <div className="h-40 w-full animate-pulse rounded-lg bg-admin-surface-muted sm:h-44" />
            <div className="h-3 w-10 animate-pulse rounded bg-admin-surface-muted" />
          </div>
        ))}
      </div>
      <ChartLegend />
    </div>
  );
}

export function PlatformTrendChart({ trend, isLoading, granularity }) {
  const [activePoint, setActivePoint] = useState(null);

  if (isLoading) {
    return <ChartSkeleton />;
  }

  const points = trend?.points ?? [];
  const maxAmount = Math.max(...points.map((point) => point.gmvAmount), 1);

  if (!points.length) {
    return (
      <p className="mt-4 rounded-lg border border-dashed border-admin-border bg-admin-surface-raised px-4 py-8 text-center text-sm text-admin-text-secondary sm:mt-5">
        Chưa có dữ liệu GMV trong khoảng thời gian này.
      </p>
    );
  }

  const highlightedPoint = activePoint ?? points[points.length - 1];

  return (
    <div className="mt-4 max-w-full min-w-0 space-y-4 sm:mt-5 sm:space-y-5">
      <div className="relative z-10 shrink-0">
        <PlatformTrendTooltip point={highlightedPoint} granularity={granularity} />
      </div>

      <div
        className="max-w-full overflow-x-auto overscroll-x-contain rounded-lg pb-2 [-webkit-overflow-scrolling:touch] [scrollbar-width:thin]"
        role="region"
        aria-label="Cuộn ngang biểu đồ GMV"
      >
        <div
          className="flex min-w-max items-end gap-2 px-0.5 sm:gap-3 sm:px-1"
          role="list"
          aria-label="Biểu đồ GMV và phí sàn theo kỳ"
        >
          {points.map((point) => {
            const gmvHeight = Math.max(6, Math.round((point.gmvAmount / maxAmount) * 100));
            const feeHeight = Math.max(4, Math.round((point.platformFeeAmount / maxAmount) * 100));
            const isActive = highlightedPoint?.periodStart === point.periodStart;
            const label = formatPeriodLabel(point.periodStart, granularity);

            return (
              <div key={point.periodStart} role="listitem" className="flex w-12 shrink-0 flex-col items-center gap-2 sm:w-14 sm:gap-2.5">
                <button
                  type="button"
                  onMouseEnter={() => setActivePoint(point)}
                  onMouseLeave={() => setActivePoint(null)}
                  onFocus={() => setActivePoint(point)}
                  onBlur={() => setActivePoint(null)}
                  className={[
                    "group flex h-40 w-full flex-col items-center justify-end gap-1 rounded-lg px-0.5 py-2 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent focus-visible:ring-offset-2 focus-visible:ring-offset-admin-surface sm:h-44 sm:px-1",
                    isActive ? "bg-admin-accent-soft/50" : "hover:bg-admin-surface-muted",
                  ].join(" ")}
                  aria-label={`${label}: GMV ${formatVndPrice(point.gmvAmount)}, phí sàn ${formatVndPrice(point.platformFeeAmount)}`}
                  aria-current={isActive ? "true" : undefined}
                >
                  <div className="flex h-32 w-full items-end justify-center gap-0.5 sm:h-36 sm:gap-1">
                    <div
                      className={[
                        "w-2.5 rounded-t transition-all sm:w-3",
                        isActive ? "bg-admin-accent" : "bg-admin-accent/80 group-hover:bg-admin-accent",
                      ].join(" ")}
                      style={{ height: `${gmvHeight}%` }}
                    />
                    <div
                      className={[
                        "w-2.5 rounded-t transition-all sm:w-3",
                        isActive ? "bg-admin-warning" : "bg-admin-warning/70 group-hover:bg-admin-warning",
                      ].join(" ")}
                      style={{ height: `${feeHeight}%` }}
                    />
                  </div>
                </button>
                <span className="max-w-full truncate text-center text-[10px] leading-tight text-admin-text-muted sm:text-[11px]">
                  {label}
                </span>
              </div>
            );
          })}
        </div>
      </div>

      <ChartLegend />
    </div>
  );
}
