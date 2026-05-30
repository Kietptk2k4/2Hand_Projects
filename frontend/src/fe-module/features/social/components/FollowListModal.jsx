import { useEffect } from "react";
import { FollowListRow } from "./FollowListRow";
import { useUserRelations } from "../hooks/useUserRelations";

export function FollowListModal({
  isOpen,
  targetUserId,
  profile,
  activeType,
  onClose,
  onTypeChange,
  onViewProfile,
}) {
  const relations = useUserRelations(targetUserId, activeType, { enabled: isOpen });

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
      if (event.key === "Escape") onClose?.();
    };
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen || !profile) return null;

  const followerCount = profile.followerCount ?? 0;
  const followingCount = profile.followingCount ?? 0;
  const searchPlaceholder =
    activeType === "following"
      ? "Tìm kiếm đang theo dõi..."
      : "Tìm kiếm người theo dõi...";

  const handleTabChange = (type) => {
    relations.setSearchQuery("");
    onTypeChange?.(type);
  };

  return (
    <div
      className="fixed inset-0 z-[55] flex items-center justify-center bg-on-background/40 p-4 backdrop-blur-sm"
      role="presentation"
      onClick={onClose}
    >
      <div
        className="relative flex max-h-[min(800px,85vh)] w-full max-w-md flex-col overflow-hidden rounded-xl bg-surface-container-lowest shadow-lg"
        role="dialog"
        aria-modal="true"
        aria-labelledby="follow-list-title"
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

        <header className="shrink-0 border-b border-outline-variant px-6 pb-3 pt-6">
          <h2 id="follow-list-title" className="text-center text-lg font-semibold text-on-surface">
            Connections
          </h2>
          <div className="mt-4 flex border-b border-outline-variant">
            <button
              type="button"
              onClick={() => handleTabChange("followers")}
              className={[
                "flex-1 pb-2 text-center text-sm font-medium transition-colors",
                activeType === "followers"
                  ? "border-b-2 border-primary text-primary"
                  : "text-on-surface-variant hover:text-primary",
              ].join(" ")}
            >
              Người theo dõi ({followerCount})
            </button>
            <button
              type="button"
              onClick={() => handleTabChange("following")}
              className={[
                "flex-1 pb-2 text-center text-sm font-medium transition-colors",
                activeType === "following"
                  ? "border-b-2 border-primary text-primary"
                  : "text-on-surface-variant hover:text-primary",
              ].join(" ")}
            >
              Đang theo dõi ({followingCount})
            </button>
          </div>
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
              value={relations.searchQuery}
              onChange={(event) => relations.setSearchQuery(event.target.value)}
              placeholder={searchPlaceholder}
              className="w-full rounded-lg border border-outline-variant bg-surface-container-lowest py-2 pl-10 pr-3 text-sm outline-none transition placeholder:text-on-surface-variant/70 focus:border-primary focus:ring-1 focus:ring-primary"
            />
          </div>
        </div>

        <div className="flex-1 overflow-y-auto px-4 py-2">
          {relations.isInitialLoading ? (
            <div className="flex justify-center py-12">
              <div
                className="h-8 w-8 animate-spin rounded-full border-4 border-[#d8e3fb] border-t-primary"
                aria-label="Đang tải"
              />
            </div>
          ) : null}

          {!relations.isInitialLoading && relations.errorMessage ? (
            <div className="rounded-lg border border-error/30 bg-error-container/30 p-4 text-center">
              <p className="text-sm text-on-error-container">{relations.errorMessage}</p>
              {relations.errorCode !== 403 ? (
                <button
                  type="button"
                  onClick={relations.retry}
                  className="mt-3 text-sm font-medium text-primary hover:underline"
                >
                  Thử lại
                </button>
              ) : null}
            </div>
          ) : null}

          {!relations.isInitialLoading &&
          !relations.errorMessage &&
          relations.items.length === 0 ? (
            <p className="py-12 text-center text-sm text-on-surface-variant">
              {relations.searchQuery.trim()
                ? "Không tìm thấy kết quả phù hợp."
                : "Chưa có ai trong danh sách này."}
            </p>
          ) : null}

          {!relations.isInitialLoading && !relations.errorMessage && relations.items.length > 0 ? (
            <div className="flex flex-col">
              {relations.items.map((item) => (
                <FollowListRow
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

          {!relations.isInitialLoading && !relations.errorMessage && relations.hasNext ? (
            <div className="flex justify-center py-4">
              {relations.isLoadingMore ? (
                <div
                  className="h-6 w-6 animate-spin rounded-full border-4 border-[#d8e3fb] border-t-primary"
                  aria-label="Đang tải thêm"
                />
              ) : (
                <button
                  type="button"
                  onClick={relations.loadMore}
                  className="text-sm font-medium text-primary hover:underline"
                >
                  Tải thêm
                </button>
              )}
            </div>
          ) : null}
        </div>
      </div>
    </div>
  );
}
