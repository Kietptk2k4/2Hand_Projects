import { useCallback, useState } from "react";
import { FeedToast } from "../../../../../social/components/FeedToast.jsx";
import {
  buildModerateSuccessToast,
  buildRestoreSuccessToast,
} from "../../constants/socialModerationConstants.js";
import { useContentModerationPermissions } from "../../hooks/useContentModerationPermissions.js";
import { useModeratePost } from "../../hooks/useModeratePost.js";
import { useRestorePost } from "../../hooks/useRestorePost.js";
import { SocialModerateDialog } from "../SocialModerateDialog.jsx";
import { SocialModerationPanel } from "../SocialModerationPanel.jsx";
import { SocialRestoreDialog } from "../SocialRestoreDialog.jsx";

const TARGET_LABEL = "bai viet";

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
    return (
      <div className="rounded-xl border border-error/30 bg-error-container/20 p-8 text-center">
        <p className="text-body-md text-on-error-container">
          Ban khong co quyen POST_MODERATE hoac POST_RESTORE.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-headline-sm font-semibold text-on-surface md:text-headline-lg">Kiem duyet bai viet</h2>
        <p className="mt-1 text-body-sm text-on-surface-variant">
          Nhap postId (MongoDB ObjectId) o thanh tren, sau do moderate hoac restore qua admin-service.
        </p>
      </div>

      {!postId ? (
        <div className="rounded-xl border border-dashed border-outline-variant bg-surface-container-low/40 p-10 text-center">
          <span className="material-symbols-outlined mb-2 text-4xl text-outline">post_add</span>
          <p className="text-body-md text-on-surface-variant">Chon postId de bat dau kiem duyet.</p>
        </div>
      ) : (
        <SocialModerationPanel
          targetLabel={TARGET_LABEL}
          targetId={postId}
          canModerate={canModeratePost}
          canRestore={canRestorePost}
          lastResult={lastResult}
          disabled={disabled}
          onModerate={() => setModerateOpen(true)}
          onRestore={() => setRestoreOpen(true)}
        />
      )}

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
    </div>
  );
}