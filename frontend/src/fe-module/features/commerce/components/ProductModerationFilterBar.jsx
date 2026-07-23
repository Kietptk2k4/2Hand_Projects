import {
  AdminFilterBar,
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
} from "../../auth/admin/components/ui";
import {
  PRODUCT_MODERATION_LIST_SORT_OPTIONS,
  PRODUCT_MODERATION_STATUS_OPTIONS,
} from "../constants/productModerationListConstants.js";
import { ProductModerationActiveFilterChips } from "./ProductModerationActiveFilterChips.jsx";
import { ProductModerationQuickFilterChips } from "./ProductModerationQuickFilterChips.jsx";

export function ProductModerationFilterBar({
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
      <ProductModerationQuickFilterChips filters={appliedFilters} onQuickFilter={onQuickFilter} />
      <ProductModerationActiveFilterChips
        filters={appliedFilters}
        onRemoveChip={onRemoveFilterChip}
      />

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
        <AdminFilterField label="Tìm kiếm" htmlFor="product-mod-q" className="lg:col-span-2">
          <AdminFilterInput
            id="product-mod-q"
            type="search"
            value={draftFilters.q}
            onChange={(event) => onDraftFiltersChange({ ...draftFilters, q: event.target.value })}
            placeholder="Tên sản phẩm, Product ID, Shop ID…"
          />
        </AdminFilterField>

        <AdminFilterField label="Trạng thái" htmlFor="product-mod-status">
          <AdminFilterSelect
            id="product-mod-status"
            value={draftFilters.status}
            onChange={(event) =>
              onDraftFiltersChange({ ...draftFilters, status: event.target.value })
            }
          >
            {PRODUCT_MODERATION_STATUS_OPTIONS.map((option) => (
              <option key={option.value || "all"} value={option.value}>
                {option.label}
              </option>
            ))}
          </AdminFilterSelect>
        </AdminFilterField>

        <AdminFilterField label="Sắp xếp theo" htmlFor="product-mod-sort">
          <AdminFilterSelect
            id="product-mod-sort"
            value={draftFilters.sort}
            onChange={(event) => onDraftFiltersChange({ ...draftFilters, sort: event.target.value })}
          >
            {PRODUCT_MODERATION_LIST_SORT_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </AdminFilterSelect>
        </AdminFilterField>
      </AdminFilterBar>
    </div>
  );
}
