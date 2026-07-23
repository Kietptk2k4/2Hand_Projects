import { ANNOUNCEMENT_SEVERITY_LABELS } from "../constants/announcementListConstants.js";

const SEVERITY_STYLES = {
  INFO: "border-admin-accent/30 bg-admin-accent-soft/40 text-admin-text",
  WARNING: "border-admin-warning/40 bg-admin-warning-soft text-admin-text",
  CRITICAL: "border-admin-danger/40 bg-admin-danger-soft/30 text-admin-text",
};

export function AnnouncementBannerPreview({ title, content, severity, dismissible = true, pinned = false }) {
  const style = SEVERITY_STYLES[severity] || SEVERITY_STYLES.INFO;
  const severityLabel = ANNOUNCEMENT_SEVERITY_LABELS[severity] || severity;

  return (
    <div className={`rounded-xl border px-4 py-3 ${style}`}>
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-center gap-2">
            <span className="text-[11px] font-semibold tracking-wide uppercase text-admin-text-muted">
              {severityLabel}
            </span>
            {pinned ? (
              <span className="inline-flex items-center gap-1 text-[11px] text-admin-accent">
                <span className="material-symbols-outlined text-[14px]" aria-hidden="true">
                  push_pin
                </span>
                Pin
              </span>
            ) : null}
          </div>
          <p className="mt-1 text-sm font-semibold text-admin-text">{title || "Tiêu đề thông báo"}</p>
          <p className="mt-2 whitespace-pre-wrap text-sm text-admin-text-secondary">
            {content || "Nội dung thông báo sẽ hiển thị tại đây."}
          </p>
        </div>
        {dismissible ? (
          <button
            type="button"
            disabled
            className="flex min-h-8 min-w-8 items-center justify-center rounded-lg text-admin-text-muted"
            aria-label="Đóng (preview)"
          >
            ×
          </button>
        ) : null}
      </div>
      <p className="mt-3 text-[11px] text-admin-text-muted">Xem trước banner in-app</p>
    </div>
  );
}
