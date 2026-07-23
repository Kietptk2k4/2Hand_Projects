import { AdminFilterButton, AdminFilterField } from "../../components/ui";
import { ANNOUNCEMENT_PUBLISH_AUDIENCE_OPTIONS } from "../constants/announcementListConstants.js";
import { parseRecipientUserIds } from "../utils/announcementDisplayUtils.js";
import { AnnouncementBannerPreview } from "./AnnouncementBannerPreview.jsx";

export function AnnouncementPublishWizard({
  announcement,
  audienceMode,
  recipientUserIdsRaw,
  onAudienceModeChange,
  onRecipientUserIdsChange,
  onPublish,
  onCancel,
  pending,
  fieldError,
}) {
  const { invalid } = parseRecipientUserIds(recipientUserIdsRaw);
  const canPublish =
    audienceMode !== "RECIPIENT_LIST" || (recipientUserIdsRaw?.trim() && invalid.length === 0);

  return (
    <div className="space-y-4">
      <AnnouncementBannerPreview
        title={announcement?.title}
        content={announcement?.content}
        severity={announcement?.severity}
        dismissible={announcement?.dismissible}
        pinned={announcement?.pinned}
      />

      <fieldset className="space-y-2">
        <legend className="text-sm font-medium text-admin-text">Đối tượng nhận</legend>
        {ANNOUNCEMENT_PUBLISH_AUDIENCE_OPTIONS.map((option) => (
          <label key={option.id} className="flex min-h-10 items-start gap-2 text-sm text-admin-text">
            <input
              type="radio"
              name="publish-audience"
              checked={audienceMode === option.id}
              onChange={() => onAudienceModeChange?.(option.id)}
              className="mt-1"
            />
            <span>{option.label}</span>
          </label>
        ))}
      </fieldset>

      {audienceMode === "RECIPIENT_LIST" ? (
        <AdminFilterField label="Danh sách user ID" htmlFor="publish-recipients">
          <textarea
            id="publish-recipients"
            rows={3}
            value={recipientUserIdsRaw}
            onChange={(event) => onRecipientUserIdsChange?.(event.target.value)}
            placeholder="Mỗi UUID cách nhau bởi dấu phẩy hoặc xuống dòng"
            className="w-full rounded-lg border border-admin-border bg-admin-surface px-3 py-2 font-mono text-xs text-admin-text outline-none focus:border-admin-accent-border focus:ring-2 focus:ring-admin-accent-soft"
          />
          {invalid.length ? (
            <p className="mt-1 text-xs text-admin-danger">
              UUID không hợp lệ: {invalid.slice(0, 3).join(", ")}
              {invalid.length > 3 ? "…" : ""}
            </p>
          ) : null}
        </AdminFilterField>
      ) : null}

      {audienceMode === "DEV_FALLBACK" ? (
        <p className="rounded-lg border border-admin-border bg-admin-surface-muted px-3 py-2 text-xs text-admin-text-secondary">
          Server sẽ dùng `admin.announcements.dev-recipient-user-ids` khi body publish để trống.
        </p>
      ) : null}

      {fieldError ? <p className="text-xs text-admin-danger">{fieldError}</p> : null}

      <div className="flex flex-wrap gap-2">
        <AdminFilterButton
          type="button"
          variant="primary"
          className="min-h-11"
          disabled={pending || !canPublish}
          onClick={onPublish}
        >
          Xác nhận publish
        </AdminFilterButton>
        <AdminFilterButton type="button" variant="secondary" className="min-h-11" disabled={pending} onClick={onCancel}>
          Hủy
        </AdminFilterButton>
      </div>
    </div>
  );
}
