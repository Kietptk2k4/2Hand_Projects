import { formatRelativeTime } from "../utils/formatRelativeTime";
import { useEnrichedProductTags } from "../hooks/useEnrichedProductTags";
import { usePostAuthorDisplay } from "../hooks/usePostAuthorDisplay";
import { PostCaption } from "./PostCaption";
import { PostMediaCarousel } from "./PostMediaCarousel";
import { PostOptionsMenu } from "./PostOptionsMenu";
import { PostProductTagsBlock } from "./PostProductTagsBlock";
import { LikeCountButton } from "./LikeCountButton";

import { PostActionRow } from "./PostActionRow";

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
  const author = usePostAuthorDisplay(post.authorId, post);
  const openDetail = (options) => {
    onOpenPost?.(post.postId, options);
  };

  const stopAnd = (handler) => (event) => {
    event.stopPropagation();
    handler?.(event);
  };

  return (
    <article
      onClick={() => openDetail()}
      className="group flex cursor-pointer gap-3 border-b border-outline-variant/40 bg-surface-container-lowest px-4 py-3 transition-colors hover:bg-surface-container-low/30"
    >
      <div className="shrink-0">
        <button
          type="button"
          onClick={stopAnd(() => onViewProfile?.(post.authorId))}
          aria-label="Xem hồ sơ tác giả"
          className="block transition-opacity hover:opacity-85"
        >
          <img
            src={author.avatarUrl}
            alt=""
            className="h-10 w-10 rounded-full object-cover"
          />
        </button>
      </div>

      <div className="min-w-0 flex-1">
        <div className="flex items-center justify-between">
          <div className="flex min-w-0 flex-wrap items-center gap-1 text-sm">
            <button
              type="button"
              className="truncate font-bold text-on-surface hover:underline"
              onClick={stopAnd(() => onViewProfile?.(post.authorId))}
            >
              {author.displayName}
            </button>
            <span className="truncate text-xs text-on-surface-variant/70">
              {author.handle}
            </span>
            <span className="text-xs text-on-surface-variant/60">·</span>
            <span className="text-xs text-on-surface-variant/70 hover:underline">
              {formatRelativeTime(post.createdAt)}
            </span>
          </div>

          {currentUserId ? (
            <div onClick={(e) => e.stopPropagation()}>
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
            </div>
          ) : null}
        </div>

        {post.caption || (post.hashtags && post.hashtags.length > 0) ? (
          <div className="mt-1">
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
          </div>
        ) : null}

        {post.media && post.media.length > 0 ? (
          <div className="mt-2.5 overflow-hidden rounded-2xl border border-outline-variant/30">
            <PostMediaCarousel
              media={post.media}
              postId={post.postId}
              surface="feed"
              onMediaClick={() => openDetail()}
            />
          </div>
        ) : null}

        {enrichedProductTags.length > 0 ? (
          <div className="mt-2.5">
            <PostProductTagsBlock
              tags={enrichedProductTags}
              variant="compact"
              onViewProduct={onViewProduct}
            />
          </div>
        ) : null}

        {/* Action Row - X style (unified component) */}
        <PostActionRow
          post={post}
          onReplyClick={() => openDetail({ focusComments: true })}
          onComingSoon={onComingSoon}
          onToggleLikePost={onToggleLikePost}
          onOpenLikesList={onOpenLikesList}
          onToggleSavePost={onToggleSavePost}
          isLikingPost={isLikingPost}
          isSavingPost={isSavingPost}
          variant="feed"
        />
      </div>
    </article>
  );
}
