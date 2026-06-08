import { useCallback, useState } from "react";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { FeedToast } from "../../social/components/FeedToast";
import { ANNOUNCEMENT_SEVERITY_STYLES } from "../constants/notificationConstants";
import { useNotificationBadge } from "../context/NotificationBadgeContext";
import { dismissAnnouncementNotification } from "../api/notificationApi";
import { usePinnedAnnouncements } from "../hooks/usePinnedAnnouncements";

export function SystemAnnouncementBanner() {
  const { isAuthenticated } = useAuthSession();
  const { announcements, dismissLocal } = usePinnedAnnouncements({ enabled: isAuthenticated });
  const { refetch: refetchBadge } = useNotificationBadge();
  const [toastMessage, setToastMessage] = useState("");
  const [dismissingId, setDismissingId] = useState(null);

  const handleDismiss = useCallback(
    async (announcement) => {
      if (!announcement?.isDismissibleAnnouncement) return;

      setDismissingId(announcement.id);
      try {
        await dismissAnnouncementNotification(announcement.id);
        dismissLocal(announcement.id);
        await refetchBadge();
      } catch (error) {
        setToastMessage(error?.message || "Khong the an thong bao.");
      } finally {
        setDismissingId(null);
      }
    },
    [dismissLocal, refetchBadge]
  );

  if (!isAuthenticated || announcements.length === 0) {
    return null;
  }

  return (
    <>
      <div className="border-b border-outline-variant bg-surface-container-lowest">
        <div className="mx-auto flex w-full max-w-[1280px] flex-col gap-2 px-4 py-3 md:px-8">
          {announcements.map((announcement) => {
            const severityClass =
              ANNOUNCEMENT_SEVERITY_STYLES[announcement.severity] ||
              ANNOUNCEMENT_SEVERITY_STYLES.INFO;

            return (
              <div
                key={announcement.id}
                className={["flex items-start justify-between gap-3 rounded-xl border px-4 py-3", severityClass].join(
                  " "
                )}
                role="status"
              >
                <div className="min-w-0">
                  <p className="text-sm font-semibold">{announcement.title}</p>
                  <p className="mt-1 text-sm opacity-90">{announcement.content}</p>
                </div>

                {announcement.isDismissibleAnnouncement ? (
                  <button
                    type="button"
                    onClick={() => handleDismiss(announcement)}
                    disabled={dismissingId === announcement.id}
                    className="shrink-0 rounded-lg px-2 py-1 text-xs font-medium hover:bg-black/5 disabled:opacity-60"
                  >
                    An
                  </button>
                ) : null}
              </div>
            );
          })}
        </div>
      </div>

      <FeedToast message={toastMessage} onDismiss={() => setToastMessage("")} />
    </>
  );
}
