import {
  AdminFilterBar,
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
} from "../../components/ui";
import {
  POST_MODERATION_LIST_MODERATION_STATUS_OPTIONS,
  POST_MODERATION_LIST_SORT_OPTIONS,
  POST_MODERATION_LIST_STATUS_OPTIONS,
} from "../constants/postModerationListConstants.js";
import { PostModerationActiveFilterChips } from "./PostModerationActiveFilterChips.jsx";
import { PostModerationQuickFilterChips } from "./PostModerationQuickFilterChips.jsx";

export function PostModerationFilterBar({
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
      <PostModerationQuickFilterChips filters={appliedFilters} onQuickFilter={onQuickFilter} />
      <PostModerationActiveFilterChips filters={appliedFilters} onRemoveChip={onRemoveFilterChip} />

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
        <AdminFilterField label="Tìm kiếm" htmlFor="post-mod-q" className="lg:col-span-2">
          <AdminFilterInput
            id="post-mod-q"
            type="search"
            value={draftFilters.q}
            onChange={(event) => onDraftFiltersChange({ ...draftFilters, q: event.target.value })}
            placeholder="Post ID hoặc nội dung…"
          />
        </AdminFilterField>

        <AdminFilterField label="Trạng thái" htmlFor="post-mod-status">
          <AdminFilterSelect
            id="post-mod-status"
            value={draftFilters.status}
            onChange={(event) => onDraftFiltersChange({ ...draftFilters, status: event.target.value })}
          >
            {POST_MODERATION_LIST_STATUS_OPTIONS.map((option) => (
              <option key={option.value || "all"} value={option.value}>
                {option.label}
              </option>
            ))}
          </AdminFilterSelect>
        </AdminFilterField>

        <AdminFilterField label="Kiểm duyệt" htmlFor="post-mod-moderation">
          <AdminFilterSelect
            id="post-mod-moderation"
            value={draftFilters.moderation_status}
            onChange={(event) =>
              onDraftFiltersChange({ ...draftFilters, moderation_status: event.target.value })
            }
          >
            {POST_MODERATION_LIST_MODERATION_STATUS_OPTIONS.map((option) => (
              <option key={option.value || "all-mod"} value={option.value}>
                {option.label}
              </option>
            ))}
          </AdminFilterSelect>
        </AdminFilterField>

        <AdminFilterField label="Sắp xếp theo" htmlFor="post-mod-sort">
          <AdminFilterSelect
            id="post-mod-sort"
            value={draftFilters.sort}
            onChange={(event) => onDraftFiltersChange({ ...draftFilters, sort: event.target.value })}
          >
            {POST_MODERATION_LIST_SORT_OPTIONS.map((option) => (
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
