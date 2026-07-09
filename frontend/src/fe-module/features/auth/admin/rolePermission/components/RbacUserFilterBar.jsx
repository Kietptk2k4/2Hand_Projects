import {
  AdminFilterBar,
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
  ACTIVE: "ACTIVE",
  PENDING_VERIFICATION: "PENDING_VERIFICATION",
  SUSPENDED: "SUSPENDED",
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
    <AdminFilterBar
      onSubmit={onApply}
      actions={
        <>
          <AdminFilterButton type="submit" variant="primary">
            Áp dụng
          </AdminFilterButton>
          <AdminFilterButton type="button" variant="secondary" onClick={onClear}>
            Xóa lọc
          </AdminFilterButton>
        </>
      }
    >
      <AdminFilterField label="Tìm kiếm" htmlFor="rbac-user-q" className="lg:col-span-2">
        <AdminFilterInput
          id="rbac-user-q"
          type="search"
          className="text-base"
          value={draftFilters.q}
          onChange={(e) => onDraftChange({ q: e.target.value })}
          placeholder="Email hoặc tên hiển thị..."
        />
      </AdminFilterField>
      <AdminFilterField label="Trạng thái" htmlFor="rbac-user-status">
        <AdminFilterSelect
          id="rbac-user-status"
          className="text-base"
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
      <AdminFilterField label="Sắp xếp theo" htmlFor="rbac-user-sort">
        <AdminFilterSelect
          id="rbac-user-sort"
          className="text-base"
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
    </AdminFilterBar>
  );
}
