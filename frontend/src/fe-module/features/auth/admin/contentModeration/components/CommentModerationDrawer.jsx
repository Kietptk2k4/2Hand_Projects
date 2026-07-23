import { useCallback, useState } from "react";
import { FeedToast } from "../../../../social/components/FeedToast.jsx";
import {
  buildModerateSuccessToast,
  buildRestoreSuccessToast,
} from "../constants/socialModerationConstants.js";
import { useContentModerationPermissions } from "../hooks/useContentModerationPermissions.js";
import { useCommentModerationDetail } from "../hooks/useCommentModerationDetail.js";
import { useModerateComment } from "../hooks/useModerateComment.js";
import { useRestoreComment } from "../hooks/useRestoreComment.js";
import { resolveCommentAuthorSummary } from "../utils/commentModerationListMapper.js";
import { SocialModerateDialog } from "./SocialModerateDialog.jsx";
import { SocialRestoreDialog } from "./SocialRestoreDialog.jsx";
import { CommentModerationDrawerView } from "./CommentModerationDrawerView.jsx";

const TARGET_LABEL = "bình luận";

export function CommentModerationDrawer({ commentId, comment, onClose, onRefresh }) {
  const { canModerateComment, canRestoreComment } = useContentModerationPermissions();
  const { detail, status: detailStatus, errorMessage: detailErrorMessage } =
    useCommentModerationDetail(commentId);
  const [toastMessage, setToastMessage] = useState("");
  const [moderateOpen, setModerateOpen] = useState(false);
  const [restoreOpen, setRestoreOpen] = useState(false);
  const [lastResult, setLastResult] = useState(null);
  const [historyRefreshToken, setHistoryRefreshToken] = useState(0);

  const bumpHistoryRefresh = useCallback(() => {
    setHistoryRefreshToken((value) => value + 1);
  }, []);

  const handleModerateSuccess = useCallback(
    (result, action) => {
      setModerateOpen(false);
      setLastResult({ ...result, action });
      setToastMessage(buildModerateSuccessToast(TARGET_LABEL, action));
      bumpHistoryRefresh();
      onRefresh?.();
    },
    [bumpHistoryRefresh, onRefresh],
  );

  const handleRestoreSuccess = useCallback(
    (result) => {
      setRestoreOpen(false);
      setLastResult(result);
      setToastMessage(buildRestoreSuccessToast(TARGET_LABEL));
      bumpHistoryRefresh();
      onRefresh?.();
    },
    [bumpHistoryRefresh, onRefresh],
  );

  const {
    isSubmitting: isModerating,
    submitError: moderateError,
    submit: submitModerate,
    clearError: clearModerateError,
  } = useModerateComment({ onSuccess: handleModerateSuccess });

  const {
    isSubmitting: isRestoring,
    submitError: restoreError,
    submit: submitRestore,
    clearError: clearRestoreError,
  } = useRestoreComment({ onSuccess: handleRestoreSuccess });

  const disabled = isModerating || isRestoring;
  const previewComment = detail || comment;
  const authorSummary = detail?.author
    ? {
        userId: detail.author.userId,
        displayName: detail.author.displayName,
        avatarUrl: detail.author.avatarUrl,
      }
    : comment
      ? resolveCommentAuthorSummary(comment)
      : null;

  if (!commentId) return null;

  return (
    <>
      <CommentModerationDrawerView
        commentId={commentId}
        comment={previewComment}
        authorSummary={authorSummary}
        detailStatus={detailStatus}
        detailErrorMessage={detailErrorMessage}
        canModerateComment={canModerateComment}
        canRestoreComment={canRestoreComment}
        lastResult={lastResult}
        historyRefreshToken={historyRefreshToken}
        disabled={disabled}
        onClose={onClose}
        onModerate={() => setModerateOpen(true)}
        onRestore={() => setRestoreOpen(true)}
      />

      <SocialModerateDialog
        open={moderateOpen}
        targetLabel={TARGET_LABEL}
        targetId={commentId}
        isSubmitting={isModerating}
        submitError={moderateError}
        onClose={() => {
          if (isModerating) return;
          setModerateOpen(false);
          clearModerateError();
        }}
        onSubmit={async ({ action, reason }) => {
          if (!commentId) return;
          await submitModerate(commentId, { action, reason });
        }}
      />

      <SocialRestoreDialog
        open={restoreOpen}
        targetLabel={TARGET_LABEL}
        targetId={commentId}
        isSubmitting={isRestoring}
        submitError={restoreError}
        onClose={() => {
          if (isRestoring) return;
          setRestoreOpen(false);
          clearRestoreError();
        }}
        onSubmit={async ({ reason }) => {
          if (!commentId) return;
          await submitRestore(commentId, { reason });
        }}
      />

      <FeedToast message={toastMessage} onDismiss={() => setToastMessage("")} />
    </>
  );
}
