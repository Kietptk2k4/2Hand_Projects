import { ProfilePostTile } from "./ProfilePostTile";

export function ProfilePortfolioSection({
  postsState,
  onOpenPost,
  isPrivateLocked,
  isOwner = false,
  onEdit,
  onDeletePost,
  onToggleSavePost,
  isSavingPost,
  isDeletingPost,
}) {
  const {
    items,
    isInitialLoading,
    isLoadingMore,
    hasNext,
    errorMessage,
    errorCode,
    loadMore,
    retry,
  } = postsState;

  if (isPrivateLocked) {
    return (
      <div className="rounded-xl border border-outline-variant bg-surface-container-low p-8 text-center">
        <span className="material-symbols-outlined mb-2 text-4xl text-outline" aria-hidden="true">
          lock
        </span>
        <p className="text-sm font-medium text-on-surface">
          Tài khoản riêng tư — theo dõi để xem bài viết
        </p>
        <p className="mt-1 text-sm text-on-surface-variant">
          Gửi yêu cầu theo dõi để xem bài viết của người dùng này.
        </p>
      </div>
    );
  }

  if (isInitialLoading) {
    return (
      <div className="grid grid-cols-2 gap-3 md:grid-cols-4 md:gap-6">
        {Array.from({ length: 8 }).map((_, index) => (
          <div
            key={index}
            className="aspect-square animate-pulse rounded-lg bg-surface-container-high"
          />
        ))}
      </div>
    );
  }

  if (errorMessage) {
    return (
      <div className="rounded-xl border border-error/30 bg-error-container/40 p-6 text-center">
        <p className="text-sm text-on-error-container">{errorMessage}</p>
        {errorCode !== 403 ? (
          <button
            type="button"
            onClick={retry}
            className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-medium text-on-primary"
          >
            Thử lại
          </button>
        ) : null}
      </div>
    );
  }

  if (items.length === 0) {
    return (
      <div className="rounded-xl border border-outline-variant bg-surface-container-low p-8 text-center">
        <span className="material-symbols-outlined mb-2 text-4xl text-outline" aria-hidden="true">
          photo_library
        </span>
        <p className="text-sm text-on-surface-variant">Chưa có bài viết nào.</p>
      </div>
    );
  }

  return (
    <>
      <div className="grid grid-cols-2 gap-3 md:grid-cols-4 md:gap-6">
        {items.map((post) => (
          <ProfilePostTile
            key={post.postId}
            post={post}
            onOpenPost={onOpenPost}
            isOwner={isOwner}
            savedByMe={post.savedByMe ?? false}
            onEdit={onEdit}
            onDeletePost={onDeletePost}
            onToggleSavePost={onToggleSavePost}
            isSavingPost={isSavingPost?.(post.postId)}
            isDeletingPost={isDeletingPost?.(post.postId)}
          />
        ))}
      </div>

      {hasNext ? (
        <div className="mt-6 flex justify-center">
          {isLoadingMore ? (
            <div
              className="h-8 w-8 animate-spin rounded-full border-4 border-[#d8e3fb] border-t-primary"
              aria-label="Đang tải thêm"
            />
          ) : (
            <button
              type="button"
              onClick={loadMore}
              className="rounded-lg border border-primary px-6 py-2 text-sm font-medium text-primary hover:bg-[#e7eeff]"
            >
              Tải thêm
            </button>
          )}
        </div>
      ) : null}
    </>
  );
}
