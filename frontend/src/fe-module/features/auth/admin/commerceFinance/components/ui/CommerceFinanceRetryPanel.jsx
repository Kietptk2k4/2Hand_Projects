import { AdminFilterButton, AdminSurfaceCard } from "../../../components/ui";

export function CommerceFinanceRetryPanel({ message, onRetry }) {
  return (
    <AdminSurfaceCard padding="lg" className="border-admin-danger/30">
      <p className="text-sm text-admin-danger">{message}</p>
      {onRetry ? (
        <AdminFilterButton type="button" variant="primary" className="mt-4" onClick={onRetry}>
          Thử lại
        </AdminFilterButton>
      ) : null}
    </AdminSurfaceCard>
  );
}
