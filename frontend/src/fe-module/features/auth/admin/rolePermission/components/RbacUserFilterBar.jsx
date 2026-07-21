import {
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
} from "../../components/ui";

const SORT_DISPLAY_LABELS = {
  email: "Email (A–Z)",
  display_name: "Tên hiển thị (A–Z)",
  created_at: "Ngày tạo (mới nhất)",
  status: "Trạng thái",
};

const STATUS_DISPLAY_LABELS = {
  "": "Tất cả trạng thái",
  ACTIVE: "Đang hoạt động",
  PENDING_VERIFICATION: "Chờ xác minh",
  SUSPENDED: "Tạm khóa",
};

export function getRbacSortColumnLabel(sortField) {
  return SORT_DISPLAY_LABELS[sortField] || SORT_DISPLAY_LABELS.email;
}

export function RbacUserFilterBar({
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
        <AdminFilterField label="Tìm kiếm" htmlFor="rbac-user-q" className="sm:col-span-2 xl:col-span-1">
          <AdminFilterInput
            id="rbac-user-q"
            type="search"
            value={draftFilters.q}
            onChange={(e) => onDraftChange({ q: e.target.value })}
            placeholder="Email hoặc tên hiển thị..."
          />
        </AdminFilterField>
        <AdminFilterField label="Trạng thái" htmlFor="rbac-user-status">
          <AdminFilterSelect
            id="rbac-user-status"
            value={draftFilters.status}
            onChange={(e) => onDraftChange({ status: e.target.value })}
          >
            {statusOptions.map((option) => (
              <option key={option.value || "all"} value={option.value}>
                {STATUS_DISPLAY_LABELS[option.value] ?? option.label}
              </option>
            ))}
          </AdminFilterSelect>
        </AdminFilterField>
        <AdminFilterField label="Sắp xếp" htmlFor="rbac-user-sort">
          <AdminFilterSelect
            id="rbac-user-sort"
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
