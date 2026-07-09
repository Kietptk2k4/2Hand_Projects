import { AdminFilterButton } from "../../../components/ui";
import { RbacModalShell } from "./RbacModalShell.jsx";

export function RevokeRoleConfirmModalView({
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
      titleId="revoke-role-title"
      title="Thu hồi vai trò?"
      subtitle="Người dùng có thể mất quyền truy cập liên quan đến vai trò này."
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
