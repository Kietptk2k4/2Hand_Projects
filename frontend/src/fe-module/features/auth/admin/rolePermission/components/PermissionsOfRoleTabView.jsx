import {
  AdminFilterButton,
  AdminFilterField,
  AdminFilterSelect,
  AdminPageHeader,
  AdminSurfaceCard,
} from "../../components/ui";
import { RolePermissionList } from "./RolePermissionList.jsx";
import { RbacInlineAlert } from "./ui/RbacInlineAlert.jsx";
import { RbacListSkeleton } from "./ui/RbacListSkeleton.jsx";
import { RbacRetryPanel } from "./ui/RbacRetryPanel.jsx";

export function PermissionsOfRoleTabView({
  title,
  subtitle,
  roles,
  roleId,
  roleMeta,
  status,
  errorMessage,
  permissions,
  availablePermissions,
  selectedPermissionCode,
  isAssigning,
  revokingCode,
  onRoleIdChange,
  onLoadPermissions,
  onPermissionCodeChange,
  onAssignPermission,
  onRevokePermission,
  onBackToRoleList,
  onRetry,
}) {
  return (
    <div className="space-y-4">
      <AdminPageHeader title={title} subtitle={subtitle} />

      <AdminSurfaceCard padding="md">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-end">
          <AdminFilterField label="Chọn vai trò" htmlFor="role-perm-select" className="min-w-0 flex-1">
            <AdminFilterSelect
              id="role-perm-select"
              className="text-base"
              value={roleId}
              onChange={(e) => onRoleIdChange(e.target.value)}
            >
              <option value="">Chọn vai trò...</option>
              {roles.map((role) => (
                <option key={role.id} value={role.id}>
                  {role.code} — {role.name}
                </option>
              ))}
            </AdminFilterSelect>
          </AdminFilterField>
          <AdminFilterButton
            type="button"
            variant="primary"
            className="w-full min-h-11 sm:w-auto"
            disabled={!roleId}
            onClick={onLoadPermissions}
          >
            Tải permission
          </AdminFilterButton>
        </div>
      </AdminSurfaceCard>

      {status === "loading" ? <RbacListSkeleton rows={5} /> : null}
      {status === "forbidden" ? <RbacRetryPanel message={errorMessage} /> : null}

      {status === "not_found" ? (
        <AdminSurfaceCard padding="lg" className="border-admin-danger/30">
          <p className="text-sm text-admin-danger">{errorMessage}</p>
          <AdminFilterButton type="button" variant="secondary" className="mt-4" onClick={onBackToRoleList}>
            Quay lại danh sách vai trò
          </AdminFilterButton>
        </AdminSurfaceCard>
      ) : null}

      {status === "error" ? <RbacRetryPanel message={errorMessage} onRetry={onRetry} /> : null}

      {status === "ready" && roleMeta ? (
        <>
          <AdminSurfaceCard padding="md">
            <p className="text-sm text-admin-text-secondary">Vai trò</p>
            <p className="mt-1 text-lg font-semibold text-admin-text">
              {roleMeta.code} — {roleMeta.name}
            </p>
          </AdminSurfaceCard>

          <AdminSurfaceCard padding="md">
            <p className="text-sm font-semibold text-admin-text">Gán quyền mới</p>
            {availablePermissions.length === 0 ? (
              <p className="mt-2 text-sm text-admin-text-muted">
                Vai trò đã có tất cả quyền trong catalog.
              </p>
            ) : (
              <div className="mt-3 flex flex-col gap-3 sm:flex-row sm:items-end">
                <AdminFilterField
                  label="Chọn quyền"
                  htmlFor="permission-assign-select"
                  className="min-w-0 flex-1"
                >
                  <AdminFilterSelect
                    id="permission-assign-select"
                    className="text-base"
                    value={selectedPermissionCode}
                    onChange={(e) => onPermissionCodeChange(e.target.value)}
                  >
                    {availablePermissions.map((perm) => (
                      <option key={perm.code} value={perm.code}>
                        {perm.code}
                        {perm.description ? ` — ${perm.description}` : ""}
                      </option>
                    ))}
                  </AdminFilterSelect>
                </AdminFilterField>
                <AdminFilterButton
                  type="button"
                  variant="primary"
                  className="w-full min-h-11 sm:w-auto"
                  disabled={!selectedPermissionCode || isAssigning}
                  onClick={onAssignPermission}
                >
                  {isAssigning ? "Đang gán..." : "Gán quyền"}
                </AdminFilterButton>
              </div>
            )}
            <div className="mt-4">
              <RbacInlineAlert
                variant="info"
                message="Sau khi gán hoặc thu hồi quyền, người dùng thuộc vai trò cần đăng xuất và đăng nhập lại để JWT nhận quyền mới."
              />
            </div>
          </AdminSurfaceCard>

          <RolePermissionList
            permissions={permissions}
            revokingCode={revokingCode}
            onRevoke={onRevokePermission}
          />
        </>
      ) : null}
    </div>
  );
}
