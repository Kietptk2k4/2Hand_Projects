import { AdminFilterButton, AdminPageHeader, AdminSurfaceCard } from "../../components/ui";
import { RbacRoleRadioList } from "./RbacRoleRadioList.jsx";
import { RbacSelectedUserSummary } from "./RbacSelectedUserSummary.jsx";
import { RbacInlineAlert } from "./ui/RbacInlineAlert.jsx";
import { RbacListSkeleton } from "./ui/RbacListSkeleton.jsx";
import { RbacRetryPanel } from "./ui/RbacRetryPanel.jsx";

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
              name="assign-role"
              roles={roles}
              selectedRoleId={roleId}
              onSelect={onRoleSelect}
              fieldError={fieldErrors.role_id}
              label="Vai trò"
            />

            <AdminFilterButton
              type="submit"
              variant="primary"
              className="w-full sm:w-auto"
              disabled={isSubmitting || !rbacSelectedUserId}
            >
              Gán vai trò
            </AdminFilterButton>
          </form>
        </AdminSurfaceCard>
      </div>

      {confirmModal}
    </div>
  );
}
