import {
  AdminFilterBar,
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
} from "../../components/ui";
import { INVESTIGATION_USER_LIST_SORT_OPTIONS } from "../constants/investigationUserListConstants.js";

const SORT_DISPLAY_LABELS = {
  created_at: "Ngày tạo (mới nhất)",
  email: "Email (A–Z)",
  display_name: "Tên hiển thị (A–Z)",
  status: "Trạng thái",
};

const STATUS_DISPLAY_LABELS = {
  "": "Tất cả trạng thái",
  ACTIVE: "ACTIVE",
  PENDING_VERIFICATION: "PENDING_VERIFICATION",
  SUSPENDED: "SUSPENDED",
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
      <AdminFilterField label="Tìm kiếm" htmlFor="investigation-user-q" className="lg:col-span-2">
        <AdminFilterInput
          id="investigation-user-q"
          type="search"
          value={draftFilters.q}
          onChange={(e) => onDraftChange({ q: e.target.value })}
          placeholder="Email…"
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
              {STATUS_DISPLAY_LABELS[option.value] ?? option.label}
            </option>
          ))}
        </AdminFilterSelect>
      </AdminFilterField>
      <AdminFilterField label="Sắp xếp theo" htmlFor="investigation-user-sort">
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
    </AdminFilterBar>
  );
}

export function getSortColumnLabel(sortField) {
  return SORT_DISPLAY_LABELS[sortField] || SORT_DISPLAY_LABELS.created_at;
}

export { INVESTIGATION_USER_LIST_SORT_OPTIONS };
