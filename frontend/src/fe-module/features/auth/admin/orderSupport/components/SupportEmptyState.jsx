import { AdminSurfaceCard } from "../../components/ui";

export function SupportEmptyState({ message }) {
  return (
    <AdminSurfaceCard padding="lg" className="text-center">
      <p className="text-sm text-admin-text-secondary">{message}</p>
    </AdminSurfaceCard>
  );
}
