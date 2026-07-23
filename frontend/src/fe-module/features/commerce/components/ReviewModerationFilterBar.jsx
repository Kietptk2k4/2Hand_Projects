import {
  AdminFilterBar,
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
} from "../../auth/admin/components/ui";
import {
  REVIEW_MODERATION_LIST_SORT_OPTIONS,
  REVIEW_MODERATION_RATING_OPTIONS,
  REVIEW_MODERATION_STATUS_OPTIONS,
} from "../constants/reviewModerationListConstants.js";
import { ReviewModerationActiveFilterChips } from "./ReviewModerationActiveFilterChips.jsx";
import { ReviewModerationQuickFilterChips } from "./ReviewModerationQuickFilterChips.jsx";

export function ReviewModerationFilterBar({
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
      <ReviewModerationQuickFilterChips filters={appliedFilters} onQuickFilter={onQuickFilter} />
      <ReviewModerationActiveFilterChips
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
        <AdminFilterField label="Tìm kiếm" htmlFor="review-mod-q" className="lg:col-span-2">
          <AdminFilterInput
            id="review-mod-q"
            type="search"
            value={draftFilters.q}
            onChange={(event) => onDraftFiltersChange({ ...draftFilters, q: event.target.value })}
            placeholder="Review ID, Order ID, tên sản phẩm…"
          />
        </AdminFilterField>

        <AdminFilterField label="Trạng thái" htmlFor="review-mod-status">
          <AdminFilterSelect
            id="review-mod-status"
            value={draftFilters.status}
            onChange={(event) =>
              onDraftFiltersChange({ ...draftFilters, status: event.target.value })
            }
          >
            {REVIEW_MODERATION_STATUS_OPTIONS.map((option) => (
              <option key={option.value || "all"} value={option.value}>
                {option.label}
              </option>
            ))}
          </AdminFilterSelect>
        </AdminFilterField>

        <AdminFilterField label="Số sao" htmlFor="review-mod-rating">
          <AdminFilterSelect
            id="review-mod-rating"
            value={draftFilters.rating}
            onChange={(event) =>
              onDraftFiltersChange({ ...draftFilters, rating: event.target.value })
            }
          >
            {REVIEW_MODERATION_RATING_OPTIONS.map((option) => (
              <option key={option.value || "all"} value={option.value}>
                {option.label}
              </option>
            ))}
          </AdminFilterSelect>
        </AdminFilterField>

        <AdminFilterField label="Sắp xếp theo" htmlFor="review-mod-sort">
          <AdminFilterSelect
            id="review-mod-sort"
            value={draftFilters.sort}
            onChange={(event) => onDraftFiltersChange({ ...draftFilters, sort: event.target.value })}
          >
            {REVIEW_MODERATION_LIST_SORT_OPTIONS.map((option) => (
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
