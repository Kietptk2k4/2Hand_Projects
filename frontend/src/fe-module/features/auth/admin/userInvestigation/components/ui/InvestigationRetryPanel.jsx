import { AdminFilterButton, AdminSurfaceCard } from "../../../components/ui";

export function InvestigationRetryPanel({ message, onRetry }) {
  return (
    <AdminSurfaceCard padding="lg" className="border-admin-danger/30">
      <p className="text-sm text-admin-danger">{message}</p>
      <AdminFilterButton type="button" variant="primary" className="mt-4" onClick={onRetry}>
        Thử lại
      </AdminFilterButton>
    </AdminSurfaceCard>
  );
}
