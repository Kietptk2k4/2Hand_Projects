import { AdminSurfaceCard } from "../../components/ui";

export function FinanceChartPanel({
  title,
  subtitle,
  status,
  errorMessage,
  onRetry,
  empty,
  emptyMessage = "Không có dữ liệu trong kỳ này.",
  children,
  className = "",
}) {
  return (
    <AdminSurfaceCard padding="lg" className={`min-w-0 ${className}`.trim()}>
      <div className="mb-4 min-w-0">
        <h2 className="text-base font-semibold text-admin-text">{title}</h2>
        {subtitle ? (
          <p className="mt-1 text-sm text-admin-text-secondary">{subtitle}</p>
        ) : null}
      </div>

      {status === "loading" ? (
        <div className="h-64 animate-pulse rounded-xl bg-admin-surface-muted" />
      ) : null}

      {status === "error" ? (
        <div className="rounded-xl border border-dashed border-admin-border px-4 py-10 text-center">
          <p className="text-sm text-admin-text-secondary">{errorMessage}</p>
          {onRetry ? (
            <button
              type="button"
              onClick={onRetry}
              className="mt-3 inline-flex min-h-10 items-center rounded-lg border border-admin-border px-3 py-2 text-sm font-medium text-admin-text-secondary hover:bg-admin-surface-muted"
            >
              Thử lại
            </button>
          ) : null}
        </div>
      ) : null}

      {status === "ready" && empty ? (
        <div className="rounded-xl border border-dashed border-admin-border px-4 py-12 text-center">
          <span
            className="material-symbols-outlined text-[36px] text-admin-text-muted"
            aria-hidden="true"
          >
            monitoring
          </span>
          <p className="mt-3 text-sm text-admin-text-secondary">{emptyMessage}</p>
        </div>
      ) : null}

      {status === "ready" && !empty ? children : null}
    </AdminSurfaceCard>
  );
}
