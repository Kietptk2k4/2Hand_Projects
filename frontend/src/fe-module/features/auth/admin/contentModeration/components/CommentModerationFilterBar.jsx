import {
  AdminFilterBar,
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
} from "../../components/ui";
import {
  COMMENT_MODERATION_LIST_SORT_OPTIONS,
  COMMENT_MODERATION_LIST_STATUS_OPTIONS,
} from "../constants/commentModerationListConstants.js";

export function CommentModerationFilterBar({ draftFilters, onDraftFiltersChange, onApply, onClear }) {
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
      <AdminFilterField label="Tìm kiếm" htmlFor="comment-mod-q" className="lg:col-span-2">
        <AdminFilterInput
          id="comment-mod-q"
          type="search"
          value={draftFilters.q}
          onChange={(event) => onDraftFiltersChange({ ...draftFilters, q: event.target.value })}
          placeholder="Comment ID, Post ID hoặc nội dung…"
        />
      </AdminFilterField>

      <AdminFilterField label="Post ID" htmlFor="comment-mod-post">
        <AdminFilterInput
          id="comment-mod-post"
          type="text"
          value={draftFilters.post_id}
          onChange={(event) => onDraftFiltersChange({ ...draftFilters, post_id: event.target.value })}
          placeholder="Lọc theo bài viết…"
        />
      </AdminFilterField>

      <AdminFilterField label="Trạng thái" htmlFor="comment-mod-status">
        <AdminFilterSelect
          id="comment-mod-status"
          value={draftFilters.status}
          onChange={(event) => onDraftFiltersChange({ ...draftFilters, status: event.target.value })}
        >
          {COMMENT_MODERATION_LIST_STATUS_OPTIONS.map((option) => (
            <option key={option.value || "all"} value={option.value}>
              {option.label}
            </option>
          ))}
        </AdminFilterSelect>
      </AdminFilterField>

      <AdminFilterField label="Sắp xếp theo" htmlFor="comment-mod-sort">
        <AdminFilterSelect
          id="comment-mod-sort"
          value={draftFilters.sort}
          onChange={(event) => onDraftFiltersChange({ ...draftFilters, sort: event.target.value })}
        >
          {COMMENT_MODERATION_LIST_SORT_OPTIONS.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </AdminFilterSelect>
      </AdminFilterField>
    </AdminFilterBar>
  );
}
