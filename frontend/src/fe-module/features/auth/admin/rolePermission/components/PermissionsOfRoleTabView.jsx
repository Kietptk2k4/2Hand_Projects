import {
  AdminEmptyPanel,
  AdminFilterButton,
  AdminFilterSelect,
  AdminForbiddenPanel,
  AdminErrorPanel,
  AdminListSkeleton,
  AdminPageRefreshButton,
  AdminSurfaceCard,
  AdminWorkspacePageShell,
} from "../../components/ui";
import { RBAC_SECTION_EYEBROW } from "../rbacPageContract.js";
import { RoleCodeBadge } from "./RoleCodeBadge.jsx";
import { RolePermissionList } from "./RolePermissionList.jsx";
import { RolePermissionPicker } from "./RolePermissionPicker.jsx";
import { RbacInlineAlert } from "./ui/RbacInlineAlert.jsx";

export function PermissionsOfRoleTabView({
  title,
  subtitle,
  roles,
  rolesStatus,
  roleId,
  roleMeta,
  status,
  errorMessage,
  permissions,
  availablePermissions,
  selectedPermissionCode,
  isAssigning,
  revokingCode,
  isBusy = false,
  onRoleIdChange,
  onLoadPermissions,
  onPermissionCodeChange,
  onAssignPermission,
  onRevokePermission,
  onBackToRoleList,
  onRetry,
  onRolesRetry,
}) {
  const shellStatus = rolesStatus === "loading" || rolesStatus === "error" ? rolesStatus : "ready";

  return (
    <AdminWorkspacePageShell
      layout="master-detail"
      eyebrow={RBAC_SECTION_EYEBROW}
      title={title}
      subtitle={subtitle}
      status={shellStatus}
      errorMessage="Không tải được danh sách vai trò."
      onRetry={onRolesRetry}
      headerActions={
        roleId ? (
          <AdminPageRefreshButton
            onClick={onLoadPermissions}
            disabled={status === "loading" || isBusy}
            label="Tải lại quyền"
          />
        ) : null
      }
      sidebar={
        <RolePermissionPicker
          roles={roles}
          selectedRoleId={roleId}
          onSelect={onRoleIdChange}
          disabled={status === "loading" || isBusy}
        />
      }
    >
      {!roleId ? (
        <AdminEmptyPanel
          icon="shield_person"
          message="Chưa chọn vai trò"
          hint="Chọn một vai trò để xem, gán hoặc thu hồi quyền."
        />
      ) : null}

      {roleId && status === "loading" ? <AdminListSkeleton rows={6} /> : null}
      {roleId && status === "forbidden" ? (
        <AdminForbiddenPanel message={errorMessage || "Bạn không có quyền truy cập."} />
      ) : null}

      {roleId && status === "not_found" ? (
        <AdminSurfaceCard padding="lg" className="border-admin-danger/30">
          <p className="text-sm text-admin-danger">{errorMessage}</p>
          <AdminFilterButton type="button" variant="secondary" className="mt-4" onClick={onBackToRoleList}>
            Quay lại danh sách vai trò
          </AdminFilterButton>
        </AdminSurfaceCard>
      ) : null}

      {roleId && status === "error" ? (
        <AdminErrorPanel message={errorMessage} onRetry={onRetry} />
      ) : null}

      {roleId && status === "ready" && roleMeta ? (
        <div className="space-y-4">
          <AdminSurfaceCard padding="md">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
              <div className="min-w-0">
                <p className="text-xs font-medium tracking-wide text-admin-text-muted uppercase">
                  Đang chỉnh sửa
                </p>
                <div className="mt-2 flex flex-wrap items-center gap-2">
                  <RoleCodeBadge code={roleMeta.code} />
                  <h2 className="text-lg font-semibold tracking-tight text-balance text-admin-text">
                    {roleMeta.name}
                  </h2>
                </div>
              </div>
              <div className="shrink-0 rounded-lg border border-admin-border bg-admin-surface-raised px-3 py-2 text-right">
                <p className="tabular-nums text-lg font-semibold text-admin-text">
                  {permissions.length}
                </p>
                <p className="text-xs text-admin-text-muted">quyền đã gán</p>
              </div>
            </div>
          </AdminSurfaceCard>

          <AdminSurfaceCard padding="md">
            <p className="text-sm font-semibold text-admin-text">Gán quyền mới</p>
            {availablePermissions.length === 0 ? (
              <p className="mt-2 text-sm text-admin-text-muted">
                Vai trò đã có tất cả quyền trong catalog.
              </p>
            ) : (
              <div className="mt-3 flex flex-col gap-3 sm:flex-row sm:items-end">
                <label className="min-w-0 flex-1" htmlFor="permission-assign-select">
                  <span className="mb-1.5 block text-xs font-medium text-admin-text-secondary">
                    Chọn quyền
                  </span>
                  <AdminFilterSelect
                    id="permission-assign-select"
                    value={selectedPermissionCode}
                    onChange={(event) => onPermissionCodeChange(event.target.value)}
                  >
                    {availablePermissions.map((perm) => (
                      <option key={perm.code} value={perm.code}>
                        {perm.code}
                        {perm.description ? ` — ${perm.description}` : ""}
                      </option>
                    ))}
                  </AdminFilterSelect>
                </label>
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
        </div>
      ) : null}
    </AdminWorkspacePageShell>
  );
}
