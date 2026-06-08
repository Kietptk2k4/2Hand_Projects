import { useCallback, useState } from "react";
import { FeedToast } from "../../../../../social/components/FeedToast.jsx";
import {
  buildModerateSuccessToast,
  buildRestoreSuccessToast,
} from "../../constants/socialModerationConstants.js";
import { useContentModerationPermissions } from "../../hooks/useContentModerationPermissions.js";
import { useModerateComment } from "../../hooks/useModerateComment.js";
import { useRestoreComment } from "../../hooks/useRestoreComment.js";
import { SocialModerateDialog } from "../SocialModerateDialog.jsx";
import { SocialModerationPanel } from "../SocialModerationPanel.jsx";
import { SocialRestoreDialog } from "../SocialRestoreDialog.jsx";

const TARGET_LABEL = "binh luan";

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
    return (
      <div className="rounded-xl border border-error/30 bg-error-container/20 p-8 text-center">
        <p className="text-body-md text-on-error-container">
          Ban khong co quyen COMMENT_MODERATE hoac COMMENT_RESTORE.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-headline-sm font-semibold text-on-surface md:text-headline-lg">Kiem duyet binh luan</h2>
        <p className="mt-1 text-body-sm text-on-surface-variant">
          Nhap commentId (MongoDB ObjectId) o thanh tren, sau do moderate hoac restore qua admin-service.
        </p>
      </div>

      {!commentId ? (
        <div className="rounded-xl border border-dashed border-outline-variant bg-surface-container-low/40 p-10 text-center">
          <span className="material-symbols-outlined mb-2 text-4xl text-outline">chat</span>
          <p className="text-body-md text-on-surface-variant">Chon commentId de bat dau kiem duyet.</p>
        </div>
      ) : (
        <SocialModerationPanel
          targetLabel={TARGET_LABEL}
          targetId={commentId}
          canModerate={canModerateComment}
          canRestore={canRestoreComment}
          lastResult={lastResult}
          disabled={disabled}
          onModerate={() => setModerateOpen(true)}
          onRestore={() => setRestoreOpen(true)}
        />
      )}

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
    </div>
  );
}