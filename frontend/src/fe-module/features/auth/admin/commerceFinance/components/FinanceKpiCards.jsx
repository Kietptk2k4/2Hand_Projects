import { Area, AreaChart, ResponsiveContainer } from "recharts";
import { AdminSurfaceCard } from "../../components/ui";
import { formatDeltaPercent } from "../utils/financeOverviewHelpers.js";

function DeltaBadge({ delta }) {
  if (delta == null || !Number.isFinite(delta)) {
    return <span className="text-xs text-admin-text-muted">—</span>;
  }
  const positive = delta > 0;
  const negative = delta < 0;
  return (
    <span
      className={[
        "inline-flex items-center rounded-md px-1.5 py-0.5 text-xs font-semibold tabular-nums",
        positive
          ? "bg-emerald-50 text-emerald-700"
          : negative
            ? "bg-rose-50 text-rose-700"
            : "bg-admin-surface-muted text-admin-text-secondary",
      ].join(" ")}
    >
      {formatDeltaPercent(delta)}
    </span>
  );
}

function MiniSparkline({ data, dataKey = "gmvAmount", gradientId }) {
  if (!data?.length) return null;
  const fillId = gradientId || `sparkFill-${dataKey}`;
  return (
    <div className="mt-3 h-10 w-full">
      <ResponsiveContainer width="100%" height="100%">
        <AreaChart data={data} margin={{ top: 2, right: 0, left: 0, bottom: 0 }}>
          <defs>
            <linearGradient id={fillId} x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="var(--color-admin-accent, #8b7355)" stopOpacity={0.35} />
              <stop offset="100%" stopColor="var(--color-admin-accent, #8b7355)" stopOpacity={0} />
            </linearGradient>
          </defs>
          <Area
            type="monotone"
            dataKey={dataKey}
            stroke="var(--color-admin-accent, #8b7355)"
            fill={`url(#${fillId})`}
            strokeWidth={1.5}
            isAnimationActive={false}
            dot={false}
          />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  );
}

function KpiCard({
  label,
  value,
  hint,
  delta,
  sparkData,
  sparkKey,
  onClick,
}) {
  const interactive = typeof onClick === "function";
  const content = (
    <>
      <div className="flex items-start justify-between gap-2">
        <p className="text-[11px] font-medium uppercase tracking-[0.08em] text-admin-text-muted">
          {label}
        </p>
        <DeltaBadge delta={delta} />
      </div>
      <p className="mt-2 break-words text-xl font-semibold tabular-nums tracking-tight text-admin-text sm:text-2xl">
        {value}
      </p>
      {hint ? <p className="mt-1 text-sm text-admin-text-secondary">{hint}</p> : null}
      <MiniSparkline
        data={sparkData}
        dataKey={sparkKey}
        gradientId={sparkKey ? `sparkFill-${sparkKey}` : undefined}
      />
    </>
  );

  if (interactive) {
    return (
      <button
        type="button"
        onClick={onClick}
        className="rounded-xl border border-admin-border bg-admin-surface-raised p-4 text-left transition-colors hover:border-admin-accent-border hover:bg-admin-accent-soft/20 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
      >
        {content}
      </button>
    );
  }

  return (
    <AdminSurfaceCard padding="md" className="min-w-0">
      {content}
    </AdminSurfaceCard>
  );
}

export function FinanceKpiCards({
  summary,
  deltas,
  feeRate,
  sparkPoints,
  isLoading,
  onNavigateCod,
  onNavigatePayout,
  onNavigateTopSellers,
}) {
  if (isLoading) {
    return (
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-6">
        {Array.from({ length: 6 }, (_, index) => (
          <AdminSurfaceCard key={index} padding="md" className="animate-pulse">
            <div className="h-3 w-24 rounded bg-admin-surface-muted" />
            <div className="mt-4 h-8 w-32 rounded bg-admin-surface-muted" />
            <div className="mt-3 h-10 rounded bg-admin-surface-muted" />
          </AdminSurfaceCard>
        ))}
      </div>
    );
  }

  const formatVnd = (amount) =>
    new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
      maximumFractionDigits: 0,
    }).format(Number(amount) || 0);

  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-6">
      <KpiCard
        label="GMV đã ghi nhận"
        value={formatVnd(summary?.recognizedGmv)}
        hint={`${summary?.recognizedItemCount ?? 0} mặt hàng`}
        delta={deltas?.gmv}
        sparkData={sparkPoints}
        sparkKey="gmvAmount"
        onClick={onNavigateTopSellers}
      />
      <KpiCard
        label="Tổng phí sàn"
        value={formatVnd(summary?.totalPlatformFee)}
        delta={deltas?.fee}
        sparkData={sparkPoints}
        sparkKey="platformFeeAmount"
      />
      <KpiCard
        label="Tỷ lệ phí / GMV"
        value={`${(Math.round((feeRate || 0) * 10) / 10).toFixed(1)}%`}
        hint="Fee take-rate"
        delta={deltas?.feeRate}
      />
      <KpiCard
        label="COD đang pipeline"
        value={formatVnd(summary?.codPipelineAmount)}
        delta={deltas?.cod}
        onClick={onNavigateCod}
      />
      <KpiCard
        label="Payout chờ duyệt"
        value={formatVnd(summary?.pendingPayoutAmount)}
        hint={`${summary?.pendingPayoutCount ?? 0} yêu cầu`}
        delta={deltas?.pendingPayout}
        onClick={onNavigatePayout}
      />
      <KpiCard
        label="Payout đã trả (kỳ)"
        value={formatVnd(summary?.paidPayoutAmount)}
        delta={deltas?.paidPayout}
        onClick={onNavigatePayout}
      />
    </div>
  );
}
