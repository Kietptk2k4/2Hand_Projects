import { AdminSurfaceCard } from "../../components/ui";

export function SystemOperationsForbiddenState({ message }) {
  return (
    <AdminSurfaceCard padding="lg" className="border-admin-danger/30 text-center">
      <p className="text-sm text-admin-danger">{message}</p>
    </AdminSurfaceCard>
  );
}
