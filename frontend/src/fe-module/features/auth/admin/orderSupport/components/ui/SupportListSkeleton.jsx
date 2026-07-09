import { AdminSurfaceCard } from "../../../components/ui";

export function SupportListSkeleton({ rows = 4 }) {
  return (
    <AdminSurfaceCard padding="lg">
      <div className="space-y-3">
        {Array.from({ length: rows }, (_, index) => (
          <div key={index} className="h-14 animate-pulse rounded-lg bg-admin-surface-muted" />
        ))}
      </div>
    </AdminSurfaceCard>
  );
}
