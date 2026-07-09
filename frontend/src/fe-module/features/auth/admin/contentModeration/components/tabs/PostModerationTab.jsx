import { useCallback, useState } from "react";
import { FeedToast } from "../../../../../social/components/FeedToast.jsx";
import {
  buildModerateSuccessToast,
  buildRestoreSuccessToast,
} from "../../constants/socialModerationConstants.js";
import { useContentModerationPermissions } from "../../hooks/useContentModerationPermissions.js";
import { useModeratePost } from "../../hooks/useModeratePost.js";
import { useRestorePost } from "../../hooks/useRestorePost.js";
import { PostModerationForbiddenView, PostModerationTabView } from "../PostModerationTabView.jsx";

const TARGET_LABEL = "bài viết";

export function PostModerationTab({ postId }) {
  const { canModeratePost, canRestorePost } = useContentModerationPermissions();
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
  } = useModeratePost({ onSuccess: handleModerateSuccess });

  const {
    isSubmitting: isRestoring,
    submitError: restoreError,
    submit: submitRestore,
    clearError: clearRestoreError,
  } = useRestorePost({ onSuccess: handleRestoreSuccess });

  const disabled = isModerating || isRestoring;

  if (!canModeratePost && !canRestorePost) {
    return <PostModerationForbiddenView />;
  }

  return (
    <PostModerationTabView
      postId={postId}
      canModeratePost={canModeratePost}
      canRestorePost={canRestorePost}
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
        if (!postId) return;
        await submitModerate(postId, { action, reason });
      }}
      onSubmitRestore={async ({ reason }) => {
        if (!postId) return;
        await submitRestore(postId, { reason });
      }}
      ToastComponent={FeedToast}
    />
  );
}
