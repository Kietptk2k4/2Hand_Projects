import { AdminFilterButton, AdminSurfaceCard, AdminWorkspacePageShell } from "../../components/ui";
import { RBAC_SECTION_EYEBROW } from "../rbacPageContract.js";
import { RbacRoleRadioList } from "./RbacRoleRadioList.jsx";
import { RbacSelectedUserSummary } from "./RbacSelectedUserSummary.jsx";
import { RbacInlineAlert } from "./ui/RbacInlineAlert.jsx";

export function AssignRoleTabView({
  title,
  subtitle,
  rolesStatus,
  globalError,
  rbacSelectedUserId,
  selectedUser,
  roles,
  roleId,
  fieldErrors,
  isSubmitting,
  userListPanel,
  confirmModal,
  onRoleSelect,
  onSubmit,
  onRolesRetry,
  onClearUser,
}) {
  const canSubmit = Boolean(rbacSelectedUserId && roleId) && !isSubmitting;
  const disabledHint = !rbacSelectedUserId
    ? "Chọn người dùng và vai trò"
    : !roleId
      ? "Chọn một vai trò để tiếp tục"
      : null;

  return (
    <AdminWorkspacePageShell
      eyebrow={RBAC_SECTION_EYEBROW}
      title={title}
      subtitle={subtitle}
      status={rolesStatus}
      errorMessage="Không tải được danh sách vai trò."
      onRetry={onRolesRetry}
      alert={globalError ? <RbacInlineAlert variant="error" message={globalError} /> : null}
      sidebar={userListPanel}
      modals={confirmModal}
    >
      <AdminSurfaceCard padding="md">
        <form onSubmit={onSubmit} className="space-y-5" noValidate>
          <RbacSelectedUserSummary
            selectedUserId={rbacSelectedUserId}
            selectedUser={selectedUser}
            fieldError={fieldErrors.userId}
            onClear={onClearUser}
          />

          <RbacRoleRadioList
            name="assign-role"
            roles={roles}
            selectedRoleId={roleId}
            onSelect={onRoleSelect}
            fieldError={fieldErrors.role_id}
            label="Vai trò"
            disabled={!rbacSelectedUserId}
          />

          <div>
            <AdminFilterButton
              type="submit"
              variant="primary"
              className="w-full"
              disabled={!canSubmit}
            >
              {isSubmitting ? "Đang gán..." : "Gán vai trò"}
            </AdminFilterButton>
            {!canSubmit && disabledHint ? (
              <p className="mt-2 text-xs text-admin-text-muted">{disabledHint}</p>
            ) : null}
          </div>
        </form>
      </AdminSurfaceCard>
    </AdminWorkspacePageShell>
  );
}
