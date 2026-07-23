import { formatVndPrice } from "../../../../social/utils/formatPrice";
import { AdminSurfaceCard } from "../../components/ui";

function formatShare(value) {
  if (!Number.isFinite(value)) return "—";
  return `${(Math.round(value * 10) / 10).toFixed(1)}%`;
}

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

export function TopSellersHeroStrip({ metrics, isLoading }) {
  const n = metrics?.count || 0;

  return (
    <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-4">
      <Kpi
        label={`GMV Top ${n || "N"}`}
        value={formatVndPrice(metrics?.totalGross)}
        hint={`${metrics?.totalItems ?? 0} dòng hoàn tất · trong Top ${n}`}
        isLoading={isLoading}
      />
      <Kpi
        label="Phí sàn (Top N)"
        value={formatVndPrice(metrics?.totalFee)}
        hint="Tổng phí trong danh sách"
        isLoading={isLoading}
      />
      <Kpi
        label="Fee rate"
        value={`${(Math.round((metrics?.feeRate || 0) * 10) / 10).toFixed(1)}%`}
        hint="Phí / GMV trong Top N"
        isLoading={isLoading}
      />
      <Kpi
        label="#1 share"
        value={formatShare(metrics?.topShare)}
        hint={metrics?.topShopName ? `Lead: ${metrics.topShopName}` : "Tỷ trọng seller #1"}
        isLoading={isLoading}
      />
    </div>
  );
}
