import { useCallback, useState } from "react";
import {
  cancelSystemAnnouncement,
  createSystemAnnouncement,
  pinSystemAnnouncement,
  publishSystemAnnouncement,
} from "../api/systemAnnouncementApi.js";
import {
  toCreateSystemAnnouncementPayload,
  toPinSystemAnnouncementPayload,
} from "../utils/systemAnnouncementMapper.js";

export function useSystemAnnouncementMutations({ onSuccess, onError }) {
  const [pending, setPending] = useState(false);

  const run = useCallback(
    async (action) => {
      setPending(true);
      try {
        const data = await action();
        onSuccess?.(data);
        return data;
      } catch (error) {
        onError?.(error);
        throw error;
      } finally {
        setPending(false);
      }
    },
    [onError, onSuccess],
  );

  const createAnnouncement = useCallback(
    (form) => run(() => createSystemAnnouncement(toCreateSystemAnnouncementPayload(form))),
    [run],
  );

  const publishAnnouncement = useCallback(
    (announcementId, body) => run(() => publishSystemAnnouncement(announcementId, body || {})),
    [run],
  );

  const pinAnnouncement = useCallback(
    (announcementId, pinned) =>
      run(() => pinSystemAnnouncement(announcementId, toPinSystemAnnouncementPayload(pinned))),
    [run],
  );

  const cancelAnnouncement = useCallback(
    (announcementId) => run(() => cancelSystemAnnouncement(announcementId)),
    [run],
  );

  return { pending, createAnnouncement, publishAnnouncement, pinAnnouncement, cancelAnnouncement };
}