import { formatRelativeTime } from "../utils/formatRelativeTime";

const DEFAULT_AVATAR = "https://i.pravatar.cc/80?img=11";

export function CommentItem({
  comment,
  replies = [],
  isRepliesLoading,
  isRepliesExpanded,
  onExpandReplies,
  onComingSoon,
  onViewProfile,
}) {
  const avatarUrl = comment.author?.avatarUrl || DEFAULT_AVATAR;
  const displayName = comment.author?.displayName || "User";
  const authorUserId = comment.author?.userId;

  const openAuthorProfile = (event) => {
    event.stopPropagation();
    if (authorUserId) onViewProfile?.(authorUserId);
  };

  return (
    <div className="flex flex-col gap-3">
      <div className="flex gap-3">
        <button
          type="button"
          onClick={openAuthorProfile}
          className="shrink-0"
          aria-label={`Xem hồ sơ ${displayName}`}
        >
          <img src={avatarUrl} alt="" className="h-10 w-10 rounded-full object-cover" />
        </button>
        <div className="min-w-0 flex-1">
          <div className="rounded-lg bg-surface-container-low px-3 py-2">
            <button
              type="button"
              onClick={openAuthorProfile}
              className="text-sm font-semibold text-on-surface hover:text-primary"
            >
              {displayName}
            </button>
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
              <button
                type="button"
                onClick={(event) => {
                  event.stopPropagation();
                  if (reply.author?.userId) onViewProfile?.(reply.author.userId);
                }}
                className="shrink-0"
                aria-label="Xem hồ sơ"
              >
                <img
                  src={reply.author?.avatarUrl || DEFAULT_AVATAR}
                  alt=""
                  className="h-8 w-8 rounded-full object-cover"
                />
              </button>
              <div className="min-w-0 flex-1">
                <div className="rounded-lg bg-surface-container-low px-3 py-2">
                  <button
                    type="button"
                    onClick={(event) => {
                      event.stopPropagation();
                      if (reply.author?.userId) onViewProfile?.(reply.author.userId);
                    }}
                    className="text-sm font-semibold text-on-surface hover:text-primary"
                  >
                    {reply.author?.displayName || "User"}
                  </button>
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
