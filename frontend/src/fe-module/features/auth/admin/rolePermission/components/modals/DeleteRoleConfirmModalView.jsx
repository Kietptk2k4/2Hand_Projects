import { AdminFilterButton } from "../../../components/ui";
import { RoleCodeBadge } from "../RoleCodeBadge.jsx";
import { RbacModalShell } from "./RbacModalShell.jsx";

export function DeleteRoleConfirmModalView({
  open,
  role,
  isSubmitting,
  error,
  onClose,
  onConfirm,
}) {
  return (
    <RbacModalShell
      open={open}
      titleId="delete-role-title"
      title="Xóa vai trò?"
      subtitle="Vai trò sẽ bị xóa vĩnh viễn khỏi hệ thống. Thao tác này không thể hoàn tác."
      onClose={isSubmitting ? undefined : onClose}
      footer={
        <>
          <AdminFilterButton type="button" variant="secondary" disabled={isSubmitting} onClick={onClose}>
            Hủy
          </AdminFilterButton>
          <AdminFilterButton
            type="button"
            variant="primary"
            disabled={isSubmitting}
            className="border-admin-danger bg-admin-danger text-white hover:bg-admin-danger/90"
            onClick={onConfirm}
          >
            {isSubmitting ? "Đang xóa…" : "Xóa vai trò"}
          </AdminFilterButton>
        </>
      }
    >
      {role ? (
        <div className="space-y-3">
          <div className="flex flex-wrap items-center gap-2">
            <RoleCodeBadge code={role.code} />
            <span className="text-sm font-medium text-admin-text">{role.name}</span>
          </div>
          <p className="text-sm text-admin-text-secondary">
            Chỉ xóa được vai trò chưa gán cho người dùng nào.
          </p>
          {error ? <p className="text-sm text-admin-danger">{error}</p> : null}
        </div>
      ) : null}
    </RbacModalShell>
  );
}
