import {
  AdminFilterBar,
  AdminFilterButton,
  AdminFilterField,
  AdminFilterSelect,
} from "../../components/ui";

const STATUS_OPTIONS = [
  { value: "", label: "Tất cả" },
  { value: "REQUESTED", label: "Chờ duyệt" },
  { value: "APPROVED", label: "Đã duyệt" },
  { value: "PAID", label: "Đã chuyển" },
  { value: "REJECTED", label: "Từ chối" },
  { value: "CANCELLED", label: "Đã hủy" },
];

export function PayoutQueueFilterBar({ statusFilter, loading, onStatusChange, onRefresh }) {
  return (
    <AdminFilterBar
      className="!grid-cols-1 sm:!grid-cols-2 lg:!grid-cols-3"
      actions={
        <AdminFilterButton type="button" variant="secondary" disabled={loading} onClick={onRefresh}>
          Làm mới
        </AdminFilterButton>
      }
    >
      <AdminFilterField label="Trạng thái" htmlFor="payout-status-filter" className="sm:col-span-1">
        <AdminFilterSelect
          id="payout-status-filter"
          className="text-base"
          value={statusFilter}
          onChange={(event) => onStatusChange?.(event.target.value)}
        >
          {STATUS_OPTIONS.map((option) => (
            <option key={option.value || "all"} value={option.value}>
              {option.label}
            </option>
          ))}
        </AdminFilterSelect>
      </AdminFilterField>
    </AdminFilterBar>
  );
}
