import { AdminPageHeader, AdminSurfaceCard } from "../../components/ui";
import { SocialModerateDialog } from "./SocialModerateDialog.jsx";
import { SocialModerationPanelView } from "./SocialModerationPanelView.jsx";
import { SocialRestoreDialog } from "./SocialRestoreDialog.jsx";

export function CommentModerationTabView({
  commentId,
  canModerateComment,
  canRestoreComment,
  lastResult,
  disabled,
  moderateOpen,
  restoreOpen,
  isModerating,
  isRestoring,
  moderateError,
  restoreError,
  toastMessage,
  onDismissToast,
  onModerateOpen,
  onRestoreOpen,
  onModerateClose,
  onRestoreClose,
  onSubmitModerate,
  onSubmitRestore,
  ToastComponent,
}) {
  return (
    <div className="max-w-full min-w-0 space-y-6">
      <AdminPageHeader
        eyebrow="Kiểm duyệt nội dung"
        title="Kiểm duyệt bình luận"
        subtitle="Nhập commentId (MongoDB ObjectId) ở thanh trên, sau đó kiểm duyệt hoặc khôi phục qua admin-service."
      />

      {!commentId ? (
        <AdminSurfaceCard padding="lg">
          <div className="py-8 text-center">
            <span className="material-symbols-outlined mb-2 text-4xl text-admin-text-muted" aria-hidden="true">
              chat
            </span>
            <p className="text-sm text-admin-text-secondary">Chọn commentId để bắt đầu kiểm duyệt.</p>
          </div>
        </AdminSurfaceCard>
      ) : (
        <SocialModerationPanelView
          targetLabel="bình luận"
          targetId={commentId}
          canModerate={canModerateComment}
          canRestore={canRestoreComment}
          lastResult={lastResult}
          disabled={disabled}
          onModerate={onModerateOpen}
          onRestore={onRestoreOpen}
        />
      )}

      <SocialModerateDialog
        open={moderateOpen}
        targetLabel="bình luận"
        targetId={commentId}
        isSubmitting={isModerating}
        submitError={moderateError}
        onClose={onModerateClose}
        onSubmit={onSubmitModerate}
      />

      <SocialRestoreDialog
        open={restoreOpen}
        targetLabel="bình luận"
        targetId={commentId}
        isSubmitting={isRestoring}
        submitError={restoreError}
        onClose={onRestoreClose}
        onSubmit={onSubmitRestore}
      />

      {ToastComponent ? (
        <ToastComponent message={toastMessage} onDismiss={onDismissToast} />
      ) : null}
    </div>
  );
}

export function CommentModerationForbiddenView() {
  return (
    <AdminSurfaceCard padding="lg" className="border-admin-danger/30 bg-admin-danger-soft/20">
      <p className="text-center text-sm text-admin-danger">
        Bạn không có quyền COMMENT_MODERATE hoặc COMMENT_RESTORE.
      </p>
    </AdminSurfaceCard>
  );
}
