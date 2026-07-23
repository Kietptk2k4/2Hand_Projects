import { useCallback, useMemo, useState } from "react";
import { FeedToast } from "../../social/components/FeedToast";
import { useContentModerationPermissions } from "../../auth/admin/contentModeration/hooks/useContentModerationPermissions.js";
import { buildModerateReviewSuccessToast } from "../constants/adminReviewModerationConstants";
import { useModerateReview } from "../hooks/useModerateReview";
import { useReviewModerationDetail } from "../hooks/useReviewModerationDetail.js";
import { AdminReviewModerateDialog } from "./AdminReviewModerateDialog";
import { ReviewModerationDrawerView } from "./ReviewModerationDrawerView.jsx";

export function ReviewModerationDrawer({ reviewId, review, onClose, onRefresh }) {
  const { canHideReview, canRemoveReview, canRestoreReview } = useContentModerationPermissions();
  const { detail, status: detailStatus, errorMessage: detailErrorMessage } =
    useReviewModerationDetail(reviewId);
  const [toastMessage, setToastMessage] = useState("");
  const [moderateOpen, setModerateOpen] = useState(false);
  const [moderateAction, setModerateAction] = useState("");
  const [historyRefreshToken, setHistoryRefreshToken] = useState(0);

  const bumpHistoryRefresh = useCallback(() => {
    setHistoryRefreshToken((value) => value + 1);
  }, []);

  const handleModerateSuccess = useCallback(
    (result, action) => {
      setModerateOpen(false);
      setToastMessage(buildModerateReviewSuccessToast(action, result));
      bumpHistoryRefresh();
      onRefresh?.();
    },
    [bumpHistoryRefresh, onRefresh],
  );

  const { isSubmitting, submitError, submit, clearError } = useModerateReview({
    onSuccess: handleModerateSuccess,
  });

  const previewReview = useMemo(() => {
    if (detail) return detail;
    if (!review) return null;
    return review;
  }, [detail, review]);

  const openModerate = useCallback((action) => {
    setModerateAction(action);
    setModerateOpen(true);
  }, []);

  if (!reviewId) return null;

  return (
    <>
      <ReviewModerationDrawerView
        reviewId={reviewId}
        review={previewReview}
        detailStatus={detailStatus}
        detailErrorMessage={detailErrorMessage}
        canHideReview={canHideReview}
        canRemoveReview={canRemoveReview}
        canRestoreReview={canRestoreReview}
        historyRefreshToken={historyRefreshToken}
        disabled={isSubmitting}
        onClose={onClose}
        onHide={() => openModerate("HIDE")}
        onRemove={() => openModerate("REMOVE")}
        onRestore={() => openModerate("RESTORE")}
      />

      <AdminReviewModerateDialog
        open={moderateOpen}
        review={previewReview}
        initialAction={moderateAction}
        isSubmitting={isSubmitting}
        submitError={submitError}
        onClose={() => {
          if (isSubmitting) return;
          setModerateOpen(false);
          clearError();
        }}
        onSubmit={async ({ action, reason, note }) => {
          if (!reviewId) return;
          await submit(reviewId, { action, reason, note });
        }}
      />

      <FeedToast message={toastMessage} onDismiss={() => setToastMessage("")} />
    </>
  );
}
