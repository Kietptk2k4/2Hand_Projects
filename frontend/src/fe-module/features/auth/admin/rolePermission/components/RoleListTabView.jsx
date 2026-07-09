import { AdminPageHeader, AdminSurfaceCard } from "../../components/ui";
import { RoleListTable } from "./RoleListTable.jsx";
import { RbacListSkeleton } from "./ui/RbacListSkeleton.jsx";
import { RbacRetryPanel } from "./ui/RbacRetryPanel.jsx";

export function RoleListTabView({
  title,
  subtitle,
  status,
  errorMessage,
  roles,
  onViewRolePermissions,
  onRetry,
}) {
  return (
    <div className="space-y-4">
      <AdminPageHeader title={title} subtitle={subtitle} />

      {status === "loading" ? <RbacListSkeleton rows={6} /> : null}
      {status === "forbidden" ? <RbacRetryPanel message={errorMessage} /> : null}
      {status === "error" ? <RbacRetryPanel message={errorMessage} onRetry={onRetry} /> : null}

      {status === "ready" ? (
        <AdminSurfaceCard padding="md">
          <RoleListTable roles={roles} onViewRolePermissions={onViewRolePermissions} />
        </AdminSurfaceCard>
      ) : null}
    </div>
  );
}
