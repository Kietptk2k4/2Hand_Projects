import { AdminSurfaceCard } from "../../components/ui";

export function InvestigationPermissionNotice({ message }) {
  if (!message) return null;
  return (
    <AdminSurfaceCard padding="md" className="mb-6 border-admin-warning/40 bg-admin-warning-soft">
      <p className="text-sm text-admin-warning">{message}</p>
    </AdminSurfaceCard>
  );
}
