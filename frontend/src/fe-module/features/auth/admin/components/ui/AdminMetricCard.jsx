import { AdminSurfaceCard } from "./AdminSurfaceCard.jsx";

export function AdminMetricCard({ label, value, hint, footer, className = "" }) {
  return (
    <AdminSurfaceCard className={className} padding="md">
      <p className="text-[11px] font-medium uppercase tracking-[0.08em] text-admin-text-muted">{label}</p>
      <p className="mt-2 break-words text-xl font-semibold tabular-nums tracking-tight text-admin-text sm:text-2xl">
        {value}
      </p>
      {hint ? <p className="mt-1 text-sm text-admin-text-secondary">{hint}</p> : null}
      {footer ? <div className="mt-3 border-t border-admin-border-subtle pt-3">{footer}</div> : null}
    </AdminSurfaceCard>
  );
}
