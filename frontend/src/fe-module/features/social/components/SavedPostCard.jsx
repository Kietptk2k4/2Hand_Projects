import { authorAvatarUrl, authorDisplayName } from "../utils/authorDisplay";
import { formatSavedAt } from "../utils/formatSavedAt";

const PLACEHOLDER_IMAGE =
  "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='400' height='300' viewBox='0 0 400 300'%3E%3Crect fill='%23dee8ff' width='400' height='300'/%3E%3C/svg%3E";

export function SavedPostCard({
  post,
  onOpenPost,
  onOpenComments,
  onViewProfile,
  onUnsave,
  isUnsaveLoading = false,
}) {
  const mediaUrl = post.media?.[0]?.url || PLACEHOLDER_IMAGE;
  const savedLabel = formatSavedAt(post.savedAt);
  const displayName = authorDisplayName(post.authorId);
  const avatarUrl = authorAvatarUrl(post.authorId);
  const titleText = post.caption?.trim() || "Bài viết không có nội dung";

  const handleOpenPost = () => onOpenPost?.(post.postId);
  const handleOpenComments = (event) => {
    event.stopPropagation();
    onOpenComments?.(post.postId);
  };
  const handleLikeClick = (event) => {
    event.stopPropagation();
    onOpenPost?.(post.postId);
  };
  const handleViewProfile = (event) => {
    event.stopPropagation();
    onViewProfile?.(post.authorId);
  };
  const handleUnsave = (event) => {
    event.stopPropagation();
    onUnsave?.(post.postId);
  };

  return (
    <article className="flex flex-col overflow-hidden rounded-lg border border-outline-variant bg-surface-container-lowest shadow-sm transition-all hover:shadow-md md:flex-row">
      <button
        type="button"
        onClick={handleOpenPost}
        className="relative h-48 flex-shrink-0 bg-surface-variant md:h-auto md:w-1/3"
        aria-label="Xem chi tiết bài viết"
      >
        <img src={mediaUrl} alt="" className="h-full w-full object-cover" />
        <div className="absolute right-3 top-3 flex items-center gap-1 rounded-full border border-outline-variant bg-surface-container-lowest/90 px-2 py-1 backdrop-blur-sm">
          <span
            className="material-symbols-outlined text-[16px] text-primary"
            style={{ fontVariationSettings: "'FILL' 1" }}
            aria-hidden="true"
          >
            bookmark
          </span>
        </div>
      </button>

      <div className="flex flex-grow flex-col p-4">
        <div className="mb-2 flex items-start justify-between">
          <button
            type="button"
            onClick={handleViewProfile}
            className="flex items-center gap-2 text-left"
          >
            <img src={avatarUrl} alt="" className="h-8 w-8 rounded-full object-cover" />
            <h3 className="text-sm font-medium text-on-surface">{displayName}</h3>
          </button>
          <button
            type="button"
            title="Bỏ lưu"
            disabled={isUnsaveLoading}
            onClick={handleUnsave}
            className="p-1 text-on-surface-variant transition-colors hover:text-error disabled:opacity-50"
            aria-label="Bỏ lưu bài viết"
          >
            <span className="material-symbols-outlined" aria-hidden="true">
              bookmark_remove
            </span>
          </button>
        </div>

        <button type="button" onClick={handleOpenPost} className="text-left">
          <h2 className="mb-1 line-clamp-2 text-lg font-semibold text-on-surface">{titleText}</h2>
        </button>

        <div className="mt-auto flex items-center justify-between border-t border-outline-variant pt-3">
          <div className="flex items-center gap-4 text-sm text-on-surface-variant">
            <button
              type="button"
              onClick={handleLikeClick}
              className="flex items-center gap-1 transition-colors hover:text-primary"
              aria-label="Xem lượt thích"
            >
              <span className="material-symbols-outlined text-[16px]" aria-hidden="true">
                thumb_up
              </span>
              <span>{post.likeCount ?? 0}</span>
            </button>
            <button
              type="button"
              onClick={handleOpenComments}
              className="flex items-center gap-1 transition-colors hover:text-primary"
              aria-label="Xem bình luận"
            >
              <span className="material-symbols-outlined text-[16px]" aria-hidden="true">
                comment
              </span>
              <span>{post.replyCount ?? 0}</span>
            </button>
          </div>
          {savedLabel ? (
            <span className="text-xs font-semibold text-tertiary-container">
              Đã lưu vào {savedLabel}
            </span>
          ) : null}
        </div>
      </div>
    </article>
  );
}
