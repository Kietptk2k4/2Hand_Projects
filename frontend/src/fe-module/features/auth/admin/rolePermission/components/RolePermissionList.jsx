import { AdminFilterButton, AdminSurfaceCard } from "../../components/ui";

export function RolePermissionList({
  permissions,
  revokingCode,
  onRevoke,
}) {
  if (!permissions?.length) {
    return (
      <AdminSurfaceCard padding="lg" className="text-center">
        <p className="text-sm text-admin-text-muted">Vai trò chưa có quyền.</p>
      </AdminSurfaceCard>
    );
  }

  return (
    <AdminSurfaceCard padding="none" className="overflow-hidden">
      <ul className="divide-y divide-admin-border-subtle">
        {permissions.map((perm) => (
          <li
            key={perm.code}
            className="flex flex-col gap-3 px-4 py-4 sm:flex-row sm:items-center sm:justify-between sm:px-5"
          >
            <div className="min-w-0">
              <p className="break-all font-medium text-admin-text">{perm.code}</p>
              {perm.description ? (
                <p className="mt-1 text-sm text-admin-text-secondary">{perm.description}</p>
              ) : null}
            </div>
            <AdminFilterButton
              type="button"
              variant="secondary"
              className="w-full min-h-11 sm:w-auto"
              disabled={revokingCode === perm.code}
              onClick={() => onRevoke(perm.code)}
            >
              {revokingCode === perm.code ? "Đang thu hồi..." : "Thu hồi"}
            </AdminFilterButton>
          </li>
        ))}
      </ul>
    </AdminSurfaceCard>
  );
}
