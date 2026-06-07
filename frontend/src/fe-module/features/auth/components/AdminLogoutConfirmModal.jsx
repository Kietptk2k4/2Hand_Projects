import { LogoutIcon } from "./AdminAuthIcons.jsx";

export function AdminLogoutConfirmModal({ open, isLoggingOut, onCancel, onConfirm }) {
  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-[100] flex items-center justify-center bg-inverse-surface/40 p-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-labelledby="admin-logout-title"
      onClick={(event) => {
        if (event.target === event.currentTarget && !isLoggingOut) {
          onCancel();
        }
      }}
    >
      <div className="w-full max-w-md overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest shadow-[0px_10px_15px_-3px_rgba(0,0,0,0.1)]">
        <div className="flex flex-col items-center px-6 pb-2 pt-6 text-center">
          <div className="mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-error-container">
            <LogoutIcon className="h-6 w-6 text-error" />
          </div>
          <h2 id="admin-logout-title" className="text-xl font-semibold text-on-background">
            Xác nhận Đăng xuất
          </h2>
        </div>

        <div className="px-6 py-2 text-center">
          <p className="text-base text-on-surface-variant">
            Bạn có chắc chắn muốn kết thúc phiên làm việc này?
          </p>
          <p className="mt-1 text-sm text-tertiary">
            Refresh token sẽ bị thu hồi theo quy định bảo mật.
          </p>
        </div>

        <div className="mt-4 flex items-center gap-3 border-t border-outline-variant/50 bg-surface-container-low/50 px-6 py-4">
          <button
            type="button"
            disabled={isLoggingOut}
            onClick={onCancel}
            className="h-10 flex-1 rounded-lg border border-outline-variant bg-surface-container-lowest px-4 text-sm font-medium text-on-surface-variant transition-colors hover:bg-surface-container-low disabled:cursor-not-allowed disabled:opacity-60"
          >
            Hủy
          </button>
          <button
            type="button"
            disabled={isLoggingOut}
            onClick={onConfirm}
            className="h-10 flex-1 rounded-lg bg-error px-4 text-sm font-medium text-on-error shadow-[0px_4px_6px_-1px_rgba(0,0,0,0.1)] transition-colors hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {isLoggingOut ? "Đang đăng xuất..." : "Đăng xuất"}
          </button>
        </div>
      </div>
    </div>
  );
}
