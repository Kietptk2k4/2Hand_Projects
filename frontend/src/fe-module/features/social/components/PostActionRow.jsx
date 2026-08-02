import { LikeCountButton } from "./LikeCountButton";

function formatCount(value) {
  const num = Number(value) || 0;
  if (num >= 1000) {
    return `${(num / 1000).toFixed(1).replace(/\.0$/, "")}k`;
  }
  return String(num);
}

export function PostActionRow({
  post,
  onReplyClick,
  onComingSoon,
  onToggleLikePost,
  onOpenLikesList,
  onToggleSavePost,
  isLikingPost = false,
  isSavingPost = false,
  variant = "feed",
}) {
  const likedByMe = post?.likedByMe ?? false;
  const savedByMe = post?.savedByMe ?? false;

  const stopAnd = (handler) => (event) => {
    event?.stopPropagation();
    handler?.(event);
  };

  return (
    <div
      className={`flex items-center justify-between text-on-surface-variant/70 ${
        variant === "detail"
          ? "w-full border-y border-outline-variant/40 py-1.5 px-1"
          : "-ml-2 mt-3 max-w-[425px]"
      }`}
    >
      {/* Reply Action */}
      <button
        type="button"
        className="group/btn flex items-center gap-1.5 text-xs transition-colors hover:text-sky-500"
        onClick={stopAnd(onReplyClick)}
        aria-label="Bình luận"
        disabled={post?.allowComments === false}
      >
        <div className="flex h-8 w-8 items-center justify-center rounded-full transition-colors group-hover/btn:bg-sky-500/10">
          <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
            chat_bubble_outline
          </span>
        </div>
        <span className="font-medium">{formatCount(post?.replyCount)}</span>
      </button>

      {/* Repost/Retweet Action */}
      <button
        type="button"
        className="group/btn flex items-center gap-1.5 text-xs transition-colors hover:text-emerald-500"
        onClick={stopAnd(onComingSoon)}
        aria-label="Repost"
      >
        <div className="flex h-8 w-8 items-center justify-center rounded-full transition-colors group-hover/btn:bg-emerald-500/10">
          <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
            repeat
          </span>
        </div>
        <span className="font-medium">0</span>
      </button>

      {/* Like Action */}
      <div className="flex items-center">
        <button
          type="button"
          className={`group/btn flex items-center gap-1.5 text-xs transition-colors ${
            likedByMe ? "text-pink-600" : "hover:text-pink-600"
          }`}
          onClick={stopAnd(() => onToggleLikePost?.(post?.postId))}
          disabled={isLikingPost || !onToggleLikePost}
          aria-label={likedByMe ? "Bỏ thích" : "Thích"}
          aria-pressed={likedByMe}
        >
          <div className="flex h-8 w-8 items-center justify-center rounded-full transition-colors group-hover/btn:bg-pink-600/10">
            <span
              className={`material-symbols-outlined text-[18px] ${
                likedByMe ? "fill text-pink-600" : ""
              }`}
              aria-hidden="true"
            >
              favorite
            </span>
          </div>
        </button>
        <LikeCountButton
          count={post?.likeCount}
          showZero
          onPress={stopAnd(() =>
            onOpenLikesList?.({
              type: "post",
              targetId: post?.postId,
              likeCount: post?.likeCount,
            })
          )}
        />
      </div>

      {/* View / Analytics Action */}
      <button
        type="button"
        className="group/btn flex items-center gap-1.5 text-xs transition-colors hover:text-sky-500"
        onClick={stopAnd(onComingSoon)}
        aria-label="Lượt xem"
      >
        <div className="flex h-8 w-8 items-center justify-center rounded-full transition-colors group-hover/btn:bg-sky-500/10">
          <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
            bar_chart
          </span>
        </div>
        <span className="font-medium">{formatCount(post?.viewCount || 0)}</span>
      </button>

      {/* Bookmark & Share Actions */}
      <div className="flex items-center">
        <button
          type="button"
          className={`group/btn flex items-center text-xs transition-colors ${
            savedByMe ? "text-sky-500" : "hover:text-sky-500"
          }`}
          onClick={stopAnd(() => onToggleSavePost?.(post?.postId))}
          disabled={isSavingPost}
          aria-label={savedByMe ? "Bỏ lưu" : "Lưu bài viết"}
        >
          <div className="flex h-8 w-8 items-center justify-center rounded-full transition-colors group-hover/btn:bg-sky-500/10">
            <span
              className={`material-symbols-outlined text-[18px] ${
                savedByMe ? "fill text-sky-500" : ""
              }`}
              aria-hidden="true"
            >
              bookmark
            </span>
          </div>
        </button>
        <button
          type="button"
          className="group/btn flex items-center text-xs transition-colors hover:text-sky-500"
          onClick={stopAnd(onComingSoon)}
          aria-label="Chia sẻ"
        >
          <div className="flex h-8 w-8 items-center justify-center rounded-full transition-colors group-hover/btn:bg-sky-500/10">
            <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
              ios_share
            </span>
          </div>
        </button>
      </div>
    </div>
  );
}
