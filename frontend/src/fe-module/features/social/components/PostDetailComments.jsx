import { CommentItem } from "./CommentItem";
import { CommentSkeleton } from "./CommentSkeleton";
import { CommentSortSelect } from "./CommentSortSelect";

export function PostDetailComments({
  commentsState,
  onViewProfile,
  onOpenLikesList,
  commentInputRef,
  onDeleteComment,
  onOpenCommentMedia,
}) {
  const {
    comments,
    isLoading,
    isLoadingMore,
    isEmpty,
    errorMessage,
    hasNext,
    loadMore,
    retry,
    repliesByParent,
    replyStatusByParent,
    replyingToId,
    isSubmittingReplyId,
    submitError,
    startReply,
    cancelReply,
    submitReply,
    expandReplies,
    deleteComment: deleteCommentFromState,
    canDeleteComment,
    deletingCommentId,
    toggleCommentLike,
    likingCommentId,
    commentSort,
    setCommentSort,
  } = commentsState;

  const handleDeleteComment =
    onDeleteComment ||
    ((commentId, parentCommentId) =>
      deleteCommentFromState(commentId, { parentCommentId }));

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h4 className="text-sm font-semibold text-on-surface">Bình luận</h4>
        <CommentSortSelect
          value={commentSort}
          onChange={setCommentSort}
          disabled={isLoading}
        />
      </div>

      {submitError ? (
        <p className="text-xs text-error" role="alert">
          {submitError}
        </p>
      ) : null}

      {isLoading ? (
        <div className="space-y-4">
          <CommentSkeleton />
          <CommentSkeleton />
          <CommentSkeleton />
        </div>
      ) : null}

      {!isLoading && errorMessage ? (
        <div className="rounded-lg border border-error/30 bg-error-container/30 p-3 text-sm text-on-error-container">
          <p>{errorMessage}</p>
          <button
            type="button"
            onClick={retry}
            className="mt-2 font-medium text-primary hover:underline"
          >
            Thử lại
          </button>
        </div>
      ) : null}

      {!isLoading && !errorMessage && isEmpty ? (
        <p className="text-sm text-on-surface-variant">Chưa có bình luận nào. Hãy là người đầu tiên!</p>
      ) : null}

      {!isLoading && !errorMessage && comments.length > 0 ? (
        <div className="space-y-4">
          {comments.map((item) => {
            const expanded = repliesByParent[item.commentId] !== undefined;
            return (
              <CommentItem
                key={item.commentId}
                comment={item}
                replies={repliesByParent[item.commentId] || []}
                isRepliesExpanded={expanded}
                isRepliesLoading={replyStatusByParent[item.commentId] === "loading"}
                onExpandReplies={expandReplies}
                onToggleLike={(commentId) =>
                  toggleCommentLike(commentId, { parentCommentId: null })
                }
                onToggleReplyLike={(commentId, parentCommentId) =>
                  toggleCommentLike(commentId, { parentCommentId })
                }
                onOpenLikesList={onOpenLikesList}
                likingCommentId={likingCommentId}
                onViewProfile={onViewProfile}
                replyingToId={replyingToId}
                onStartReply={startReply}
                onCancelReply={cancelReply}
                onSubmitReply={submitReply}
                isSubmittingReply={isSubmittingReplyId === item.commentId}
                canDeleteComment={canDeleteComment}
                onDeleteComment={handleDeleteComment}
                deletingCommentId={deletingCommentId}
                onOpenCommentMedia={onOpenCommentMedia}
              />
            );
          })}
          {hasNext ? (
            <button
              type="button"
              onClick={loadMore}
              disabled={isLoadingMore}
              className="text-sm font-medium text-primary hover:underline disabled:opacity-50"
            >
              {isLoadingMore ? "Đang tải..." : "Xem thêm bình luận"}
            </button>
          ) : null}
        </div>
      ) : null}

      <div ref={commentInputRef} className="h-px w-full scroll-mt-4" aria-hidden="true" />
    </div>
  );
}
