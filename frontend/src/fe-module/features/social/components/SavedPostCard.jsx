import { useEnrichedProductTags } from "../hooks/useEnrichedProductTags";
import { usePostAuthorDisplay } from "../hooks/usePostAuthorDisplay";
import { formatSavedAt } from "../utils/formatSavedAt";
import { PostMediaThumbnail } from "./PostMediaItem";
import { PostProductTagsBlock } from "./PostProductTagsBlock";
import { LikeCountButton } from "./LikeCountButton";

const PLACEHOLDER_IMAGE =
  "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='400' height='300' viewBox='0 0 400 300'%3E%3Crect fill='%23dee8ff' width='400' height='300'/%3E%3C/svg%3E";

export function SavedPostCard({
  post,
  onOpenPost,
  onOpenComments,
  onViewProfile,
  onUnsave,
  onViewProduct,
  onOpenLikesList,
  isUnsaveLoading = false,
}) {
  const primaryMedia = post.media?.[0];
  const savedLabel = formatSavedAt(post.savedAt);
  const author = usePostAuthorDisplay(post.authorId);
  const titleText = post.caption?.trim() || "Bài viết không có nội dung";
  const enrichedProductTags = useEnrichedProductTags(post.productTags);

  const handleOpenPost = () => onOpenPost?.(post.postId);
  const handleOpenComments = (event) => {
    event.stopPropagation();
    onOpenComments?.(post.postId);
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
    <article className="flex flex-col gap-3 overflow-hidden rounded-2xl border border-outline-variant/40 bg-surface-container-lowest p-4 shadow-sm transition-all hover:border-sky-500/40 hover:shadow-md md:flex-row md:items-start">
      {primaryMedia ? (
        <button
          type="button"
          onClick={handleOpenPost}
          className="relative h-44 w-full flex-shrink-0 overflow-hidden rounded-xl bg-surface-container-low border border-outline-variant/30 md:h-36 md:w-36 group"
          aria-label="Xem chi tiết bài viết"
        >
          <PostMediaThumbnail item={primaryMedia} fallbackSrc={PLACEHOLDER_IMAGE} />
          <div className="absolute right-2.5 top-2.5 flex items-center gap-1 rounded-full border border-outline-variant/40 bg-surface-container-lowest/90 px-2 py-0.5 text-xs backdrop-blur-sm shadow-xs">
            <span
              className="material-symbols-outlined text-[14px] text-sky-500"
              style={{ fontVariationSettings: "'FILL' 1" }}
              aria-hidden="true"
            >
              bookmark
            </span>
          </div>
        </button>
      ) : null}

      <div className="flex flex-grow flex-col min-w-0">
        <div className="mb-2 flex items-center justify-between gap-2">
          <button
            type="button"
            onClick={handleViewProfile}
            className="flex items-center gap-2.5 text-left group/author"
          >
            <img src={author.avatarUrl} alt="" className="h-8 w-8 rounded-full object-cover border border-outline-variant/40" />
            <div className="flex flex-col min-w-0">
              <h3 className="truncate text-xs font-bold text-on-surface group-hover/author:text-sky-500 transition-colors">
                {author.displayName}
              </h3>
              {savedLabel ? (
                <span className="text-[11px] text-on-surface-variant/60">
                  Đã lưu vào {savedLabel}
                </span>
              ) : null}
            </div>
          </button>
          <button
            type="button"
            title="Bỏ lưu bài viết"
            disabled={isUnsaveLoading}
            onClick={handleUnsave}
            className="flex h-8 w-8 items-center justify-center rounded-full text-on-surface-variant/60 transition-colors hover:bg-error/10 hover:text-error disabled:opacity-50"
            aria-label="Bỏ lưu bài viết"
          >
            <span className="material-symbols-outlined text-[20px]" aria-hidden="true">
              bookmark_remove
            </span>
          </button>
        </div>

        <button type="button" onClick={handleOpenPost} className="text-left group/title">
          <h2 className="mb-1.5 line-clamp-2 text-base font-bold text-on-surface leading-snug group-hover/title:text-sky-500 transition-colors">
            {titleText}
          </h2>
        </button>

        {enrichedProductTags.length > 0 ? (
          <div className="my-2">
            <PostProductTagsBlock
              tags={enrichedProductTags}
              variant="compact"
              onViewProduct={onViewProduct}
            />
          </div>
        ) : null}

        <div className="mt-3 flex items-center justify-between border-t border-outline-variant/30 pt-2.5 text-xs text-on-surface-variant/70">
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-1.5 font-semibold">
              <span
                className="material-symbols-outlined text-[15px] text-sky-500"
                aria-hidden="true"
              >
                thumb_up
              </span>
              <LikeCountButton
                count={post.likeCount}
                size="compact"
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
              onClick={handleOpenComments}
              className="flex items-center gap-1 font-semibold transition-colors hover:text-sky-500"
              aria-label="Xem bình luận"
            >
              <span className="material-symbols-outlined text-[15px]" aria-hidden="true">
                comment
              </span>
              <span>{post.replyCount ?? 0}</span>
            </button>
          </div>
        </div>
      </div>
    </article>
  );
}
