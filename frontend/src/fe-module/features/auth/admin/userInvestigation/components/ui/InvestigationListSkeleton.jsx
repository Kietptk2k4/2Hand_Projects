import { AdminSurfaceCard } from "../../../components/ui";

export function InvestigationListSkeleton({ rows = 4, bare = false }) {
  const body = (
    <div className="space-y-3">
      {Array.from({ length: rows }, (_, index) => (
        <div key={index} className="h-14 animate-pulse rounded-lg bg-admin-surface-muted" />
      ))}
    </div>
  );

  if (bare) {
    return body;
  }

  return <AdminSurfaceCard padding="lg">{body}</AdminSurfaceCard>;
}
