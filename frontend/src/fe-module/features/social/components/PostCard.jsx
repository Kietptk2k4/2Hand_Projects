import { formatRelativeTime } from "../utils/formatRelativeTime";
import { useEnrichedProductTags } from "../hooks/useEnrichedProductTags";
import { usePostAuthorDisplay } from "../hooks/usePostAuthorDisplay";
import { PostCaption } from "./PostCaption";
import { PostMediaGrid } from "./PostMediaGrid";
import { PostOptionsMenu } from "./PostOptionsMenu";
import { PostProductTagsBlock } from "./PostProductTagsBlock";
import { LikeCountButton } from "./LikeCountButton";

function formatCount(value) {
  const num = Number(value) || 0;
  if (num >= 1000) {
    return `${(num / 1000).toFixed(1).replace(/\.0$/, "")}k`;
  }
  return String(num);
}

export function PostCard({
  post,
  onOpenPost,
  onComingSoon,
  onEdit,
  onDeletePost,
  onToggleSavePost,
  onToggleLikePost,
  onOpenLikesList,
  isSavingPost = false,
  isLikingPost = false,
  isDeletingPost = false,
  onViewProfile,
  onHashtagClick,
  onViewProduct,
  currentUserId,
}) {
  const isOwner = Boolean(currentUserId && post.authorId === currentUserId);
  const savedByMe = post.savedByMe ?? false;
  const likedByMe = post.likedByMe ?? false;
  const enrichedProductTags = useEnrichedProductTags(post.productTags);
  const author = usePostAuthorDisplay(post.authorId);
  const openDetail = (options) => {
    onOpenPost?.(post.postId, options);
  };

  const stopAnd = (handler) => (event) => {
    event.stopPropagation();
    handler?.(event);
  };

  return (
    <article className="overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest shadow-sm transition-shadow hover:shadow-md">
      <div className="p-6">
        <div className="mb-4 flex items-start justify-between">
          <div className="flex min-w-0 items-center gap-3">
            <button
              type="button"
              className="shrink-0"
              onClick={stopAnd(() => onViewProfile?.(post.authorId))}
              aria-label="Xem hồ sơ tác giả"
            >
              <img
                src={author.avatarUrl}
                alt=""
                className="h-12 w-12 rounded-full object-cover"
              />
            </button>
            <button
              type="button"
              className="min-w-0 text-left"
              onClick={stopAnd(() => onViewProfile?.(post.authorId))}
            >
              <h4 className="truncate text-sm font-semibold text-on-surface hover:text-primary">
                {author.displayName}
              </h4>
              <p className="text-sm text-on-surface-variant">Thành viên 2Hands</p>
              <span className="mt-1 flex items-center gap-1 text-xs text-outline">
                <span className="material-symbols-outlined text-[14px]" aria-hidden="true">
                  schedule
                </span>
                {formatRelativeTime(post.createdAt)}
              </span>
            </button>
          </div>
          {currentUserId ? (
            <PostOptionsMenu
              postId={post.postId}
              isOwner={isOwner}
              savedByMe={savedByMe}
              icon="more_horiz"
              onEdit={() => onEdit?.(post.postId)}
              onDelete={() => onDeletePost?.(post.postId)}
              onToggleSave={() => onToggleSavePost?.(post.postId)}
              isSaving={isSavingPost}
              isDeleting={isDeletingPost}
            />
          ) : null}
        </div>
        <PostCaption
          caption={post.caption}
          hashtags={post.hashtags}
          onCaptionClick={() => openDetail()}
          onHashtagClick={(tag) => {
            if (onHashtagClick) {
              onHashtagClick(tag);
              return;
            }
            onComingSoon?.();
          }}
        />
        {enrichedProductTags.length > 0 ? (
          <PostProductTagsBlock
            tags={enrichedProductTags}
            variant="compact"
            onViewProduct={onViewProduct}
          />
        ) : null}
      </div>

      <PostMediaGrid
        media={post.media}
        postId={post.postId}
        surface="feed"
        onMediaClick={() => openDetail()}
      />

      <div className="flex items-center justify-between border-t border-outline-variant bg-[#f9f9ff] p-6">
        <div className="flex gap-4">
          <div className="flex items-center gap-2">
            <button
              type="button"
              className="flex items-center text-on-surface-variant transition-colors hover:text-primary disabled:opacity-50"
              onClick={stopAnd(() => onToggleLikePost?.(post.postId))}
              disabled={isLikingPost || !onToggleLikePost}
              aria-label={likedByMe ? "Bỏ thích bài viết" : "Thích bài viết"}
              aria-pressed={likedByMe}
            >
              <span
                className={`material-symbols-outlined ${likedByMe ? "fill text-primary" : ""}`}
                aria-hidden="true"
              >
                thumb_up
              </span>
            </button>
            <LikeCountButton
              count={post.likeCount}
              showZero
              onPress={() =>
                onOpenLikesList?.({
                  type: "post",
                  targetId: post.postId,
                  likeCount: post.likeCount,
                })
              }
            />
          </div>
          <button
            type="button"
            className="flex items-center gap-2 text-on-surface-variant transition-colors hover:text-primary"
            onClick={() => openDetail({ focusComments: true })}
            aria-label="Bình luận"
            disabled={post.allowComments === false}
          >
            <span className="material-symbols-outlined" aria-hidden="true">
              chat_bubble_outline
            </span>
            <span className="text-sm font-medium">{formatCount(post.replyCount)}</span>
          </button>
        </div>
        <button
          type="button"
          className="flex items-center gap-2 text-on-surface-variant transition-colors hover:text-primary"
          onClick={stopAnd(onComingSoon)}
          aria-label="Chia sẻ"
        >
          <span className="material-symbols-outlined" aria-hidden="true">
            share
          </span>
          <span className="text-sm font-medium">Chia sẻ</span>
        </button>
      </div>
    </article>
  );
}
