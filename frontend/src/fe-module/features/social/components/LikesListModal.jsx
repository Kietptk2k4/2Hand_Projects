import { useEffect } from "react";
import { createPortal } from "react-dom";
import { LikesListRow } from "./LikesListRow";
import { useLikeUsersList } from "../hooks/useLikeUsersList";

export function LikesListModal({
  isOpen,
  targetType,
  targetId,
  likeCount = 0,
  onClose,
  onViewProfile,
}) {
  const likes = useLikeUsersList(targetType, targetId, { enabled: isOpen });

  useEffect(() => {
    if (!isOpen) return undefined;
    const previous = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = previous;
    };
  }, [isOpen]);

  useEffect(() => {
    if (!isOpen) return undefined;
    const onKeyDown = (event) => {
      if (event.key !== "Escape") return;
      event.stopPropagation();
      onClose?.();
    };
    window.addEventListener("keydown", onKeyDown, true);
    return () => window.removeEventListener("keydown", onKeyDown, true);
  }, [isOpen, onClose]);

  if (!isOpen || !targetType || !targetId) return null;

  const title =
    targetType === "comment"
      ? `Người thích bình luận (${likeCount})`
      : `Người thích bài viết (${likeCount})`;

  return createPortal(
    <div
      className="fixed inset-0 z-[70] flex items-center justify-center bg-on-background/50 p-4 backdrop-blur-sm"
      role="presentation"
      onClick={onClose}
    >
      <div
        className="relative flex max-h-[min(800px,85vh)] w-full max-w-md flex-col overflow-hidden rounded-xl bg-surface-container-lowest shadow-lg"
        role="dialog"
        aria-modal="true"
        aria-labelledby="likes-list-title"
        onClick={(event) => event.stopPropagation()}
      >
        <button
          type="button"
          onClick={onClose}
          className="absolute right-4 top-4 z-10 flex h-8 w-8 items-center justify-center rounded-full text-on-surface-variant transition-colors hover:bg-surface-container-low hover:text-on-surface"
          aria-label="Đóng"
        >
          <span className="material-symbols-outlined text-[20px]" aria-hidden="true">
            close
          </span>
        </button>

        <header className="shrink-0 border-b border-outline-variant px-6 pb-4 pt-6">
          <h2 id="likes-list-title" className="text-center text-lg font-semibold text-on-surface">
            {title}
          </h2>
        </header>

        <div className="shrink-0 border-b border-outline-variant/50 px-6 py-3">
          <div className="relative">
            <span
              className="material-symbols-outlined pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-[20px] text-on-surface-variant"
              aria-hidden="true"
            >
              search
            </span>
            <input
              type="search"
              value={likes.searchQuery}
              onChange={(event) => likes.setSearchQuery(event.target.value)}
              placeholder="Tìm kiếm..."
              className="w-full rounded-lg border border-outline-variant bg-surface-container-lowest py-2 pl-10 pr-3 text-sm outline-none transition placeholder:text-on-surface-variant/70 focus:border-primary focus:ring-1 focus:ring-primary"
            />
          </div>
        </div>

        <div className="flex-1 overflow-y-auto px-4 py-2">
          {likes.isInitialLoading ? (
            <div className="flex justify-center py-12">
              <div
                className="h-8 w-8 animate-spin rounded-full border-4 border-[#d8e3fb] border-t-primary"
                aria-label="Đang tải"
              />
            </div>
          ) : null}

          {!likes.isInitialLoading && likes.errorMessage ? (
            <div className="rounded-lg border border-error/30 bg-error-container/30 p-4 text-center">
              <p className="text-sm text-on-error-container">{likes.errorMessage}</p>
              {likes.errorCode !== 403 ? (
                <button
                  type="button"
                  onClick={likes.retry}
                  className="mt-3 text-sm font-medium text-primary hover:underline"
                >
                  Thử lại
                </button>
              ) : null}
            </div>
          ) : null}

          {!likes.isInitialLoading && !likes.errorMessage && likes.items.length === 0 ? (
            <p className="py-12 text-center text-sm text-on-surface-variant">
              {likes.searchQuery.trim()
                ? "Không tìm thấy kết quả phù hợp."
                : "Chưa có ai thích."}
            </p>
          ) : null}

          {!likes.isInitialLoading && !likes.errorMessage && likes.items.length > 0 ? (
            <div className="flex flex-col">
              {likes.items.map((item) => (
                <LikesListRow
                  key={item.userId}
                  item={item}
                  onViewProfile={(userId) => {
                    onViewProfile?.(userId);
                    onClose?.();
                  }}
                />
              ))}
            </div>
          ) : null}

          {!likes.isInitialLoading && !likes.errorMessage && likes.hasNext ? (
            <div className="flex justify-center py-4">
              {likes.isLoadingMore ? (
                <div
                  className="h-6 w-6 animate-spin rounded-full border-4 border-[#d8e3fb] border-t-primary"
                  aria-label="Đang tải thêm"
                />
              ) : (
                <button
                  type="button"
                  onClick={likes.loadMore}
                  className="text-sm font-medium text-primary hover:underline"
                >
                  Tải thêm
                </button>
              )}
            </div>
          ) : null}
        </div>
      </div>
    </div>,
    document.body
  );
}