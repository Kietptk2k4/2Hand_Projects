import { useState } from "react";
import { formatVndPrice } from "../../../../../social/utils/formatPrice";
import { SellerRevenueBucketCards } from "../../../../../commerce/components/SellerRevenueBucketCards";
import { useAdminFinanceOverview } from "../../hooks/useAdminFinanceOverview";
import { AccountCard, AccountSkeleton, TabPanelHeader } from "../../../../../../shared/ui/auth/authUi.jsx";
import { ErrorState } from "../../../../../../shared/ui/PageState.jsx";

const GRANULARITY_OPTIONS = [
  { value: "DAY", label: "Theo ngày" },
  { value: "WEEK", label: "Theo tuần" },
  { value: "MONTH", label: "Theo tháng" },
];

function PlatformTrendChart({ trend, isLoading }) {
  if (isLoading) {
    return <div className="mt-6 h-52 animate-pulse rounded-xl border border-outline-variant bg-surface-container-low" />;
  }
  const points = trend?.points ?? [];
  const maxAmount = Math.max(...points.map((p) => p.gmvAmount), 1);
  if (!points.length) {
    return (
      <p className="mt-6 text-body-md text-on-surface-variant">
        Chưa có dữ liệu GMV trong khoảng thời gian này.
      </p>
    );
  }
  return (
    <div className="mt-6 overflow-x-auto rounded-xl border border-outline-variant bg-surface-container-lowest p-4">
      <div className="mb-3 text-title-md font-semibold text-on-surface">GMV & phí sàn đã ghi nhận</div>
      <div className="flex h-48 items-end gap-2">
        {points.map((point) => {
          const height = Math.max(8, Math.round((point.gmvAmount / maxAmount) * 100));
          return (
            <div key={point.periodStart} className="flex min-w-[48px] flex-col items-center gap-1">
              <div className="flex h-40 w-8 flex-col justify-end">
                <div className="w-full rounded-t bg-primary" style={{ height: `${height}%` }} title={formatVndPrice(point.gmvAmount)} />
              </div>
              <span className="text-[10px] text-on-surface-variant">
                {point.periodStart ? new Date(point.periodStart).toLocaleDateString("vi-VN", { day: "2-digit", month: "2-digit" }) : ""}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export function AdminFinanceOverviewTab() {
  const [granularity, setGranularity] = useState("DAY");
  const { summary, trend, isLoading, errorMessage, retry } = useAdminFinanceOverview({ granularity });

  if (errorMessage) {
    return <ErrorState message={errorMessage} onRetry={retry} />;
  }

  return (
    <AccountCard>
      <TabPanelHeader
        title="Tổng quan tài chính sàn"
        subtitle="GMV đã ghi nhận, phí sàn, pipeline COD và payout (mặc định 30 ngày)."
      />
      <div className="mb-4 flex justify-end">
        <button type="button" onClick={retry} disabled={isLoading} className="rounded-lg border border-outline-variant px-3 py-2 text-label-md hover:bg-surface-container-high disabled:opacity-60">
          Làm mới
        </button>
      </div>

      {isLoading ? (
        <AccountSkeleton rows={3} />
      ) : (
        <div className="mb-8 grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          <div className="rounded-xl border border-outline-variant bg-surface-container-low p-4">
            <p className="text-label-md text-on-surface-variant">GMV đã ghi nhận</p>
            <p className="mt-2 text-headline-sm font-bold">{formatVndPrice(summary?.recognizedGmv ?? 0)}</p>
            <p className="text-body-sm text-on-surface-variant">{summary?.recognizedItemCount ?? 0} mặt hàng</p>
          </div>
          <div className="rounded-xl border border-outline-variant bg-surface-container-low p-4">
            <p className="text-label-md text-on-surface-variant">Tổng phí sàn</p>
            <p className="mt-2 text-headline-sm font-bold">{formatVndPrice(summary?.totalPlatformFee ?? 0)}</p>
          </div>
          <div className="rounded-xl border border-outline-variant bg-surface-container-low p-4">
            <p className="text-label-md text-on-surface-variant">COD đang pipeline</p>
            <p className="mt-2 text-headline-sm font-bold">{formatVndPrice(summary?.codPipelineAmount ?? 0)}</p>
          </div>
          <div className="rounded-xl border border-outline-variant bg-surface-container-low p-4">
            <p className="text-label-md text-on-surface-variant">Payout chờ duyệt</p>
            <p className="mt-2 text-headline-sm font-bold">{formatVndPrice(summary?.pendingPayoutAmount ?? 0)}</p>
            <p className="text-body-sm text-on-surface-variant">{summary?.pendingPayoutCount ?? 0} yêu cầu</p>
          </div>
          <div className="rounded-xl border border-outline-variant bg-surface-container-low p-4">
            <p className="text-label-md text-on-surface-variant">Payout đã trả (kỳ)</p>
            <p className="mt-2 text-headline-sm font-bold">{formatVndPrice(summary?.paidPayoutAmount ?? 0)}</p>
          </div>
        </div>
      )}

      <div className="mb-4 flex flex-wrap gap-2">
        {GRANULARITY_OPTIONS.map((option) => (
          <button
            key={option.value}
            type="button"
            onClick={() => setGranularity(option.value)}
            className={[
              "rounded-full px-4 py-2 text-label-md",
              granularity === option.value ? "bg-primary text-on-primary" : "border border-outline-variant text-on-surface-variant",
            ].join(" ")}
          >
            {option.label}
          </button>
        ))}
      </div>

      <PlatformTrendChart trend={trend} isLoading={isLoading} />
    </AccountCard>
  );
}
