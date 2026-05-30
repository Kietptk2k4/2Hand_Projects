import { useEffect, useState } from "react";
import { MAX_COMMENT_LENGTH } from "../constants/commentConstants";
import { useSocialWriteBlock } from "../context/SocialWriteBlockContext";
import { formatRelativeTime } from "../utils/formatRelativeTime";

const DEFAULT_AVATAR = "https://i.pravatar.cc/80?img=11";

function CommentActions({
  commentId,
  createdAt,
  canDelete,
  isDeleting,
  onDelete,
  onComingSoon,
  showReply,
  onStartReply,
  isSubmittingReply,
  replyCount,
  isRepliesExpanded,
  isRepliesLoading,
  onExpandReplies,
  writeDisabled = false,
}) {
  return (
    <div className="mt-1 flex flex-wrap items-center gap-3 text-xs text-on-surface-variant">
      <span>{formatRelativeTime(createdAt)}</span>
      <button type="button" className="font-medium hover:text-primary" onClick={onComingSoon}>
        Thích
      </button>
      {showReply ? (
        <button
          type="button"
          className="font-medium hover:text-primary"
          onClick={onStartReply}
          disabled={writeDisabled || isSubmittingReply}
        >
          Trả lời
        </button>
      ) : null}
      {canDelete ? (
        <button
          type="button"
          className="font-medium text-error hover:underline"
          onClick={onDelete}
          disabled={writeDisabled || isDeleting}
        >
          {isDeleting ? "Đang xóa..." : "Xóa"}
        </button>
      ) : null}
      {replyCount > 0 && !isRepliesExpanded ? (
        <button
          type="button"
          className="font-medium text-primary hover:underline"
          onClick={onExpandReplies}
          disabled={isRepliesLoading}
        >
          {isRepliesLoading ? "Đang tải..." : `Xem ${replyCount} phản hồi`}
        </button>
      ) : null}
    </div>
  );
}

export function CommentItem({
  comment,
  replies = [],
  isRepliesLoading,
  isRepliesExpanded,
  onExpandReplies,
  onComingSoon,
  onViewProfile,
  replyingToId,
  onStartReply,
  onCancelReply,
  onSubmitReply,
  isSubmittingReply = false,
  canDeleteComment,
  onDeleteComment,
  deletingCommentId,
}) {
  const { isWriteBlocked } = useSocialWriteBlock();
  const [replyDraft, setReplyDraft] = useState("");
  const avatarUrl = comment.author?.avatarUrl || DEFAULT_AVATAR;
  const displayName = comment.author?.displayName || "User";
  const authorUserId = comment.author?.userId;
  const isTopLevel = !comment.parentCommentId;
  const isReplying = replyingToId === comment.commentId;
  const canDeleteTop = canDeleteComment?.(comment);

  useEffect(() => {
    if (!isReplying) {
      setReplyDraft("");
    }
  }, [isReplying]);

  const openAuthorProfile = (event) => {
    event.stopPropagation();
    if (authorUserId) onViewProfile?.(authorUserId);
  };

  const handleSubmitReply = async () => {
    const result = await onSubmitReply?.(comment.commentId, replyDraft);
    if (result?.ok) {
      setReplyDraft("");
    }
  };

  const handleReplyKeyDown = (event) => {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault();
      handleSubmitReply();
    }
  };

  const handleDeleteTop = () => {
    onDeleteComment?.(comment.commentId, null);
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
          <CommentActions
            commentId={comment.commentId}
            createdAt={comment.createdAt}
            canDelete={canDeleteTop}
            isDeleting={deletingCommentId === comment.commentId}
            onDelete={handleDeleteTop}
            onComingSoon={onComingSoon}
            showReply={isTopLevel}
            onStartReply={() => onStartReply?.(comment.commentId)}
            isSubmittingReply={isSubmittingReply}
            replyCount={comment.replyCount}
            isRepliesExpanded={isRepliesExpanded}
            isRepliesLoading={isRepliesLoading}
            onExpandReplies={() => onExpandReplies(comment.commentId)}
            writeDisabled={isWriteBlocked}
          />

          {isTopLevel && isReplying ? (
            <div className="mt-3 space-y-2">
              <input
                type="text"
                value={replyDraft}
                onChange={(event) => setReplyDraft(event.target.value)}
                onKeyDown={handleReplyKeyDown}
                maxLength={MAX_COMMENT_LENGTH}
                placeholder="Viết phản hồi..."
                disabled={isWriteBlocked || isSubmittingReply}
                className="w-full rounded-lg border border-outline-variant bg-surface-container-lowest px-3 py-2 text-sm outline-none focus:border-primary focus:ring-1 focus:ring-primary disabled:opacity-60"
                autoFocus
              />
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={handleSubmitReply}
                  disabled={isWriteBlocked || isSubmittingReply || !replyDraft.trim()}
                  className="rounded-lg bg-primary px-3 py-1.5 text-xs font-medium text-on-primary hover:bg-[#0050cb] disabled:opacity-50"
                >
                  {isSubmittingReply ? "Đang gửi..." : "Gửi"}
                </button>
                <button
                  type="button"
                  onClick={onCancelReply}
                  disabled={isSubmittingReply}
                  className="text-xs font-medium text-on-surface-variant hover:text-primary"
                >
                  Hủy
                </button>
              </div>
            </div>
          ) : null}
        </div>
      </div>

      {isRepliesExpanded ? (
        <div className="ml-10 space-y-3 border-l-2 border-outline-variant pl-4">
          {replies.map((reply) => {
            const canDeleteReply = canDeleteComment?.(reply);
            return (
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
                  <CommentActions
                    commentId={reply.commentId}
                    createdAt={reply.createdAt}
                    canDelete={canDeleteReply}
                    isDeleting={deletingCommentId === reply.commentId}
                    onDelete={() => onDeleteComment?.(reply.commentId, comment.commentId)}
                    onComingSoon={onComingSoon}
                    showReply={false}
                    replyCount={0}
                    isRepliesExpanded={false}
                    isRepliesLoading={false}
                    onExpandReplies={() => {}}
                    writeDisabled={isWriteBlocked}
                  />
                </div>
              </div>
            );
          })}
        </div>
      ) : null}
    </div>
  );
}
