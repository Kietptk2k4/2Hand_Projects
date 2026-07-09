import { AdminFilterButton, AdminPageHeader, AdminSurfaceCard } from "../../components/ui";
import { RbacRoleRadioList } from "./RbacRoleRadioList.jsx";
import { RbacSelectedUserSummary } from "./RbacSelectedUserSummary.jsx";
import { RbacInlineAlert } from "./ui/RbacInlineAlert.jsx";
import { RbacListSkeleton } from "./ui/RbacListSkeleton.jsx";
import { RbacRetryPanel } from "./ui/RbacRetryPanel.jsx";

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
}) {
  if (rolesStatus === "loading") {
    return (
      <div className="space-y-4">
        <AdminPageHeader title={title} subtitle={subtitle} />
        <RbacListSkeleton />
      </div>
    );
  }

  if (rolesStatus === "error") {
    return (
      <div className="space-y-4">
        <AdminPageHeader title={title} subtitle={subtitle} />
        <RbacRetryPanel message="Không tải được danh sách vai trò." onRetry={onRolesRetry} />
      </div>
    );
  }

  const revokeEmptyMessage =
    rbacSelectedUserId && assignableRoles.length === 0
      ? "Người dùng chưa có vai trò nào."
      : undefined;

  return (
    <div className="space-y-4">
      <AdminPageHeader title={title} subtitle={subtitle} />

      {globalError ? <RbacInlineAlert variant="error" message={globalError} /> : null}

      <div className="grid gap-6 lg:grid-cols-[minmax(0,1.3fr)_minmax(0,0.7fr)]">
        {userListPanel}

        <AdminSurfaceCard padding="md">
          <form onSubmit={onSubmit} className="space-y-5" noValidate>
            <RbacSelectedUserSummary
              selectedUserId={rbacSelectedUserId}
              selectedUser={selectedUser}
              fieldError={fieldErrors.userId}
            />

            <RbacRoleRadioList
              name="revoke-role"
              roles={assignableRoles}
              selectedRoleId={roleId}
              onSelect={onRoleSelect}
              fieldError={fieldErrors.role_id}
              label="Vai trò cần thu hồi"
              emptyMessage={revokeEmptyMessage}
            />

            <AdminFilterButton
              type="submit"
              variant="primary"
              className="w-full sm:w-auto"
              disabled={isSubmitting || !rbacSelectedUserId || assignableRoles.length === 0}
            >
              Thu hồi vai trò
            </AdminFilterButton>
          </form>
        </AdminSurfaceCard>
      </div>

      {confirmModal}
    </div>
  );
}
