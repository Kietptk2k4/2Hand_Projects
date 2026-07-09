import { AdminSurfaceCard } from "../../components/ui";

export function UserPermissionChipList({ permissions, groupedHint }) {
  if (!permissions?.length) {
    return (
      <AdminSurfaceCard padding="lg" className="text-center">
        <p className="text-sm text-admin-text-muted">Người dùng chưa có quyền nào.</p>
      </AdminSurfaceCard>
    );
  }

  return (
    <AdminSurfaceCard padding="md">
      <div className="flex flex-wrap gap-2">
        {permissions.map((perm) => (
          <span
            key={perm.code}
            className="inline-flex max-w-full break-all rounded-full bg-admin-surface-muted px-3 py-1 text-xs font-medium text-admin-text"
          >
            {perm.code}
          </span>
        ))}
      </div>
      {groupedHint ? (
        <p className="mt-4 text-xs text-admin-text-muted">{groupedHint}</p>
      ) : null}
    </AdminSurfaceCard>
  );
}
