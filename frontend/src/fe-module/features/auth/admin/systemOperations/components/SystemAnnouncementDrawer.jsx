import { useEffect, useState } from "react";
import { ANNOUNCEMENT_VIEW_MODES } from "../constants/announcementListConstants.js";
import { buildPublishPayload, mapApiFieldErrors } from "../utils/announcementDisplayUtils.js";
import { AnnouncementActionConfirmDialog } from "./AnnouncementActionConfirmDialog.jsx";
import { SystemAnnouncementDrawerView } from "./SystemAnnouncementDrawerView.jsx";

const defaultForm = {
  title: "",
  content: "",
  severity: "INFO",
  pinned: false,
  dismissible: true,
};

export function SystemAnnouncementDrawer({
  announcement,
  loading,
  announcementView,
  canCreate,
  canUpdate,
  canPublish,
  canCancel,
  pending,
  onClose,
  onViewChange,
  onSave,
  onPublish,
  onPin,
  onUnpin,
  onCancelAnnouncement,
}) {
  const [form, setForm] = useState(defaultForm);
  const [fieldErrors, setFieldErrors] = useState({});
  const [publishActive, setPublishActive] = useState(false);
  const [audienceMode, setAudienceMode] = useState("ALL_ACTIVE_USERS");
  const [recipientUserIdsRaw, setRecipientUserIdsRaw] = useState("");
  const [publishError, setPublishError] = useState("");
  const [confirmRequest, setConfirmRequest] = useState(null);

  useEffect(() => {
    if (!announcement) return;
    setForm({
      title: announcement.title || "",
      content: announcement.content || "",
      severity: announcement.severity || "INFO",
      pinned: Boolean(announcement.pinned),
      dismissible: announcement.dismissible !== false,
    });
    setFieldErrors({});
    setPublishActive(false);
    setPublishError("");
  }, [announcement]);

  const handleSave = async (event) => {
    event.preventDefault();
    if (!canUpdate || announcement?.status !== "DRAFT") return;
    setFieldErrors({});
    try {
      await onSave?.(form);
    } catch (error) {
      setFieldErrors(mapApiFieldErrors(error?.errors));
      throw error;
    }
  };

  const handlePublish = async () => {
    setPublishError("");
    try {
      const payload = buildPublishPayload({ audienceMode, recipientUserIdsRaw });
      await onPublish?.(payload);
      setPublishActive(false);
    } catch (error) {
      setPublishError(error?.message || "Không thể publish thông báo.");
      setFieldErrors(mapApiFieldErrors(error?.errors));
      throw error;
    }
  };

  const publishWizard = publishActive
    ? {
        active: true,
        props: {
          announcement,
          audienceMode,
          recipientUserIdsRaw,
          onAudienceModeChange: setAudienceMode,
          onRecipientUserIdsChange: setRecipientUserIdsRaw,
          onPublish: handlePublish,
          onCancel: () => setPublishActive(false),
          pending,
          fieldError: publishError || fieldErrors.recipient_user_ids,
        },
      }
    : { active: false };

  return (
    <>
      <SystemAnnouncementDrawerView
        open={Boolean(announcement)}
        announcement={announcement}
        loading={loading}
        announcementView={announcementView || ANNOUNCEMENT_VIEW_MODES.DETAIL}
        form={form}
        fieldErrors={fieldErrors}
        canCreate={canCreate}
        canUpdate={canUpdate}
        canPublish={canPublish}
        canCancel={canCancel}
        pending={pending}
        publishWizard={publishWizard}
        onClose={onClose}
        onViewChange={onViewChange}
        onFieldChange={(patch) => setForm((prev) => ({ ...prev, ...patch }))}
        onSave={handleSave}
        onStartPublish={() => {
          setPublishActive(true);
          onViewChange?.(ANNOUNCEMENT_VIEW_MODES.ACTIONS);
        }}
        onPin={() => setConfirmRequest({ type: "pin", item: announcement })}
        onUnpin={() => setConfirmRequest({ type: "unpin", item: announcement })}
        onCancelAnnouncement={() => setConfirmRequest({ type: "cancel", item: announcement })}
      />

      <AnnouncementActionConfirmDialog
        request={confirmRequest}
        pending={pending}
        onClose={() => setConfirmRequest(null)}
        onConfirm={async (request) => {
          if (request.type === "pin") await onPin?.();
          if (request.type === "unpin") await onUnpin?.();
          if (request.type === "cancel") await onCancelAnnouncement?.();
          setConfirmRequest(null);
        }}
      />
    </>
  );
}
