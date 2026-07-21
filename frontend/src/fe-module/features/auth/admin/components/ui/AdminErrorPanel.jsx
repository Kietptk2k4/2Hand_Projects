import { AdminFilterButton } from "./AdminFilterBar.jsx";
import { AdminSurfaceCard } from "./AdminSurfaceCard.jsx";

export function AdminErrorPanel({
  message,
  errorCode,
  onRetry,
  retryLabel = "Thử lại",
  className = "",
}) {
  return (
    <AdminSurfaceCard padding="lg" className={["border-admin-danger/30", className].filter(Boolean).join(" ")}>
      {errorCode ? (
        <p className="font-mono text-xs uppercase tracking-wide text-admin-danger/80">{errorCode}</p>
      ) : null}
      <p className={errorCode ? "mt-2 text-sm text-admin-danger" : "text-sm text-admin-danger"}>{message}</p>
      {onRetry ? (
        <AdminFilterButton type="button" variant="primary" className="mt-4" onClick={onRetry}>
          {retryLabel}
        </AdminFilterButton>
      ) : null}
    </AdminSurfaceCard>
  );
}
