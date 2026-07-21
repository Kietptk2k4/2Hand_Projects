import { AdminListSkeleton, AdminSurfaceCard } from "../../../components/ui";

export function RbacListSkeleton({ rows = 5, bare = false }) {
  if (bare) {
    return <AdminListSkeleton rows={rows} />;
  }

  return (
    <AdminSurfaceCard padding="md">
      <AdminListSkeleton rows={rows} />
    </AdminSurfaceCard>
  );
}
