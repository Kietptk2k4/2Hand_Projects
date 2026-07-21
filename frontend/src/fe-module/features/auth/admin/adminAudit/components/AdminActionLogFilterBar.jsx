import { useState } from "react";
import {
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
} from "../../components/ui";
import {
  AUDIT_ACTION_OPTIONS,
  AUDIT_STATUS_OPTIONS,
  AUDIT_TARGET_TYPE_OPTIONS,
} from "../constants/adminAuditConstants.js";
import { toIsoInstantFromLocalInput, toLocalInputFromIso } from "../utils/auditDateTime.js";
import { AuditActiveFilterChips } from "./AuditActiveFilterChips.jsx";
import { AuditQuickFilterChips } from "./AuditQuickFilterChips.jsx";

function isValidUuid(value) {
  return /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(
    (value || "").trim(),
  );
}

export function AdminActionLogFilterBar({
  filters,
  onApply,
  onReset,
  onQuickFilter,
  onRemoveFilterChip,
}) {
  const [adminIdError, setAdminIdError] = useState("");

  const handleSubmit = (event) => {
    event.preventDefault();
    const formData = new FormData(event.currentTarget);
    const adminId = String(formData.get("admin_id") || "").trim();
    if (adminId && !isValidUuid(adminId)) {
      setAdminIdError("Admin ID phải là UUID hợp lệ.");
      return;
    }

    setAdminIdError("");
    onApply?.({
      admin_id: adminId,
      action: String(formData.get("action") || "").trim(),
      target_type: String(formData.get("target_type") || "").trim(),
      target_id: String(formData.get("target_id") || "").trim(),
      status: String(formData.get("status") || "").trim(),
      from: toIsoInstantFromLocalInput(String(formData.get("from") || "")),
      to: toIsoInstantFromLocalInput(String(formData.get("to") || "")),
      critical_only: filters?.critical_only || "",
      page: "1",
      size: filters?.size || "20",
    });
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <p className="mb-2 text-xs font-medium text-admin-text-secondary">Lọc nhanh</p>
        <AuditQuickFilterChips filters={filters} onQuickFilter={onQuickFilter} />
      </div>

      <AuditActiveFilterChips filters={filters} onRemoveChip={onRemoveFilterChip} />

      <div className="grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-4">
        <AdminFilterField label="Admin ID" htmlFor="audit-admin-id">
          <AdminFilterInput
            id="audit-admin-id"
            name="admin_id"
            defaultValue={filters?.admin_id || ""}
            placeholder="uuid"
            aria-invalid={Boolean(adminIdError)}
            onChange={() => setAdminIdError("")}
          />
          {adminIdError ? (
            <p className="mt-1 text-xs text-admin-danger">{adminIdError}</p>
          ) : null}
        </AdminFilterField>

        <AdminFilterField label="Hành động" htmlFor="audit-action">
          <AdminFilterSelect
            id="audit-action"
            name="action"
            defaultValue={filters?.action || ""}
          >
            {AUDIT_ACTION_OPTIONS.map((option) => (
              <option key={option.value || "all"} value={option.value}>
                {option.label}
              </option>
            ))}
          </AdminFilterSelect>
        </AdminFilterField>

        <AdminFilterField label="Loại đối tượng" htmlFor="audit-target-type">
          <AdminFilterSelect
            id="audit-target-type"
            name="target_type"
            defaultValue={filters?.target_type || ""}
          >
            {AUDIT_TARGET_TYPE_OPTIONS.map((option) => (
              <option key={option.value || "all"} value={option.value}>
                {option.label}
              </option>
            ))}
          </AdminFilterSelect>
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
      </div>

      <div className="flex flex-col gap-2 sm:flex-row sm:flex-wrap">
        <AdminFilterButton type="submit" variant="primary">
          Áp dụng bộ lọc
        </AdminFilterButton>
        <AdminFilterButton type="button" variant="secondary" onClick={onReset}>
          Xóa bộ lọc
        </AdminFilterButton>
      </div>
    </form>
  );
}
