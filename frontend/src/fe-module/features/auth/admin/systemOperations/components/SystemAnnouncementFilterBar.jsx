import {
  AdminFilterBar,
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
} from "../../components/ui";
import { ANNOUNCEMENT_SEVERITIES } from "../constants/systemAnnouncementConstants.js";
import {
  ANNOUNCEMENT_SEVERITY_LABELS,
  ANNOUNCEMENT_STATUS_LABELS,
} from "../constants/announcementListConstants.js";
import { SystemAnnouncementActiveFilterChips } from "./SystemAnnouncementActiveFilterChips.jsx";
import { SystemAnnouncementQuickFilterChips } from "./SystemAnnouncementQuickFilterChips.jsx";

export function SystemAnnouncementFilterBar({
  appliedFilters,
  draftFilters,
  onDraftFiltersChange,
  onApply,
  onClear,
  onQuickFilter,
  onRemoveFilterChip,
}) {
  return (
    <div className="space-y-4">
      <SystemAnnouncementQuickFilterChips filters={appliedFilters} onQuickFilter={onQuickFilter} />
      <SystemAnnouncementActiveFilterChips filters={appliedFilters} onRemoveChip={onRemoveFilterChip} />

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
        <AdminFilterField label="Tìm kiếm" htmlFor="sa-q" className="lg:col-span-2">
          <AdminFilterInput
            id="sa-q"
            type="search"
            value={draftFilters.q}
            onChange={(event) => onDraftFiltersChange({ ...draftFilters, q: event.target.value })}
            placeholder="tiêu đề / nội dung"
          />
        </AdminFilterField>

        <AdminFilterField label="Trạng thái" htmlFor="sa-status">
          <AdminFilterSelect
            id="sa-status"
            value={draftFilters.status}
            onChange={(event) => onDraftFiltersChange({ ...draftFilters, status: event.target.value })}
          >
            <option value="">Tất cả</option>
            {Object.entries(ANNOUNCEMENT_STATUS_LABELS).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </AdminFilterSelect>
        </AdminFilterField>

        <AdminFilterField label="Mức độ" htmlFor="sa-severity">
          <AdminFilterSelect
            id="sa-severity"
            value={draftFilters.severity}
            onChange={(event) => onDraftFiltersChange({ ...draftFilters, severity: event.target.value })}
          >
            <option value="">Tất cả</option>
            {ANNOUNCEMENT_SEVERITIES.map((severity) => (
              <option key={severity} value={severity}>
                {ANNOUNCEMENT_SEVERITY_LABELS[severity] || severity}
              </option>
            ))}
          </AdminFilterSelect>
        </AdminFilterField>
      </AdminFilterBar>
    </div>
  );
}
