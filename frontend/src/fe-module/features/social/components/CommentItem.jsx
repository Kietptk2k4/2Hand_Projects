import { formatRelativeTime } from "../utils/formatRelativeTime";

const DEFAULT_AVATAR = "https://i.pravatar.cc/80?img=11";

export function CommentItem({
  comment,
  replies = [],
  isRepliesLoading,
  isRepliesExpanded,
  onExpandReplies,
  onComingSoon,
}) {
  const avatarUrl = comment.author?.avatarUrl || DEFAULT_AVATAR;
  const displayName = comment.author?.displayName || "User";

  return (
    <div className="flex flex-col gap-3">
      <div className="flex gap-3">
        <img src={avatarUrl} alt="" className="h-10 w-10 shrink-0 rounded-full object-cover" />
        <div className="min-w-0 flex-1">
          <div className="rounded-lg bg-surface-container-low px-3 py-2">
            <p className="text-sm font-semibold text-on-surface">{displayName}</p>
            <p className="mt-0.5 text-sm text-on-surface">{comment.contentText}</p>
          </div>
          <div className="mt-1 flex flex-wrap items-center gap-3 text-xs text-on-surface-variant">
            <span>{formatRelativeTime(comment.createdAt)}</span>
            <button
              type="button"
              className="font-medium hover:text-primary"
              onClick={onComingSoon}
            >
              Thích
            </button>
            {comment.replyCount > 0 && !isRepliesExpanded ? (
              <button
                type="button"
                className="font-medium text-primary hover:underline"
                onClick={() => onExpandReplies(comment.commentId)}
                disabled={isRepliesLoading}
              >
                {isRepliesLoading
                  ? "Đang tải..."
                  : `Xem ${comment.replyCount} phản hồi`}
              </button>
            ) : null}
          </div>
        </div>
      </div>

      {isRepliesExpanded ? (
        <div className="ml-10 space-y-3 border-l-2 border-outline-variant pl-4">
          {replies.map((reply) => (
            <div key={reply.commentId} className="flex gap-3">
              <img
                src={reply.author?.avatarUrl || DEFAULT_AVATAR}
                alt=""
                className="h-8 w-8 shrink-0 rounded-full object-cover"
              />
              <div className="min-w-0 flex-1">
                <div className="rounded-lg bg-surface-container-low px-3 py-2">
                  <p className="text-sm font-semibold text-on-surface">
                    {reply.author?.displayName || "User"}
                  </p>
                  <p className="mt-0.5 text-sm text-on-surface">{reply.contentText}</p>
                </div>
                <span className="mt-1 block text-xs text-on-surface-variant">
                  {formatRelativeTime(reply.createdAt)}
                </span>
              </div>
            </div>
          ))}
        </div>
      ) : null}
    </div>
  );
}
