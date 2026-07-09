import { useCallback, useState } from "react";
import { FeedToast } from "../../../../../social/components/FeedToast.jsx";
import {
  buildModerateSuccessToast,
  buildRestoreSuccessToast,
} from "../../constants/socialModerationConstants.js";
import { useContentModerationPermissions } from "../../hooks/useContentModerationPermissions.js";
import { useModerateComment } from "../../hooks/useModerateComment.js";
import { useRestoreComment } from "../../hooks/useRestoreComment.js";
import { CommentModerationForbiddenView, CommentModerationTabView } from "../CommentModerationTabView.jsx";

const TARGET_LABEL = "bình luận";

export function CommentModerationTab({ commentId }) {
  const { canModerateComment, canRestoreComment } = useContentModerationPermissions();
  const [toastMessage, setToastMessage] = useState("");
  const [moderateOpen, setModerateOpen] = useState(false);
  const [restoreOpen, setRestoreOpen] = useState(false);
  const [lastResult, setLastResult] = useState(null);

  const handleModerateSuccess = useCallback((result, action) => {
    setModerateOpen(false);
    setLastResult({ ...result, action });
    setToastMessage(buildModerateSuccessToast(TARGET_LABEL, action));
  }, []);

  const handleRestoreSuccess = useCallback((result) => {
    setRestoreOpen(false);
    setLastResult(result);
    setToastMessage(buildRestoreSuccessToast(TARGET_LABEL));
  }, []);

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

  if (!canModerateComment && !canRestoreComment) {
    return <CommentModerationForbiddenView />;
  }

  return (
    <CommentModerationTabView
      commentId={commentId}
      canModerateComment={canModerateComment}
      canRestoreComment={canRestoreComment}
      lastResult={lastResult}
      disabled={disabled}
      moderateOpen={moderateOpen}
      restoreOpen={restoreOpen}
      isModerating={isModerating}
      isRestoring={isRestoring}
      moderateError={moderateError}
      restoreError={restoreError}
      toastMessage={toastMessage}
      onDismissToast={() => setToastMessage("")}
      onModerateOpen={() => setModerateOpen(true)}
      onRestoreOpen={() => setRestoreOpen(true)}
      onModerateClose={() => {
        if (isModerating) return;
        setModerateOpen(false);
        clearModerateError();
      }}
      onRestoreClose={() => {
        if (isRestoring) return;
        setRestoreOpen(false);
        clearRestoreError();
      }}
      onSubmitModerate={async ({ action, reason }) => {
        if (!commentId) return;
        await submitModerate(commentId, { action, reason });
      }}
      onSubmitRestore={async ({ reason }) => {
        if (!commentId) return;
        await submitRestore(commentId, { reason });
      }}
      ToastComponent={FeedToast}
    />
  );
}
