import { authorAvatarUrl, authorDisplayName } from "../utils/authorDisplay";
import { formatRelativeTime } from "../utils/formatRelativeTime";
import { PostCaption } from "./PostCaption";
import { PostOptionsMenu } from "./PostOptionsMenu";

const PLACEHOLDER_IMAGE =
  "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='400' height='300' viewBox='0 0 400 300'%3E%3Crect fill='%23dee8ff' width='400' height='300'/%3E%3C/svg%3E";

function formatCount(value) {
  const num = Number(value) || 0;
  if (num >= 1000) {
    return `${(num / 1000).toFixed(1).replace(/\.0$/, "")}k`;
  }
  return String(num);
}

export function HashtagPostCard({
  post,
  onOpenPost,
  onOpenComments,
  onViewProfile,
  onHashtagClick,
  onEdit,
  onDeletePost,
  onToggleSavePost,
  isSavingPost = false,
  isDeletingPost = false,
  currentUserId,
}) {
  const isOwner = Boolean(currentUserId && post.authorId === currentUserId);
  const savedByMe = post.savedByMe ?? false;
  const mediaUrl = post.media?.[0]?.url || PLACEHOLDER_IMAGE;
  const displayName = authorDisplayName(post.authorId);
  const avatarUrl = authorAvatarUrl(post.authorId);

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

  return (
    <article className="flex flex-col overflow-hidden rounded-lg border border-outline-variant bg-surface-container-lowest shadow-sm transition-all hover:shadow-md md:flex-row">
      <button
        type="button"
        onClick={handleOpenPost}
        className="h-48 flex-shrink-0 bg-surface-variant md:h-auto md:w-1/3"
        aria-label="Xem chi tiết bài viết"
      >
        <img src={mediaUrl} alt="" className="h-full w-full object-cover" />
      </button>

      <div className="flex flex-grow flex-col p-4">
        <div className="mb-2 flex items-start justify-between gap-2">
          <button
            type="button"
            onClick={handleViewProfile}
            className="flex min-w-0 items-center gap-2 text-left"
          >
            <img src={avatarUrl} alt="" className="h-8 w-8 shrink-0 rounded-full object-cover" />
            <div className="min-w-0">
              <h3 className="truncate text-sm font-medium text-on-surface">{displayName}</h3>
              <p className="text-xs text-on-surface-variant">{formatRelativeTime(post.createdAt)}</p>
            </div>
          </button>
          {currentUserId ? (
            <PostOptionsMenu
              postId={post.postId}
              isOwner={isOwner}
              savedByMe={savedByMe}
              onEdit={() => onEdit?.(post.postId)}
              onDelete={() => onDeletePost?.(post.postId)}
              onToggleSave={() => onToggleSavePost?.(post.postId)}
              isSaving={isSavingPost}
              isDeleting={isDeletingPost}
            />
          ) : null}
        </div>

        <div className="mb-3 flex-grow">
          <PostCaption
            caption={post.caption}
            hashtags={post.hashtags}
            onCaptionClick={handleOpenPost}
            onHashtagClick={onHashtagClick}
          />
        </div>

        <div className="flex items-center gap-4 border-t border-outline-variant pt-3 text-sm text-on-surface-variant">
          <button
            type="button"
            onClick={handleLikeClick}
            className="flex items-center gap-1 transition-colors hover:text-primary"
            aria-label="Xem lượt thích"
          >
            <span className="material-symbols-outlined text-[16px]" aria-hidden="true">
              thumb_up
            </span>
            <span>{formatCount(post.likeCount)}</span>
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
            <span>{formatCount(post.replyCount)}</span>
          </button>
        </div>
      </div>
    </article>
  );
}
