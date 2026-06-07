import { useEffect, useState } from "react";
import { revokeAdminSession } from "../../api/userInvestigationApi.js";
import { InvestigationModalShell } from "./InvestigationModalShell.jsx";

export function RevokeAdminSessionModal({
  open,
  session,
  userLabel,
  onClose,
  onSuccess,
  onError,
}) {
  const [revokeAllSessions, setRevokeAllSessions] = useState(false);
  const [submitError, setSubmitError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (!open) return;
    setRevokeAllSessions(false);
    setSubmitError("");
    setIsSubmitting(false);
  }, [open, session?.session_id]);

  if (!session) return null;

  const onSubmit = async () => {
    setIsSubmitting(true);
    setSubmitError("");

    try {
      const result = await revokeAdminSession(session.session_id, {
        revoke_all_sessions: revokeAllSessions,
      });
      onSuccess?.(result);
      onClose?.();
    } catch (error) {
      const message = error?.message || "Không thể thu hồi phiên admin.";
      setSubmitError(message);
      onError?.(error);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <InvestigationModalShell
      open={open}
      title="Thu hồi phiên admin"
      subtitle="Force sign-out phien dang nhap admin portal."
      titleIcon={<span aria-hidden>!</span>}
      onClose={onClose}
      maxWidthClass="max-w-md"
      footer={
        <>
          <button
            type="button"
            onClick={onClose}
            disabled={isSubmitting}
            className="rounded-lg border border-outline-variant px-4 py-2 text-sm font-medium text-on-surface-variant hover:bg-surface-container-low disabled:opacity-50"
          >
            Huy
          </button>
          <button
            type="button"
            onClick={onSubmit}
            disabled={isSubmitting}
            className="rounded-lg bg-error px-4 py-2 text-sm font-semibold text-on-error hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {isSubmitting ? "Đang xử lý..." : "Xác nhận thu hồi"}
          </button>
        </>
      }
    >
      <div className="space-y-4">
        <div className="rounded-lg border border-outline-variant bg-surface-container-low p-4 text-sm">
          <p className="font-medium text-on-surface">{userLabel || "Admin đang điều tra"}</p>
          <p className="mt-1 break-all text-on-surface-variant">
            Session: {session.session_id || session.id}
          </p>
          <p className="mt-1 text-on-surface-variant">IP: {session.ip_address || "—"}</p>
        </div>

        <label className="flex items-start gap-3 rounded-lg border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">
          <input
            type="checkbox"
            checked={revokeAllSessions}
            onChange={(e) => setRevokeAllSessions(e.target.checked)}
            className="mt-0.5"
          />
          <span>
            Thu hoi <strong>tất cả</strong> phien admin đang hoạt động của người dùng này
          </span>
        </label>

        {submitError ? <p className="text-sm text-error">{submitError}</p> : null}
      </div>
    </InvestigationModalShell>
  );
}
