import { AdminFilterButton, AdminSurfaceCard, AdminWorkspacePageShell } from "../../components/ui";
import { RBAC_SECTION_EYEBROW } from "../rbacPageContract.js";
import { RbacRoleRadioList } from "./RbacRoleRadioList.jsx";
import { RbacSelectedUserSummary } from "./RbacSelectedUserSummary.jsx";
import { RbacInlineAlert } from "./ui/RbacInlineAlert.jsx";

export function RevokeRoleTabView({
  title,
  subtitle,
  rolesStatus,
  globalError,
  rbacSelectedUserId,
  selectedUser,
  assignableRoles,
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
  const revokeEmptyMessage =
    rbacSelectedUserId && assignableRoles.length === 0
      ? "Người dùng chưa có vai trò nào."
      : undefined;

  const canSubmit =
    Boolean(rbacSelectedUserId && roleId) &&
    assignableRoles.length > 0 &&
    !isSubmitting;

  const disabledHint = !rbacSelectedUserId
    ? "Chọn người dùng và vai trò"
    : assignableRoles.length === 0
      ? "Người dùng chưa có vai trò để thu hồi"
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
            name="revoke-role"
            roles={assignableRoles}
            selectedRoleId={roleId}
            onSelect={onRoleSelect}
            fieldError={fieldErrors.role_id}
            label="Vai trò cần thu hồi"
            emptyMessage={revokeEmptyMessage}
            disabled={!rbacSelectedUserId}
          />

          <div>
            <AdminFilterButton
              type="submit"
              variant="primary"
              className="w-full"
              disabled={!canSubmit}
            >
              {isSubmitting ? "Đang thu hồi..." : "Thu hồi vai trò"}
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
