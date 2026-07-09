import { AdminSurfaceCard } from "../../components/ui";

export function AuditForbiddenState({ message }) {
  return (
    <AdminSurfaceCard padding="md" className="border-admin-warning/40 bg-admin-warning-soft">
      <p className="text-sm text-admin-warning">{message}</p>
    </AdminSurfaceCard>
  );
}
