import { AdminFilterField, AdminFilterSelect } from "../../../components/ui";

export function InvestigationSessionsFilterBar({ statusFilter, statusOptions, onStatusChange }) {
  return (
    <AdminFilterField label="Trạng thái phiên" htmlFor="session-status">
      <AdminFilterSelect
        id="session-status"
        value={statusFilter}
        onChange={(e) => onStatusChange(e.target.value)}
        className="max-w-xs text-base"
      >
        {statusOptions.map((opt) => (
          <option key={opt.value} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </AdminFilterSelect>
    </AdminFilterField>
  );
}
