import {
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
} from "../../components/ui";
import { INVESTIGATION_USER_LIST_SORT_OPTIONS } from "../constants/investigationUserListConstants.js";
import { getUserStatusLabel } from "../utils/investigationLabels.js";

const SORT_DISPLAY_LABELS = {
  created_at: "Ngày tạo (mới nhất)",
  email: "Email (A–Z)",
  display_name: "Tên hiển thị (A–Z)",
  status: "Trạng thái",
};

export function InvestigationUserFilterBar({
  draftFilters,
  statusOptions,
  sortOptions,
  onDraftChange,
  onApply,
  onClear,
}) {
  return (
    <form onSubmit={onApply} className="space-y-3">
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-[minmax(0,1.4fr)_minmax(0,0.9fr)_minmax(0,0.9fr)_auto]">
        <AdminFilterField
          label="Tìm kiếm"
          htmlFor="investigation-user-q"
          className="sm:col-span-2 xl:col-span-1"
        >
          <AdminFilterInput
            id="investigation-user-q"
            type="search"
            value={draftFilters.q}
            onChange={(e) => onDraftChange({ q: e.target.value })}
            placeholder="Email hoặc tên hiển thị…"
          />
        </AdminFilterField>
        <AdminFilterField label="Trạng thái" htmlFor="investigation-user-status">
          <AdminFilterSelect
            id="investigation-user-status"
            value={draftFilters.status}
            onChange={(e) => onDraftChange({ status: e.target.value })}
          >
            {statusOptions.map((option) => (
              <option key={option.value || "all"} value={option.value}>
                {option.value ? getUserStatusLabel(option.value) : "Tất cả trạng thái"}
              </option>
            ))}
          </AdminFilterSelect>
        </AdminFilterField>
        <AdminFilterField label="Sắp xếp" htmlFor="investigation-user-sort">
          <AdminFilterSelect
            id="investigation-user-sort"
            value={draftFilters.sort}
            onChange={(e) => onDraftChange({ sort: e.target.value })}
          >
            {sortOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {SORT_DISPLAY_LABELS[option.value] ?? option.label}
              </option>
            ))}
          </AdminFilterSelect>
        </AdminFilterField>
        <div className="flex flex-col gap-2 sm:col-span-2 sm:flex-row sm:items-end xl:col-span-1">
          <AdminFilterButton type="submit" variant="primary" className="min-h-11 w-full xl:w-auto">
            Áp dụng
          </AdminFilterButton>
          <AdminFilterButton
            type="button"
            variant="secondary"
            className="min-h-11 w-full xl:w-auto"
            onClick={onClear}
          >
            Xóa lọc
          </AdminFilterButton>
        </div>
      </div>
    </form>
  );
}

export function getSortColumnLabel(sortField) {
  return SORT_DISPLAY_LABELS[sortField] || SORT_DISPLAY_LABELS.created_at;
}

export { INVESTIGATION_USER_LIST_SORT_OPTIONS };
