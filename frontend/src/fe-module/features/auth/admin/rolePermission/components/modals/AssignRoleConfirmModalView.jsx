import { AdminFilterButton } from "../../../components/ui";
import { RbacModalShell } from "./RbacModalShell.jsx";

export function AssignRoleConfirmModalView({
  open,
  selectedUser,
  selectedRole,
  isSubmitting,
  onClose,
  onConfirm,
}) {
  return (
    <RbacModalShell
      open={open}
      titleId="assign-role-title"
      title="Gán vai trò cho người dùng?"
      subtitle="Thao tác này sẽ cập nhật quyền truy cập của người dùng."
      onClose={isSubmitting ? undefined : onClose}
      footer={
        <>
          <AdminFilterButton type="button" variant="secondary" disabled={isSubmitting} onClick={onClose}>
            Hủy
          </AdminFilterButton>
          <AdminFilterButton type="button" variant="primary" disabled={isSubmitting} onClick={onConfirm}>
            {isSubmitting ? "Đang xử lý..." : "Xác nhận"}
          </AdminFilterButton>
        </>
      }
    >
      {selectedUser && selectedRole ? (
        <p className="text-sm text-admin-text">
          <span className="font-medium break-all">{selectedUser.email}</span>
          {" → "}
          <span className="font-medium">{selectedRole.code}</span>
        </p>
      ) : null}
    </RbacModalShell>
  );
}
