import { AdminSurfaceCard } from "./AdminSurfaceCard.jsx";

export function AdminForbiddenPanel({ message, action, icon = "lock", className = "" }) {
  return (
    <AdminSurfaceCard
      padding="lg"
      className={["border-admin-warning/40 bg-admin-warning-soft", className].filter(Boolean).join(" ")}
    >
      <span className="material-symbols-outlined text-2xl text-admin-warning" aria-hidden="true">
        {icon}
      </span>
      <p className="mt-3 text-sm text-admin-warning">{message}</p>
      {action ? <div className="mt-4">{action}</div> : null}
    </AdminSurfaceCard>
  );
}
