import { formatVndPrice } from "../../../../social/utils/formatPrice";
import { AdminSurfaceCard } from "../../components/ui";
import { PAYOUT_HERO_KPI_META } from "../utils/payoutQueueHelpers.js";

function KpiCard({ meta, count, amount, isLoading, isActive, onClick }) {
  if (isLoading) {
    return (
      <AdminSurfaceCard padding="md" className="min-w-0 animate-pulse">
        <div className="h-3 w-24 rounded bg-admin-surface-muted" />
        <div className="mt-4 h-8 w-32 rounded bg-admin-surface-muted" />
      </AdminSurfaceCard>
    );
  }

  const interactive = typeof onClick === "function";

  return (
    <AdminSurfaceCard
      padding="md"
      className={[
        "min-w-0 text-left transition-colors",
        interactive ? "cursor-pointer hover:border-admin-accent-border hover:bg-admin-surface-muted/40" : "",
        isActive ? "ring-2 ring-admin-accent-soft" : "",
      ].join(" ")}
    >
      <button
        type="button"
        className="w-full text-left focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
        onClick={onClick}
        disabled={!interactive}
        aria-pressed={isActive}
      >
        <div className="flex items-start justify-between gap-2">
          <div>
            <p className="text-[11px] font-medium uppercase tracking-[0.08em] text-admin-text-muted">
              {meta.label}
            </p>
            <p className="mt-1 text-xs text-admin-text-secondary">{meta.hint}</p>
          </div>
          <span
            className={[
              "material-symbols-outlined flex h-9 w-9 shrink-0 items-center justify-center rounded-lg text-xl",
              meta.accentClass,
              meta.surfaceClass,
            ].join(" ")}
            aria-hidden="true"
          >
            {meta.icon}
          </span>
        </div>
        <p className="mt-4 break-words text-2xl font-semibold tabular-nums tracking-tight text-admin-text sm:text-[1.65rem]">
          {formatVndPrice(amount)}
        </p>
        <p className="mt-1 text-sm text-admin-text-secondary">
          {meta.key === "TOTAL" ? `${count} yêu cầu trong kỳ` : `${count} yêu cầu`}
        </p>
      </button>
    </AdminSurfaceCard>
  );
}

export function PayoutQueueHeroStrip({
  metrics,
  activeStatusFilter,
  isLoading,
  onStatusClick,
}) {
  const valueByKey = {
    REQUESTED: metrics?.requested,
    APPROVED: metrics?.approved,
    PAID: metrics?.paid,
    TOTAL: metrics?.total,
  };

  return (
    <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-4">
      {PAYOUT_HERO_KPI_META.map((meta) => {
        const bucket = valueByKey[meta.key] || { count: 0, amount: 0 };
        const isActive =
          meta.key === "TOTAL"
            ? !activeStatusFilter
            : activeStatusFilter === meta.key;

        return (
          <KpiCard
            key={meta.key}
            meta={meta}
            count={bucket.count}
            amount={bucket.amount}
            isLoading={isLoading}
            isActive={isActive}
            onClick={() => onStatusClick?.(meta.key)}
          />
        );
      })}
    </div>
  );
}
