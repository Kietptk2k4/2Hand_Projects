import { authorAvatarUrl, authorDisplayName } from "../utils/authorDisplay";
import { formatRelativeTime } from "../utils/formatRelativeTime";
import { PostCaption } from "./PostCaption";
import { PostMediaGrid } from "./PostMediaGrid";

function formatCount(value) {
  const num = Number(value) || 0;
  if (num >= 1000) {
    return `${(num / 1000).toFixed(1).replace(/\.0$/, "")}k`;
  }
  return String(num);
}

export function PostCard({ post, onOpenPost, onComingSoon }) {
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
          <button
            type="button"
            className="flex items-center gap-3 text-left"
            onClick={stopAnd(onComingSoon)}
            aria-label="Xem hồ sơ tác giả"
          >
            <img
              src={authorAvatarUrl(post.authorId)}
              alt=""
              className="h-12 w-12 rounded-full object-cover"
            />
            <div>
              <h4 className="text-sm font-semibold text-on-surface">
                {authorDisplayName(post.authorId)}
              </h4>
              <p className="text-sm text-on-surface-variant">Thành viên 2Hands</p>
              <span className="mt-1 flex items-center gap-1 text-xs text-outline">
                <span className="material-symbols-outlined text-[14px]" aria-hidden="true">
                  schedule
                </span>
                {formatRelativeTime(post.createdAt)}
              </span>
            </div>
          </button>
          <button
            type="button"
            className="p-1 text-on-surface-variant hover:text-on-surface"
            aria-label="Tùy chọn bài viết"
            onClick={stopAnd(onComingSoon)}
          >
            <span className="material-symbols-outlined" aria-hidden="true">
              more_horiz
            </span>
          </button>
        </div>
        <PostCaption
          caption={post.caption}
          hashtags={post.hashtags}
          onCaptionClick={() => openDetail()}
          onHashtagClick={() => onComingSoon?.()}
        />
      </div>

      <PostMediaGrid media={post.media} onMediaClick={() => openDetail()} />

      <div className="flex items-center justify-between border-t border-outline-variant bg-[#f9f9ff] p-6">
        <div className="flex gap-4">
          <button
            type="button"
            className="flex items-center gap-2 text-on-surface-variant transition-colors hover:text-primary"
            onClick={stopAnd(onComingSoon)}
            aria-label="Thích bài viết"
          >
            <span className="material-symbols-outlined" aria-hidden="true">
              thumb_up
            </span>
            <span className="text-sm font-medium">{formatCount(post.likeCount)}</span>
          </button>
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
          <span className="text-sm font-medium">Share</span>
        </button>
      </div>
    </article>
  );
}
