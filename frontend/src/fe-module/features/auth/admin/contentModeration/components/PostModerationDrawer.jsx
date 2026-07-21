import { useCallback, useState } from "react";
import { FeedToast } from "../../../../social/components/FeedToast.jsx";
import {
  buildModerateSuccessToast,
  buildRestoreSuccessToast,
} from "../constants/socialModerationConstants.js";
import { useContentModerationPermissions } from "../hooks/useContentModerationPermissions.js";
import { useModeratePost } from "../hooks/useModeratePost.js";
import { usePostModerationDetail } from "../hooks/usePostModerationDetail.js";
import { useRestorePost } from "../hooks/useRestorePost.js";
import { resolvePostAuthorSummary } from "../utils/postModerationListMapper.js";
import { SocialModerateDialog } from "./SocialModerateDialog.jsx";
import { SocialRestoreDialog } from "./SocialRestoreDialog.jsx";
import { PostModerationDrawerView } from "./PostModerationDrawerView.jsx";

const TARGET_LABEL = "bài viết";

export function PostModerationDrawer({ postId, post, onClose, onRefresh }) {
  const { canModeratePost, canRestorePost } = useContentModerationPermissions();
  const { detail, status: detailStatus, errorMessage: detailErrorMessage } = usePostModerationDetail(postId);
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
  } = useModeratePost({ onSuccess: handleModerateSuccess });

  const {
    isSubmitting: isRestoring,
    submitError: restoreError,
    submit: submitRestore,
    clearError: clearRestoreError,
  } = useRestorePost({ onSuccess: handleRestoreSuccess });

  const disabled = isModerating || isRestoring;
  const previewPost = detail || post;
  const authorSummary = detail?.author
    ? {
        userId: detail.author.userId,
        displayName: detail.author.displayName,
        avatarUrl: detail.author.avatarUrl,
      }
    : post
      ? resolvePostAuthorSummary(post)
      : null;

  if (!postId) return null;

  return (
    <>
      <PostModerationDrawerView
        postId={postId}
        post={previewPost}
        authorSummary={authorSummary}
        detailStatus={detailStatus}
        detailErrorMessage={detailErrorMessage}
        canModeratePost={canModeratePost}
        canRestorePost={canRestorePost}
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
        targetId={postId}
        isSubmitting={isModerating}
        submitError={moderateError}
        onClose={() => {
          if (isModerating) return;
          setModerateOpen(false);
          clearModerateError();
        }}
        onSubmit={async ({ action, reason }) => {
          if (!postId) return;
          await submitModerate(postId, { action, reason });
        }}
      />

      <SocialRestoreDialog
        open={restoreOpen}
        targetLabel={TARGET_LABEL}
        targetId={postId}
        isSubmitting={isRestoring}
        submitError={restoreError}
        onClose={() => {
          if (isRestoring) return;
          setRestoreOpen(false);
          clearRestoreError();
        }}
        onSubmit={async ({ reason }) => {
          if (!postId) return;
          await submitRestore(postId, { reason });
        }}
      />

      <FeedToast message={toastMessage} onDismiss={() => setToastMessage("")} />
    </>
  );
}
