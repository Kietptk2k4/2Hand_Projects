import {
  AdminFilterBar,
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
} from "../../components/ui";
import { CONFIG_ACTIVE_FILTER_OPTIONS, CONFIG_VALUE_TYPES } from "../constants/systemConfigConstants.js";
import { SYSTEM_CONFIG_VALUE_TYPE_LABELS } from "../constants/systemConfigListConstants.js";
import { SystemConfigActiveFilterChips } from "./SystemConfigActiveFilterChips.jsx";
import { SystemConfigQuickFilterChips } from "./SystemConfigQuickFilterChips.jsx";

export function SystemConfigFilterBar({
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
      <SystemConfigQuickFilterChips filters={appliedFilters} onQuickFilter={onQuickFilter} />
      <SystemConfigActiveFilterChips filters={appliedFilters} onRemoveChip={onRemoveFilterChip} />

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
        <AdminFilterField label="Tìm kiếm" htmlFor="sc-q" className="lg:col-span-2">
          <AdminFilterInput
            id="sc-q"
            type="search"
            value={draftFilters.q}
            onChange={(event) => onDraftFiltersChange({ ...draftFilters, q: event.target.value })}
            placeholder="config key / mô tả"
          />
        </AdminFilterField>

        <AdminFilterField label="Kiểu giá trị" htmlFor="sc-value-type">
          <AdminFilterSelect
            id="sc-value-type"
            value={draftFilters.value_type}
            onChange={(event) =>
              onDraftFiltersChange({ ...draftFilters, value_type: event.target.value })
            }
          >
            <option value="">Tất cả</option>
            {CONFIG_VALUE_TYPES.map((type) => (
              <option key={type} value={type}>
                {SYSTEM_CONFIG_VALUE_TYPE_LABELS[type] || type}
              </option>
            ))}
          </AdminFilterSelect>
        </AdminFilterField>

        <AdminFilterField label="Trạng thái" htmlFor="sc-is-active">
          <AdminFilterSelect
            id="sc-is-active"
            value={draftFilters.is_active}
            onChange={(event) =>
              onDraftFiltersChange({ ...draftFilters, is_active: event.target.value })
            }
          >
            {CONFIG_ACTIVE_FILTER_OPTIONS.map((option) => (
              <option key={option.value || "all"} value={option.value}>
                {option.label}
              </option>
            ))}
          </AdminFilterSelect>
        </AdminFilterField>
      </AdminFilterBar>
    </div>
  );
}
