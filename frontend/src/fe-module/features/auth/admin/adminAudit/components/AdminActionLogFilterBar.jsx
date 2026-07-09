import {
  AdminFilterBar,
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
} from "../../components/ui";
import { AUDIT_STATUS_OPTIONS } from "../constants/adminAuditConstants.js";
import { toIsoInstantFromLocalInput, toLocalInputFromIso } from "../utils/auditDateTime.js";

function isValidUuid(value) {
  return /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(
    (value || "").trim(),
  );
}

export function AdminActionLogFilterBar({ filters, onApply, onReset }) {
  const handleSubmit = (event) => {
    event.preventDefault();
    const formData = new FormData(event.currentTarget);
    const adminId = String(formData.get("admin_id") || "").trim();
    if (adminId && !isValidUuid(adminId)) {
      window.alert("admin_id phải là UUID hợp lệ.");
      return;
    }

    onApply?.({
      admin_id: adminId,
      action: String(formData.get("action") || "").trim(),
      target_type: String(formData.get("target_type") || "").trim(),
      target_id: String(formData.get("target_id") || "").trim(),
      status: String(formData.get("status") || "").trim(),
      from: toIsoInstantFromLocalInput(String(formData.get("from") || "")),
      to: toIsoInstantFromLocalInput(String(formData.get("to") || "")),
      page: "1",
      size: filters?.size || "20",
    });
  };

  return (
    <AdminFilterBar
      onSubmit={handleSubmit}
      className="md:grid-cols-2 lg:grid-cols-3"
      actions={
        <>
          <AdminFilterButton type="submit" variant="primary">
            Áp dụng bộ lọc
          </AdminFilterButton>
          <AdminFilterButton type="button" variant="secondary" onClick={onReset}>
            Xóa bộ lọc
          </AdminFilterButton>
        </>
      }
    >
      <AdminFilterField label="Admin ID" htmlFor="audit-admin-id">
        <AdminFilterInput
          id="audit-admin-id"
          name="admin_id"
          defaultValue={filters?.admin_id || ""}
          placeholder="uuid"
        />
      </AdminFilterField>
      <AdminFilterField label="Action" htmlFor="audit-action">
        <AdminFilterInput
          id="audit-action"
          name="action"
          defaultValue={filters?.action || ""}
          placeholder="USER_SUSPEND"
        />
      </AdminFilterField>
      <AdminFilterField label="Target type" htmlFor="audit-target-type">
        <AdminFilterInput
          id="audit-target-type"
          name="target_type"
          defaultValue={filters?.target_type || ""}
          placeholder="USER"
        />
      </AdminFilterField>
      <AdminFilterField label="Target ID" htmlFor="audit-target-id">
        <AdminFilterInput
          id="audit-target-id"
          name="target_id"
          defaultValue={filters?.target_id || ""}
          placeholder="uuid / objectId"
        />
      </AdminFilterField>
      <AdminFilterField label="Trạng thái" htmlFor="audit-status">
        <AdminFilterSelect id="audit-status" name="status" defaultValue={filters?.status || ""}>
          {AUDIT_STATUS_OPTIONS.map((option) => (
            <option key={option.value || "all"} value={option.value}>
              {option.label}
            </option>
          ))}
        </AdminFilterSelect>
      </AdminFilterField>
      <AdminFilterField label="Từ" htmlFor="audit-from">
        <AdminFilterInput
          id="audit-from"
          type="datetime-local"
          name="from"
          defaultValue={toLocalInputFromIso(filters?.from)}
        />
      </AdminFilterField>
      <AdminFilterField label="Đến" htmlFor="audit-to">
        <AdminFilterInput
          id="audit-to"
          type="datetime-local"
          name="to"
          defaultValue={toLocalInputFromIso(filters?.to)}
        />
      </AdminFilterField>
    </AdminFilterBar>
  );
}
