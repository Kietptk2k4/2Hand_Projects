import { AdminSurfaceCard } from "../../components/ui";

export function SupportForbiddenState({ message }) {
  if (!message) return null;

  return (
    <AdminSurfaceCard padding="lg" className="border-admin-warning/30 bg-admin-warning-soft/40">
      <p className="text-sm text-admin-text">{message}</p>
    </AdminSurfaceCard>
  );
}
