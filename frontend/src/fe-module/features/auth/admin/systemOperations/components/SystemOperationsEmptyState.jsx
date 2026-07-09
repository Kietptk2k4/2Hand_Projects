import { AdminSurfaceCard } from "../../components/ui";

export function SystemOperationsEmptyState({ message }) {
  return (
    <AdminSurfaceCard padding="lg" className="border-dashed text-center">
      <p className="text-sm text-admin-text-muted">{message}</p>
    </AdminSurfaceCard>
  );
}
