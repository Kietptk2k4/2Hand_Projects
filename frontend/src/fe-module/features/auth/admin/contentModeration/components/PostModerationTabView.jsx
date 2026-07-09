import { AdminPageHeader, AdminSurfaceCard } from "../../components/ui";
import { SocialModerateDialog } from "./SocialModerateDialog.jsx";
import { SocialModerationPanelView } from "./SocialModerationPanelView.jsx";
import { SocialRestoreDialog } from "./SocialRestoreDialog.jsx";

export function PostModerationTabView({
  postId,
  canModeratePost,
  canRestorePost,
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
        title="Kiểm duyệt bài viết"
        subtitle="Nhập postId (MongoDB ObjectId) ở thanh trên, sau đó kiểm duyệt hoặc khôi phục qua admin-service."
      />

      {!postId ? (
        <AdminSurfaceCard padding="lg">
          <div className="py-8 text-center">
            <span className="material-symbols-outlined mb-2 text-4xl text-admin-text-muted" aria-hidden="true">
              post_add
            </span>
            <p className="text-sm text-admin-text-secondary">Chọn postId để bắt đầu kiểm duyệt.</p>
          </div>
        </AdminSurfaceCard>
      ) : (
        <SocialModerationPanelView
          targetLabel="bài viết"
          targetId={postId}
          canModerate={canModeratePost}
          canRestore={canRestorePost}
          lastResult={lastResult}
          disabled={disabled}
          onModerate={onModerateOpen}
          onRestore={onRestoreOpen}
        />
      )}

      <SocialModerateDialog
        open={moderateOpen}
        targetLabel="bài viết"
        targetId={postId}
        isSubmitting={isModerating}
        submitError={moderateError}
        onClose={onModerateClose}
        onSubmit={onSubmitModerate}
      />

      <SocialRestoreDialog
        open={restoreOpen}
        targetLabel="bài viết"
        targetId={postId}
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

export function PostModerationForbiddenView() {
  return (
    <AdminSurfaceCard padding="lg" className="border-admin-danger/30 bg-admin-danger-soft/20">
      <p className="text-center text-sm text-admin-danger">
        Bạn không có quyền POST_MODERATE hoặc POST_RESTORE.
      </p>
    </AdminSurfaceCard>
  );
}
