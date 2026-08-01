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
    <div className="mt-1.5 flex flex-wrap items-center gap-4 text-xs text-on-surface-variant/70">
      {/* Reply Action */}
      {showReply ? (
        <button
          type="button"
          className="group/btn flex items-center gap-1 transition-colors hover:text-sky-500 font-medium"
          onClick={onStartReply}
          disabled={writeDisabled || isSubmittingReply}
        >
          <div className="flex h-7 w-7 items-center justify-center rounded-full transition-colors group-hover/btn:bg-sky-500/10">
            <span className="material-symbols-outlined text-[16px]" aria-hidden="true">
              chat_bubble_outline
            </span>
          </div>
          <span>Trả lời</span>
        </button>
      ) : null}

      {/* Like Action */}
      <div className="flex items-center">
        <button
          type="button"
          className={`group/btn flex items-center gap-1 transition-colors font-medium ${
            likedByMe ? "text-pink-600" : "hover:text-pink-600"
          }`}
          onClick={onToggleLike}
          disabled={writeDisabled || isLiking || !onToggleLike}
          aria-pressed={likedByMe}
        >
          <div className="flex h-7 w-7 items-center justify-center rounded-full transition-colors group-hover/btn:bg-pink-600/10">
            <span
              className={`material-symbols-outlined text-[16px] ${
                likedByMe ? "fill text-pink-600" : ""
              }`}
              aria-hidden="true"
            >
              favorite
            </span>
          </div>
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
      </div>

      {/* Delete Action */}
      {canDelete ? (
        <button
          type="button"
          className="font-medium text-error hover:underline text-xs"
          onClick={onDelete}
          disabled={writeDisabled || isDeleting}
        >
          {isDeleting ? "Đang xóa..." : "Xóa"}
        </button>
      ) : null}

      {/* Expand replies */}
      {replyCount > 0 && !isRepliesExpanded ? (
        <button
          type="button"
          className="font-semibold text-sky-500 hover:underline text-xs"
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
    <div className="flex flex-col border-b border-outline-variant/20 py-3 last:border-b-0">
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
          {/* Post-row sub header: Name + @handle + dot + timestamp */}
          <div className="flex items-center gap-1 text-sm">
            <button
              type="button"
              onClick={openAuthorProfile}
              className="font-bold text-on-surface hover:underline truncate text-[15px]"
            >
              {displayName}
            </button>
            <span className="text-xs text-on-surface-variant/70 truncate">
              {(() => {
                const email = comment.author?.email || comment.author?.username || "";
                return email ? (email.includes("@") ? `@${email.split("@")[0]}` : `@${email}`) : `@${(displayName || "user").toLowerCase().replace(/[^a-z0-9]/gi, "")}`;
              })()}
            </span>
            <span className="text-xs text-on-surface-variant/60">·</span>
            <span className="text-xs text-on-surface-variant/70">
              {formatRelativeTime(comment.createdAt)}
            </span>
          </div>

          <p className="mt-1 text-[15px] leading-normal text-on-surface">{comment.contentText}</p>

          {comment.media && comment.media.length > 0 ? (
            <div className="mt-2 overflow-hidden rounded-2xl border border-outline-variant/30">
              <CommentMediaDisplay
                media={comment.media}
                onMediaClick={(index) =>
                  onOpenCommentMedia?.(comment.commentId, comment.media, index)
                }
              />
            </div>
          ) : null}

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
                className="text-xs font-medium text-on-surface-variant hover:text-sky-500"
              >
                Hủy
              </button>
            </div>
          ) : null}
        </div>
      </div>

      {isRepliesExpanded ? (
        <div className="ml-10 mt-2 space-y-2 border-l-2 border-outline-variant/40 pl-3">
          {replies.map((reply) => {
            const canDeleteReply = canDeleteComment?.(reply);
            return (
              <div key={reply.commentId} className="flex gap-2.5 py-2">
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
                  <div className="flex items-center gap-1 text-sm">
                    <button
                      type="button"
                      onClick={(event) => {
                        event.stopPropagation();
                        if (reply.author?.userId) onViewProfile?.(reply.author.userId);
                      }}
                      className="font-bold text-on-surface hover:underline text-[14px]"
                    >
                      {reply.author?.displayName || DEFAULT_USER_DISPLAY_NAME}
                    </button>
                    <span className="text-xs text-on-surface-variant/70">
                      {(() => {
                        const email = reply.author?.email || reply.author?.username || "";
                        const name = reply.author?.displayName || DEFAULT_USER_DISPLAY_NAME;
                        return email ? (email.includes("@") ? `@${email.split("@")[0]}` : `@${email}`) : `@${name.toLowerCase().replace(/[^a-z0-9]/gi, "")}`;
                      })()}
                    </span>
                    <span className="text-xs text-on-surface-variant/60">·</span>
                    <span className="text-xs text-on-surface-variant/70">
                      {formatRelativeTime(reply.createdAt)}
                    </span>
                  </div>
                  <p className="mt-0.5 text-[14px] leading-normal text-on-surface">{reply.contentText}</p>
                  {reply.media && reply.media.length > 0 ? (
                    <div className="mt-1.5 overflow-hidden rounded-xl border border-outline-variant/30">
                      <CommentMediaDisplay
                        media={reply.media}
                        onMediaClick={(index) =>
                          onOpenCommentMedia?.(reply.commentId, reply.media, index)
                        }
                      />
                    </div>
                  ) : null}
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
