import { AdminFilterButton, AdminFilterField, AdminFilterInput } from "../../../components/ui";
import { RbacModalShell } from "./RbacModalShell.jsx";

const ROLE_CODE_PATTERN = /^[A-Z][A-Z0-9_]{1,31}$/;

export function validateRoleForm({ mode, code, name }) {
  const trimmedName = name?.trim() || "";
  if (!trimmedName) {
    return "Tên vai trò không được để trống.";
  }

  if (mode === "create") {
    const normalizedCode = (code || "").trim().toUpperCase();
    if (!normalizedCode) {
      return "Mã vai trò không được để trống.";
    }
    if (!ROLE_CODE_PATTERN.test(normalizedCode)) {
      return "Mã vai trò phải viết hoa, bắt đầu bằng chữ cái và chỉ gồm chữ, số, dấu gạch dưới.";
    }
  }

  return "";
}

export function RoleFormModalView({
  open,
  mode = "create",
  code,
  name,
  error,
  isSubmitting,
  onCodeChange,
  onNameChange,
  onClose,
  onSubmit,
}) {
  const isEdit = mode === "edit";
  const title = isEdit ? "Sửa vai trò" : "Tạo vai trò mới";
  const subtitle = isEdit
    ? "Chỉ có thể đổi tên hiển thị. Mã vai trò giữ nguyên."
    : "Mã vai trò dùng trong hệ thống và không thể đổi sau khi tạo.";
  const submitLabel = isEdit ? "Lưu thay đổi" : "Tạo vai trò";

  return (
    <RbacModalShell
      open={open}
      titleId="role-form-title"
      title={title}
      subtitle={subtitle}
      onClose={isSubmitting ? undefined : onClose}
      maxWidthClass="max-w-lg"
      footer={
        <>
          <AdminFilterButton type="button" variant="secondary" disabled={isSubmitting} onClick={onClose}>
            Hủy
          </AdminFilterButton>
          <AdminFilterButton
            type="submit"
            form="role-form"
            variant="primary"
            disabled={isSubmitting}
          >
            {isSubmitting ? "Đang lưu…" : submitLabel}
          </AdminFilterButton>
        </>
      }
    >
      <form
        id="role-form"
        className="space-y-4"
        onSubmit={(event) => {
          event.preventDefault();
          onSubmit?.();
        }}
      >
        <AdminFilterField label="Mã vai trò" htmlFor="role-code">
          <AdminFilterInput
            id="role-code"
            value={code}
            onChange={(event) => onCodeChange(event.target.value)}
            placeholder="VD: SUPPORT"
            disabled={isEdit || isSubmitting}
            readOnly={isEdit}
            className={isEdit ? "bg-admin-surface-muted text-admin-text-muted" : ""}
            autoComplete="off"
            spellCheck={false}
          />
        </AdminFilterField>
        <AdminFilterField label="Tên hiển thị" htmlFor="role-name">
          <AdminFilterInput
            id="role-name"
            value={name}
            onChange={(event) => onNameChange(event.target.value)}
            placeholder="VD: Đội hỗ trợ"
            disabled={isSubmitting}
            required
          />
        </AdminFilterField>
        {error ? <p className="text-sm text-admin-danger">{error}</p> : null}
      </form>
    </RbacModalShell>
  );
}
