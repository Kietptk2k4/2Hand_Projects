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
      window.alert("admin_id phai la UUID hop le.");
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
    <form onSubmit={handleSubmit} className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
      <div>
        <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Admin ID</label>
        <input
          name="admin_id"
          defaultValue={filters?.admin_id || ""}
          placeholder="uuid"
          className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
        />
      </div>
      <div>
        <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Action</label>
        <input
          name="action"
          defaultValue={filters?.action || ""}
          placeholder="USER_SUSPEND"
          className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
        />
      </div>
      <div>
        <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Target type</label>
        <input
          name="target_type"
          defaultValue={filters?.target_type || ""}
          placeholder="USER"
          className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
        />
      </div>
      <div>
        <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Target ID</label>
        <input
          name="target_id"
          defaultValue={filters?.target_id || ""}
          placeholder="uuid / objectId"
          className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
        />
      </div>
      <div>
        <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Status</label>
        <select
          name="status"
          defaultValue={filters?.status || ""}
          className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
        >
          {AUDIT_STATUS_OPTIONS.map((option) => (
            <option key={option.value || "all"} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>
      <div>
        <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Tu (ISO local)</label>
        <input
          type="datetime-local"
          name="from"
          defaultValue={toLocalInputFromIso(filters?.from)}
          className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
        />
      </div>
      <div>
        <label className="mb-1 block text-xs font-semibold text-on-surface-variant">Den (ISO local)</label>
        <input
          type="datetime-local"
          name="to"
          defaultValue={toLocalInputFromIso(filters?.to)}
          className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2 text-sm outline-none focus:border-primary"
        />
      </div>
      <div className="flex items-end gap-2 md:col-span-2 lg:col-span-3">
        <button
          type="submit"
          className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white hover:opacity-90"
        >
          Ap dung bo loc
        </button>
        <button
          type="button"
          onClick={onReset}
          className="rounded-lg border border-outline-variant px-4 py-2 text-sm font-medium text-on-surface-variant hover:bg-surface-container-low"
        >
          Xoa bo loc
        </button>
      </div>
    </form>
  );
}