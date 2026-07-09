function buildVisiblePages(currentPage, totalPages, maxVisible = 5) {
  const pages = [];
  let start = Math.max(1, currentPage - Math.floor(maxVisible / 2));
  const end = Math.min(totalPages, start + maxVisible - 1);
  start = Math.max(1, end - maxVisible + 1);

  for (let page = start; page <= end; page += 1) {
    pages.push(page);
  }

  return pages;
}

const navButtonClass =
  "inline-flex min-h-11 items-center justify-center rounded-lg border border-admin-border px-3 py-2 text-sm font-medium text-admin-text-secondary transition-colors hover:bg-admin-surface-muted focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft disabled:cursor-not-allowed disabled:opacity-40";

export function AdminPagination({
  currentPage,
  totalPages,
  summary,
  onPrevious,
  onNext,
  previousLabel = "Trước",
  nextLabel = "Sau",
  disabled = false,
  onGoToPage,
  showPageNumbers = false,
  maxVisiblePages = 5,
  className = "",
}) {
  const canGoPrevious = currentPage > 1 && !disabled;
  const canGoNext = currentPage < totalPages && !disabled;
  const visiblePages =
    showPageNumbers && totalPages > 0
      ? buildVisiblePages(currentPage, totalPages, maxVisiblePages)
      : [];

  return (
    <div
      className={[
        "flex flex-col gap-3 sm:flex-row sm:flex-wrap sm:items-center sm:justify-between",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
    >
      {summary ? (
        <p className="min-w-0 break-words text-sm text-admin-text-secondary">{summary}</p>
      ) : (
        <span />
      )}
      <div className="flex w-full flex-wrap items-center justify-end gap-2 sm:w-auto">
        {showPageNumbers ? (
          <button
            type="button"
            disabled={!canGoPrevious}
            onClick={onPrevious}
            className={[navButtonClass, "p-2 sm:px-2"].join(" ")}
            aria-label="Trang trước"
          >
            <span className="material-symbols-outlined text-[20px]" aria-hidden="true">
              chevron_left
            </span>
          </button>
        ) : null}

        {showPageNumbers
          ? visiblePages.map((page) => (
              <button
                key={page}
                type="button"
                disabled={disabled}
                onClick={() => onGoToPage?.(page)}
                className={[
                  "inline-flex min-h-11 min-w-[2.5rem] items-center justify-center rounded-lg px-2 py-2 text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft disabled:cursor-not-allowed disabled:opacity-40",
                  page === currentPage
                    ? "bg-admin-accent text-white"
                    : "border border-admin-border text-admin-text-secondary hover:bg-admin-surface-muted",
                ].join(" ")}
              >
                {page}
              </button>
            ))
          : null}

        {showPageNumbers ? (
          <button
            type="button"
            disabled={!canGoNext}
            onClick={onNext}
            className={[navButtonClass, "p-2 sm:px-2"].join(" ")}
            aria-label="Trang sau"
          >
            <span className="material-symbols-outlined text-[20px]" aria-hidden="true">
              chevron_right
            </span>
          </button>
        ) : null}

        {!showPageNumbers ? (
          <>
            <button
              type="button"
              disabled={!canGoPrevious}
              onClick={onPrevious}
              className={[navButtonClass, "flex-1 sm:flex-none sm:px-4"].join(" ")}
            >
              {previousLabel}
            </button>
            <button
              type="button"
              disabled={!canGoNext}
              onClick={onNext}
              className={[navButtonClass, "flex-1 sm:flex-none sm:px-4"].join(" ")}
            >
              {nextLabel}
            </button>
          </>
        ) : null}
      </div>
    </div>
  );
}
