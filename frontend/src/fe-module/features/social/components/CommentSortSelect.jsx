import { COMMENT_SORT_OPTIONS } from "../constants/commentConstants";

export function CommentSortSelect({ value, onChange, disabled = false }) {
  return (
    <label className="inline-flex items-center gap-2 text-sm text-on-surface-variant">
      <span className="sr-only">Sắp xếp bình luận</span>
      <select
        value={value}
        disabled={disabled}
        onChange={(event) => onChange?.(event.target.value)}
        className="rounded-lg border border-outline-variant bg-surface-container-lowest px-2.5 py-1.5 text-sm text-on-surface outline-none transition focus:border-primary focus:ring-1 focus:ring-primary disabled:cursor-not-allowed disabled:opacity-50"
        aria-label="Sắp xếp bình luận"
      >
        {COMMENT_SORT_OPTIONS.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </label>
  );
}
