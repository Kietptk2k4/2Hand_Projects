import {
  AdminFilterBar,
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
} from "../../auth/admin/components/ui";
import {
  SHOP_MODERATION_LIST_SORT_OPTIONS,
  SHOP_MODERATION_STATUS_OPTIONS,
} from "../constants/shopModerationListConstants.js";
import { ShopModerationActiveFilterChips } from "./ShopModerationActiveFilterChips.jsx";
import { ShopModerationQuickFilterChips } from "./ShopModerationQuickFilterChips.jsx";

export function ShopModerationFilterBar({
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
      <ShopModerationQuickFilterChips filters={appliedFilters} onQuickFilter={onQuickFilter} />
      <ShopModerationActiveFilterChips filters={appliedFilters} onRemoveChip={onRemoveFilterChip} />

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
        <AdminFilterField label="Tìm kiếm" htmlFor="shop-mod-q" className="lg:col-span-2">
          <AdminFilterInput
            id="shop-mod-q"
            type="search"
            value={draftFilters.q}
            onChange={(event) => onDraftFiltersChange({ ...draftFilters, q: event.target.value })}
            placeholder="Tên shop, Shop ID hoặc Seller ID…"
          />
        </AdminFilterField>

        <AdminFilterField label="Trạng thái" htmlFor="shop-mod-status">
          <AdminFilterSelect
            id="shop-mod-status"
            value={draftFilters.status}
            onChange={(event) => onDraftFiltersChange({ ...draftFilters, status: event.target.value })}
          >
            {SHOP_MODERATION_STATUS_OPTIONS.map((option) => (
              <option key={option.value || "all"} value={option.value}>
                {option.label}
              </option>
            ))}
          </AdminFilterSelect>
        </AdminFilterField>

        <AdminFilterField label="Sắp xếp theo" htmlFor="shop-mod-sort">
          <AdminFilterSelect
            id="shop-mod-sort"
            value={draftFilters.sort}
            onChange={(event) => onDraftFiltersChange({ ...draftFilters, sort: event.target.value })}
          >
            {SHOP_MODERATION_LIST_SORT_OPTIONS.map((option) => (
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
