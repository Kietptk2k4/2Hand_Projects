import { formatVndPrice } from "../../social/utils/formatPrice";

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

export function SellerRevenueTrendChart({ trend, isLoading }) {
  if (isLoading) {
    return (
      <div className="h-64 animate-pulse rounded-xl border border-outline-variant bg-surface-container-low" />
    );
  }

  const points = trend?.points ?? [];
  const maxAmount = Math.max(...points.map((point) => point.recognizedAmount), 1);

  if (points.length === 0) {
    return (
      <div className="flex h-64 items-center justify-center rounded-xl border border-dashed border-outline-variant bg-surface-container-lowest px-6 text-center">
        <p className="text-body-md text-on-surface-variant">
          Chưa có doanh thu đã ghi nhận trong khoảng thời gian này.
        </p>
      </div>
    );
  }

  return (
    <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
      <div className="mb-6 flex flex-wrap items-end justify-between gap-3">
        <div>
          <h3 className="text-title-md font-semibold text-on-surface">Doanh thu đã ghi nhận</h3>
          <p className="mt-1 text-body-sm text-on-surface-variant">
            Theo {trend.granularity === "MONTH" ? "tháng" : trend.granularity === "WEEK" ? "tuần" : "ngày"}
          </p>
        </div>
      </div>

      <div className="flex h-52 items-end gap-2 overflow-x-auto pb-2">
        {points.map((point) => {
          const heightPercent = Math.max(8, Math.round((point.recognizedAmount / maxAmount) * 100));
          const label = formatPeriodLabel(point.periodStart, trend.granularity);
          return (
            <div
              key={`${point.periodStart}-${point.recognizedAmount}`}
              className="flex min-w-[3rem] flex-1 flex-col items-center gap-2"
              title={`${label}: ${formatVndPrice(point.recognizedAmount)}`}
            >
              <div className="flex h-40 w-full items-end justify-center">
                <div
                  className="w-full max-w-10 rounded-t-md bg-primary transition-all"
                  style={{ height: `${heightPercent}%` }}
                />
              </div>
              <span className="text-center text-label-sm text-on-surface-variant">{label}</span>
            </div>
          );
        })}
      </div>
    </div>
  );
}
