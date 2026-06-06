import { normalizePostMediaUrl } from "../utils/postMediaUrl";
import { PostOptionsMenu } from "./PostOptionsMenu";

export function ProfilePostTile({
  post,
  onOpenPost,
  isOwner = false,
  savedByMe = false,
  onEdit,
  onDeletePost,
  onToggleSavePost,
  isSavingPost = false,
  isDeletingPost = false,
  showMenu = true,
}) {
  const thumbUrl = normalizePostMediaUrl(post.media?.[0]?.url);
  const isDraft = post.status === "DRAFT";

  const openPost = () => onOpenPost?.(post.postId);

  const handleKeyDown = (event) => {
    if (event.target !== event.currentTarget) return;
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      openPost();
    }
  };

  return (
    <div
      role="button"
      tabIndex={0}
      onClick={openPost}
      onKeyDown={handleKeyDown}
      className="group relative aspect-square cursor-pointer overflow-hidden rounded-lg border border-outline-variant/30 bg-surface-container shadow-sm transition-shadow hover:shadow-md"
      aria-label={post.caption ? `Xem bài: ${post.caption.slice(0, 40)}` : "Xem bài viết"}
    >
      {thumbUrl ? (
        <img
          src={thumbUrl}
          alt=""
          className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105"
        />
      ) : (
        <div className="flex h-full w-full flex-col items-center justify-center gap-2 bg-surface-container-high p-3 text-center">
          <span className="material-symbols-outlined text-3xl text-outline" aria-hidden="true">
            article
          </span>
          {post.caption ? (
            <p className="line-clamp-3 text-xs text-on-surface-variant">{post.caption}</p>
          ) : null}
        </div>
      )}
      <div className="pointer-events-none absolute inset-0 bg-on-background/0 transition-colors group-hover:bg-on-background/10" />
      {showMenu ? (
        <div
          className="pointer-events-auto absolute right-2 top-2 z-10"
          onClick={(event) => event.stopPropagation()}
          onKeyDown={(event) => event.stopPropagation()}
        >
          <PostOptionsMenu
            postId={post.postId}
            isOwner={isOwner}
            savedByMe={savedByMe}
            icon="more_horiz"
            className="rounded-full bg-surface-container-lowest/90 shadow-sm backdrop-blur-sm"
            onEdit={() => onEdit?.(post.postId)}
            onDelete={() => onDeletePost?.(post.postId)}
            onToggleSave={() => onToggleSavePost?.(post.postId)}
            isSaving={isSavingPost}
            isDeleting={isDeletingPost}
          />
        </div>
      ) : null}
      {isDraft ? (
        <span className="absolute left-2 top-2 rounded-sm bg-surface-variant/90 px-2 py-0.5 text-xs font-semibold text-on-surface-variant shadow-sm backdrop-blur-sm">
          NHÁP
        </span>
      ) : null}
      {post.media?.length > 1 ? (
        <span
          className="absolute bottom-2 right-2 flex items-center gap-0.5 rounded-sm bg-on-background/60 px-1.5 py-0.5 text-xs text-on-primary"
          aria-hidden="true"
        >
          <span className="material-symbols-outlined text-[14px]">collections</span>
        </span>
      ) : null}
    </div>
  );
}
