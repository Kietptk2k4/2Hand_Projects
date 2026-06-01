export function AdminReviewModerationPagination({
  page,
  totalPages,
  rangeStart,
  rangeEnd,
  totalItems,
  onPrev,
  onNext,
  onGoToPage,
  disabled,
}) {
  if (totalItems === 0) return null;

  const pages = [];
  const maxVisible = 5;
  let start = Math.max(1, page - Math.floor(maxVisible / 2));
  const end = Math.min(totalPages, start + maxVisible - 1);
  start = Math.max(1, end - maxVisible + 1);

  for (let p = start; p <= end; p += 1) {
    pages.push(p);
  }

  return (
    <footer className="flex flex-wrap items-center justify-between gap-4 border-t border-outline-variant bg-surface px-4 py-4">
      <p className="text-body-sm text-on-surface-variant">
        Hiển thị {rangeStart}–{rangeEnd} của {totalItems} đánh giá
      </p>

      <div className="flex flex-wrap items-center gap-2">
        <button
          type="button"
          disabled={disabled || page <= 1}
          onClick={onPrev}
          className="rounded-lg border border-outline-variant p-1.5 text-on-surface hover:bg-surface-container-high disabled:opacity-40"
          aria-label="Trang trước"
        >
          <span className="material-symbols-outlined text-[20px]">chevron_left</span>
        </button>

        {pages.map((p) => (
          <button
            key={p}
            type="button"
            disabled={disabled}
            onClick={() => onGoToPage(p)}
            className={[
              "min-w-[2rem] rounded-lg px-2 py-1.5 text-label-md",
              p === page
                ? "bg-primary font-semibold text-on-primary"
                : "border border-outline-variant text-on-surface hover:bg-surface-container-high",
            ].join(" ")}
          >
            {p}
          </button>
        ))}

        <button
          type="button"
          disabled={disabled || page >= totalPages}
          onClick={onNext}
          className="rounded-lg border border-outline-variant p-1.5 text-on-surface hover:bg-surface-container-high disabled:opacity-40"
          aria-label="Trang sau"
        >
          <span className="material-symbols-outlined text-[20px]">chevron_right</span>
        </button>
      </div>
    </footer>
  );
}
