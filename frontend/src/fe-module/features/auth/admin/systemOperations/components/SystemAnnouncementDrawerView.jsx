import { Link } from "react-router-dom";
import { buildAdminSearchParams } from "../../adminUrlParams.js";
import { useAuditAdminSummaries } from "../../adminAudit/hooks/useAuditAdminSummaries.js";
import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import {
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
  AdminSurfaceCard,
} from "../../components/ui";
import { ANNOUNCEMENT_SEVERITIES } from "../constants/systemAnnouncementConstants.js";
import {
  ANNOUNCEMENT_SEVERITY_LABELS,
  ANNOUNCEMENT_VIEW_MODES,
} from "../constants/announcementListConstants.js";
import { GENERIC_SAVE } from "../constants/systemOperationsUiStrings.js";
import { mapApiFieldErrors } from "../utils/announcementDisplayUtils.js";
import { AnnouncementBannerPreview } from "./AnnouncementBannerPreview.jsx";
import { AnnouncementPublishWizard } from "./AnnouncementPublishWizard.jsx";
import { AnnouncementSeverityBadge, AnnouncementStatusBadge } from "./ui/SystemOperationsBadges.jsx";
import { SystemOperationsListSkeleton } from "./ui/SystemOperationsListSkeleton.jsx";

function AuditLogLink({ announcementId }) {
  if (!announcementId) return null;

  const to = `/admin?${buildAdminSearchParams({
    section: "adminAudit",
    tab: "action-logs",
    auditFilters: {
      target_type: "ANNOUNCEMENT",
      target_id: announcementId,
    },
  }).toString()}`;

  return (
    <Link
      to={to}
      className="inline-flex min-h-9 items-center gap-1 text-xs font-medium text-admin-accent hover:underline"
    >
      <span className="material-symbols-outlined text-[16px]" aria-hidden="true">
        history
      </span>
      Xem trong nhật ký audit
    </Link>
  );
}

export function SystemAnnouncementDrawerView({
  open,
  announcement,
  loading,
  announcementView,
  form,
  fieldErrors = {},
  canCreate,
  canUpdate,
  canPublish,
  canCancel,
  pending,
  publishWizard,
  onClose,
  onViewChange,
  onFieldChange,
  onSave,
  onStartPublish,
  onPin,
  onUnpin,
  onCancelAnnouncement,
}) {
  const adminSummaries = useAuditAdminSummaries(announcement?.createdBy ? [announcement.createdBy] : []);
  const createdByLabel =
    adminSummaries[announcement?.createdBy]?.displayName ||
    adminSummaries[announcement?.createdBy]?.email ||
    announcement?.createdBy ||
    "—";

  if (!open || !announcement) return null;

  const isActions = announcementView === ANNOUNCEMENT_VIEW_MODES.ACTIONS;
  const isDraft = announcement.status === "DRAFT";
  const isSent = announcement.status === "SENT";
  const isCancelled = announcement.status === "CANCELLED";

  const tabClass = (active) =>
    [
      "min-h-11 rounded-lg px-3 py-2 text-sm font-medium transition-colors",
      active
        ? "bg-admin-accent-soft text-admin-accent-strong"
        : "text-admin-text-secondary hover:bg-admin-surface-muted",
    ].join(" ");

  return (
    <div className="fixed inset-0 z-[100] flex min-h-dvh justify-end bg-admin-text/40 backdrop-blur-sm">
      <button type="button" aria-label="Đóng" className="absolute inset-0" onClick={onClose} />
      <aside className="relative flex h-full min-h-dvh w-full max-w-xl flex-col border-l border-admin-border bg-admin-surface shadow-[var(--shadow-admin-surface)]">
        <div className="flex items-start justify-between gap-3 border-b border-admin-border bg-admin-surface-muted px-4 py-4 sm:px-6">
          <div className="min-w-0">
            <div className="mb-2 flex flex-wrap items-center gap-2">
              <AnnouncementStatusBadge status={announcement.status} />
              <AnnouncementSeverityBadge severity={announcement.severity} />
            </div>
            <h2 className="text-lg font-semibold text-admin-text">{announcement.title}</h2>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="flex min-h-11 min-w-11 shrink-0 items-center justify-center rounded-lg text-admin-text-muted hover:bg-admin-surface hover:text-admin-text"
            aria-label="Đóng"
          >
            ×
          </button>
        </div>

        <div className="flex flex-wrap items-center justify-between gap-2 border-b border-admin-border px-4 py-3 sm:px-6">
          <div className="flex flex-wrap gap-2">
            <button
              type="button"
              onClick={() => onViewChange?.(ANNOUNCEMENT_VIEW_MODES.DETAIL)}
              className={tabClass(!isActions)}
            >
              Chi tiết
            </button>
            <button
              type="button"
              onClick={() => onViewChange?.(ANNOUNCEMENT_VIEW_MODES.ACTIONS)}
              className={tabClass(isActions)}
            >
              Hành động
            </button>
          </div>
          <AuditLogLink announcementId={announcement.announcementId} />
        </div>

        <div className="flex-1 overflow-y-auto px-4 py-4 sm:px-6">
          {loading ? <SystemOperationsListSkeleton rows={4} /> : null}

          {!loading && !isActions ? (
            <div className="space-y-4">
              <AnnouncementBannerPreview
                title={isDraft ? form.title : announcement.title}
                content={isDraft ? form.content : announcement.content}
                severity={isDraft ? form.severity : announcement.severity}
                dismissible={isDraft ? form.dismissible : announcement.dismissible}
                pinned={isDraft ? form.pinned : announcement.pinned}
              />

              {isDraft && canUpdate ? (
                <form onSubmit={onSave} className="space-y-4">
                  <AdminFilterField label="Tiêu đề" htmlFor="edit-announcement-title">
                    <AdminFilterInput
                      id="edit-announcement-title"
                      required
                      value={form.title}
                      onChange={(event) => onFieldChange({ title: event.target.value })}
                    />
                    {fieldErrors.title ? (
                      <p className="mt-1 text-xs text-admin-danger">{fieldErrors.title}</p>
                    ) : null}
                  </AdminFilterField>
                  <AdminFilterField label="Nội dung" htmlFor="edit-announcement-content">
                    <textarea
                      id="edit-announcement-content"
                      required
                      rows={6}
                      value={form.content}
                      onChange={(event) => onFieldChange({ content: event.target.value })}
                      className="w-full rounded-lg border border-admin-border bg-admin-surface px-3 py-2 text-base text-admin-text outline-none focus:border-admin-accent-border focus:ring-2 focus:ring-admin-accent-soft"
                    />
                    {fieldErrors.content ? (
                      <p className="mt-1 text-xs text-admin-danger">{fieldErrors.content}</p>
                    ) : null}
                  </AdminFilterField>
                  <AdminFilterField label="Mức độ" htmlFor="edit-announcement-severity">
                    <AdminFilterSelect
                      id="edit-announcement-severity"
                      value={form.severity}
                      onChange={(event) => onFieldChange({ severity: event.target.value })}
                    >
                      {ANNOUNCEMENT_SEVERITIES.map((severity) => (
                        <option key={severity} value={severity}>
                          {ANNOUNCEMENT_SEVERITY_LABELS[severity] || severity}
                        </option>
                      ))}
                    </AdminFilterSelect>
                  </AdminFilterField>
                  <label className="flex min-h-11 items-center gap-2 text-sm text-admin-text">
                    <input
                      type="checkbox"
                      checked={form.pinned}
                      onChange={(event) => onFieldChange({ pinned: event.target.checked })}
                    />
                    Pin sau khi publish
                  </label>
                  <label className="flex min-h-11 items-center gap-2 text-sm text-admin-text">
                    <input
                      type="checkbox"
                      checked={form.dismissible}
                      onChange={(event) => onFieldChange({ dismissible: event.target.checked })}
                    />
                    Cho phép người dùng dismiss
                  </label>
                  <AdminFilterButton type="submit" variant="primary" className="min-h-11" disabled={pending}>
                    {GENERIC_SAVE}
                  </AdminFilterButton>
                </form>
              ) : (
                <AdminSurfaceCard padding="md" className="space-y-2 text-sm text-admin-text-secondary">
                  <p>
                    <span className="text-admin-text-muted">Tạo lúc:</span>{" "}
                    {formatDateTime(announcement.createdAt)}
                  </p>
                  <p>
                    <span className="text-admin-text-muted">Gửi lúc:</span>{" "}
                    {announcement.sentAt ? formatDateTime(announcement.sentAt) : "—"}
                  </p>
                  <p>
                    <span className="text-admin-text-muted">Tạo bởi:</span> {createdByLabel}
                  </p>
                  <p>
                    <span className="text-admin-text-muted">Dismissible:</span>{" "}
                    {announcement.dismissible ? "Có" : "Không"}
                  </p>
                </AdminSurfaceCard>
              )}
            </div>
          ) : null}

          {!loading && isActions ? (
            <div className="space-y-4">
              {publishWizard?.active ? (
                <AnnouncementPublishWizard {...publishWizard.props} />
              ) : (
                <>
                  <AnnouncementBannerPreview
                    title={announcement.title}
                    content={announcement.content}
                    severity={announcement.severity}
                    dismissible={announcement.dismissible}
                    pinned={announcement.pinned}
                  />
                  <div className="flex flex-wrap gap-2">
                    {isDraft && canPublish ? (
                      <AdminFilterButton
                        type="button"
                        variant="primary"
                        className="min-h-11"
                        disabled={pending}
                        onClick={onStartPublish}
                      >
                        Publish
                      </AdminFilterButton>
                    ) : null}
                    {isSent && canUpdate ? (
                      <AdminFilterButton
                        type="button"
                        variant="secondary"
                        className="min-h-11"
                        disabled={pending}
                        onClick={() => (announcement.pinned ? onUnpin?.() : onPin?.())}
                      >
                        {announcement.pinned ? "Bỏ pin" : "Pin"}
                      </AdminFilterButton>
                    ) : null}
                    {!isCancelled && canCancel ? (
                      <AdminFilterButton
                        type="button"
                        variant="secondary"
                        className="min-h-11 border-admin-danger/30 text-admin-danger hover:bg-admin-danger-soft"
                        disabled={pending}
                        onClick={onCancelAnnouncement}
                      >
                        Hủy thông báo
                      </AdminFilterButton>
                    ) : null}
                    {!canPublish && !canUpdate && !canCancel && !canCreate ? (
                      <p className="text-sm text-admin-text-muted">Bạn chỉ có quyền xem thông báo.</p>
                    ) : null}
                  </div>
                </>
              )}
            </div>
          ) : null}
        </div>
      </aside>
    </div>
  );
}
