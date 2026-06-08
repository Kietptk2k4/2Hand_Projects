import { useEffect, useState } from "react";
import { DEFAULT_USER_DISPLAY_NAME } from "../constants/socialUiStrings";
import { useSocialWriteBlock } from "../context/SocialWriteBlockContext";
import { useCommentMediaUpload } from "../hooks/useCommentMediaUpload";
import { formatRelativeTime } from "../utils/formatRelativeTime";
import { CommentComposer } from "./CommentComposer";
import { CommentMediaDisplay } from "./CommentMediaDisplay";
import { LikeCountButton } from "./LikeCountButton";

const DEFAULT_AVATAR = "https://i.pravatar.cc/80?img=11";

function CommentActions({
  commentId,
  createdAt,
  likeCount = 0,
  likedByMe = false,
  canDelete,
  isDeleting,
  onDelete,
  onToggleLike,
  onOpenLikesList,
  isLiking = false,
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
      <button
        type="button"
        className={`font-medium hover:text-primary ${likedByMe ? "text-primary" : ""}`}
        onClick={onToggleLike}
        disabled={writeDisabled || isLiking || !onToggleLike}
        aria-pressed={likedByMe}
      >
        {likedByMe ? "Đã thích" : "Thích"}
      </button>
      <LikeCountButton
        count={likeCount}
        size="compact"
        onPress={() =>
          onOpenLikesList?.({
            type: "comment",
            targetId: commentId,
            likeCount,
          })
        }
      />
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
  onToggleLike,
  onOpenLikesList,
  onToggleReplyLike,
  likingCommentId = null,
  onViewProfile,
  replyingToId,
  onStartReply,
  onCancelReply,
  onSubmitReply,
  isSubmittingReply = false,
  canDeleteComment,
  onDeleteComment,
  deletingCommentId,
  onOpenCommentMedia,
}) {
  const { isWriteBlocked } = useSocialWriteBlock();
  const replyMediaUpload = useCommentMediaUpload();
  const { mediaItems: replyMediaItems, resetMedia: resetReplyMedia } = replyMediaUpload;
  const [replyDraft, setReplyDraft] = useState("");
  const avatarUrl = comment.author?.avatarUrl || DEFAULT_AVATAR;
  const displayName = comment.author?.displayName || DEFAULT_USER_DISPLAY_NAME;
  const authorUserId = comment.author?.userId;
  const isTopLevel = !comment.parentCommentId;
  const isReplying = replyingToId === comment.commentId;
  const canDeleteTop = canDeleteComment?.(comment);

  useEffect(() => {
    if (!isReplying) {
      setReplyDraft("");
      resetReplyMedia();
    }
  }, [isReplying, resetReplyMedia]);

  const openAuthorProfile = (event) => {
    event.stopPropagation();
    if (authorUserId) onViewProfile?.(authorUserId);
  };

  const handleSubmitReply = async () => {
    const result = await onSubmitReply?.(
      comment.commentId,
      replyDraft,
      replyMediaItems
    );
    if (result?.ok) {
      setReplyDraft("");
      resetReplyMedia();
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
            <CommentMediaDisplay
              media={comment.media}
              onMediaClick={(index) =>
                onOpenCommentMedia?.(comment.commentId, comment.media, index)
              }
            />
          </div>
          <CommentActions
            commentId={comment.commentId}
            createdAt={comment.createdAt}
            likeCount={comment.likeCount ?? 0}
            likedByMe={comment.likedByMe ?? false}
            canDelete={canDeleteTop}
            isDeleting={deletingCommentId === comment.commentId}
            onDelete={handleDeleteTop}
            onToggleLike={() => onToggleLike?.(comment.commentId)}
            isLiking={likingCommentId === comment.commentId}
            showReply={isTopLevel}
            onStartReply={() => onStartReply?.(comment.commentId)}
            isSubmittingReply={isSubmittingReply}
            replyCount={comment.replyCount}
            isRepliesExpanded={isRepliesExpanded}
            isRepliesLoading={isRepliesLoading}
            onExpandReplies={() => onExpandReplies(comment.commentId)}
            onOpenLikesList={onOpenLikesList}
            writeDisabled={isWriteBlocked}
          />

          {isTopLevel && isReplying ? (
            <div className="mt-3 space-y-2">
              <CommentComposer
                variant="compact"
                value={replyDraft}
                onChange={setReplyDraft}
                onSubmit={handleSubmitReply}
                mediaUpload={replyMediaUpload}
                placeholder="Viết phản hồi..."
                disabled={isWriteBlocked}
                isSubmitting={isSubmittingReply}
              />
              <button
                type="button"
                onClick={onCancelReply}
                disabled={isSubmittingReply}
                className="text-xs font-medium text-on-surface-variant hover:text-primary"
              >
                Hủy
              </button>
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
                      {reply.author?.displayName || DEFAULT_USER_DISPLAY_NAME}
                    </button>
                    <p className="mt-0.5 text-sm text-on-surface">{reply.contentText}</p>
                    <CommentMediaDisplay
                      media={reply.media}
                      onMediaClick={(index) =>
                        onOpenCommentMedia?.(reply.commentId, reply.media, index)
                      }
                    />
                  </div>
                  <CommentActions
                    commentId={reply.commentId}
                    createdAt={reply.createdAt}
                    likeCount={reply.likeCount ?? 0}
                    likedByMe={reply.likedByMe ?? false}
                    canDelete={canDeleteReply}
                    isDeleting={deletingCommentId === reply.commentId}
                    onDelete={() => onDeleteComment?.(reply.commentId, comment.commentId)}
                    onToggleLike={() =>
                      onToggleReplyLike?.(reply.commentId, comment.commentId)
                    }
                    onOpenLikesList={onOpenLikesList}
                    isLiking={likingCommentId === reply.commentId}
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
