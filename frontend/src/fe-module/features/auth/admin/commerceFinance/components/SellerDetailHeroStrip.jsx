import { formatVndPrice } from "../../../../social/utils/formatPrice";
import { AdminSurfaceCard } from "../../components/ui";

function Kpi({ label, value, hint, isLoading }) {
  if (isLoading) {
    return (
      <AdminSurfaceCard padding="md" className="min-w-0 animate-pulse">
        <div className="h-3 w-24 rounded bg-admin-surface-muted" />
        <div className="mt-4 h-8 w-32 rounded bg-admin-surface-muted" />
      </AdminSurfaceCard>
    );
  }

  return (
    <AdminSurfaceCard padding="md" className="min-w-0">
      <p className="text-[11px] font-medium uppercase tracking-[0.08em] text-admin-text-muted">
        {label}
      </p>
      <p className="mt-2 break-words text-xl font-semibold tabular-nums tracking-tight text-admin-text sm:text-2xl">
        {value}
      </p>
      {hint ? <p className="mt-1 text-sm text-admin-text-secondary">{hint}</p> : null}
    </AdminSurfaceCard>
  );
}

export function SellerDetailHeroStrip({ metrics, isLoading }) {
  const feeRate =
    metrics?.feeRatePeriod != null && Number.isFinite(metrics.feeRatePeriod)
      ? `${(Math.round(metrics.feeRatePeriod * 10) / 10).toFixed(1)}%`
      : "—";

  return (
    <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-5">
      <Kpi
        label="Số dư khả dụng"
        value={formatVndPrice(metrics?.availableBalance)}
        hint="All-time"
        isLoading={isLoading}
      />
      <Kpi
        label="Payout chờ"
        value={formatVndPrice(metrics?.pendingPayout)}
        hint="All-time"
        isLoading={isLoading}
      />
      <Kpi
        label="GMV kỳ"
        value={formatVndPrice(metrics?.periodGmv)}
        hint="Theo khoảng thời gian đã chọn"
        isLoading={isLoading}
      />
      <Kpi
        label="Phí sàn"
        value={formatVndPrice(metrics?.totalPlatformFee)}
        hint="All-time"
        isLoading={isLoading}
      />
      <Kpi
        label="Fee rate (ước lượng)"
        value={feeRate}
        hint="Phí all-time / GMV kỳ"
        isLoading={isLoading}
      />
    </div>
  );
}
