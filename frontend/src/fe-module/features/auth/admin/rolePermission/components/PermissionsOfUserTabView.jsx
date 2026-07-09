import { AdminPageHeader, AdminSurfaceCard } from "../../components/ui";
import { UserPermissionChipList } from "./UserPermissionChipList.jsx";
import { RbacListSkeleton } from "./ui/RbacListSkeleton.jsx";
import { RbacRetryPanel } from "./ui/RbacRetryPanel.jsx";

export function PermissionsOfUserTabView({
  title,
  subtitle,
  rbacSelectedUserId,
  selectedUser,
  resolvedUserId,
  status,
  errorMessage,
  permissions,
  groupedHint,
  userListPanel,
}) {
  return (
    <div className="space-y-4">
      <AdminPageHeader title={title} subtitle={subtitle} />

      <div className="grid gap-6 lg:grid-cols-[minmax(0,1.3fr)_minmax(0,0.7fr)]">
        {userListPanel}

        <div className="space-y-4">
          {!rbacSelectedUserId ? (
            <AdminSurfaceCard padding="md">
              <p className="text-sm text-admin-text-muted">
                Chọn một người dùng từ danh sách phía trên để xem quyền.
              </p>
            </AdminSurfaceCard>
          ) : null}

          {status === "loading" ? <RbacListSkeleton rows={4} /> : null}
          {status === "forbidden" || status === "not_found" || status === "error" ? (
            <RbacRetryPanel message={errorMessage} />
          ) : null}

          {status === "ready" ? (
            <>
              <AdminSurfaceCard padding="md">
                <p className="text-sm text-admin-text-secondary">User ID</p>
                <p className="mt-1 break-all font-mono text-sm text-admin-text">{resolvedUserId}</p>
                {selectedUser ? (
                  <p className="mt-2 text-sm text-admin-text-secondary">Email: {selectedUser.email}</p>
                ) : null}
              </AdminSurfaceCard>

              <UserPermissionChipList permissions={permissions} groupedHint={groupedHint} />
            </>
          ) : null}
        </div>
      </div>
    </div>
  );
}
