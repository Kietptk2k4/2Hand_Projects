import { AdminSurfaceCard } from "./AdminSurfaceCard.jsx";

export function AdminEmptyPanel({
  message,
  hint,
  icon = "inbox",
  action,
  className = "",
}) {
  return (
    <AdminSurfaceCard padding="lg" className={["text-center", className].filter(Boolean).join(" ")}>
      <span
        className="material-symbols-outlined text-3xl text-admin-text-muted"
        aria-hidden="true"
      >
        {icon}
      </span>
      <p className="mt-3 text-sm text-admin-text-secondary">{message}</p>
      {hint ? <p className="mt-1 text-sm text-admin-text-muted">{hint}</p> : null}
      {action ? <div className="mt-4 flex justify-center">{action}</div> : null}
    </AdminSurfaceCard>
  );
}
