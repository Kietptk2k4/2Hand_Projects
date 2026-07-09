import { useEffect, useState } from "react";
import { revokeAdminSession } from "../../api/userInvestigationApi.js";
import { AdminFilterButton } from "../../../components/ui";
import { InvestigationModalShell } from "./InvestigationModalShell.jsx";
import { RevokeAdminSessionModalView } from "./RevokeAdminSessionModalView.jsx";

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
      subtitle="Force sign-out phiên đăng nhập admin portal."
      titleIcon={<span aria-hidden>!</span>}
      onClose={onClose}
      maxWidthClass="max-w-md"
      footer={
        <>
          <AdminFilterButton type="button" variant="secondary" disabled={isSubmitting} onClick={onClose}>
            Hủy
          </AdminFilterButton>
          <AdminFilterButton
            type="button"
            variant="primary"
            disabled={isSubmitting}
            className="bg-admin-danger hover:bg-admin-danger/90"
            onClick={onSubmit}
          >
            {isSubmitting ? "Đang xử lý…" : "Xác nhận thu hồi"}
          </AdminFilterButton>
        </>
      }
    >
      <RevokeAdminSessionModalView
        session={session}
        userLabel={userLabel}
        revokeAllSessions={revokeAllSessions}
        submitError={submitError}
        onRevokeAllChange={setRevokeAllSessions}
      />
    </InvestigationModalShell>
  );
}
