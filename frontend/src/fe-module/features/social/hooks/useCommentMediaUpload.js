import { useCallback, useRef, useState } from "react";
import { requestPostMediaUploadUrl, uploadPostMediaFile } from "../api/createPostApi";
import { MAX_COMMENT_MEDIA_ITEMS } from "../constants/commentConstants";
import {
  IMAGE_MAX_BYTES,
  POST_MEDIA_MIME_TYPES,
  VIDEO_MAX_BYTES,
} from "../constants/createPostConstants";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function resolveMediaKind(file) {
  return file.type.startsWith("video/") ? "VIDEO" : "IMAGE";
}

function validateMediaFile(file) {
  if (!POST_MEDIA_MIME_TYPES.includes(file.type)) {
    return "\u0110\u1ECBnh d\u1EA1ng kh\u00F4ng \u0111\u01B0\u1EE3c h\u1ED7 tr\u1EE3. Ch\u1EC9 JPEG, PNG, WEBP, MP4.";
  }
  const kind = resolveMediaKind(file);
  const maxBytes = kind === "VIDEO" ? VIDEO_MAX_BYTES : IMAGE_MAX_BYTES;
  if (file.size > maxBytes) {
    return kind === "VIDEO" ? "Video v\u01B0\u1EE3t qu\u00E1 100MB." : "\u1EA2nh v\u01B0\u1EE3t qu\u00E1 10MB.";
  }
  return "";
}

export function useCommentMediaUpload() {
  const { showSessionExpired } = useAuthSession();
  const objectUrlsRef = useRef([]);
  const [mediaItems, setMediaItems] = useState([]);

  const revokeObjectUrls = useCallback(() => {
    objectUrlsRef.current.forEach((url) => URL.revokeObjectURL(url));
    objectUrlsRef.current = [];
  }, []);

  const resetMedia = useCallback(() => {
    revokeObjectUrls();
    setMediaItems([]);
  }, [revokeObjectUrls]);

  const removeMedia = useCallback((slotId) => {
    setMediaItems((prev) => {
      const target = prev.find((item) => item.id === slotId);
      if (target?.previewUrl) {
        URL.revokeObjectURL(target.previewUrl);
        objectUrlsRef.current = objectUrlsRef.current.filter((url) => url !== target.previewUrl);
      }
      return prev.filter((item) => item.id !== slotId);
    });
  }, []);

  const uploadFile = useCallback(
    async (slotId, file) => {
      setMediaItems((prev) =>
        prev.map((item) =>
          item.id === slotId ? { ...item, status: "uploading", errorMessage: "" } : item
        )
      );

      try {
        const kind = resolveMediaKind(file);
        const meta = await requestPostMediaUploadUrl({
          contentType: file.type,
          fileSizeBytes: file.size,
          mediaKind: kind,
        });
        await uploadPostMediaFile(meta.uploadUrl, file);
        setMediaItems((prev) =>
          prev.map((item) =>
            item.id === slotId
              ? {
                  ...item,
                  status: "done",
                  mediaUrl: meta.mediaUrl,
                  type: meta.mediaKind,
                }
              : item
          )
        );
      } catch (error) {
        if (error?.code === 401) {
          showSessionExpired(error?.message);
          return;
        }
        setMediaItems((prev) =>
          prev.map((item) =>
            item.id === slotId
              ? {
                  ...item,
                  status: "error",
                  errorMessage: error?.message || "Upload th\u1EA5t b\u1EA1i.",
                }
              : item
          )
        );
      }
    },
    [showSessionExpired]
  );

  const addFiles = useCallback(
    async (fileList) => {
      const files = Array.from(fileList || []);
      if (files.length === 0) return;

      const available = MAX_COMMENT_MEDIA_ITEMS - mediaItems.length;
      if (available <= 0) return;

      const selected = files.slice(0, available);
      const newSlots = [];

      for (const file of selected) {
        const errorMessage = validateMediaFile(file);
        const previewUrl = URL.createObjectURL(file);
        objectUrlsRef.current.push(previewUrl);
        newSlots.push({
          id: crypto.randomUUID(),
          file,
          previewUrl,
          status: errorMessage ? "error" : "pending",
          mediaUrl: null,
          type: resolveMediaKind(file),
          errorMessage: errorMessage || "",
        });
      }

      setMediaItems((prev) => [...prev, ...newSlots]);

      for (const slot of newSlots) {
        if (slot.status === "pending") {
          await uploadFile(slot.id, slot.file);
        }
      }
    },
    [mediaItems.length, uploadFile]
  );

  const isUploading = mediaItems.some(
    (item) => item.status === "uploading" || item.status === "pending"
  );
  const hasUploadError = mediaItems.some((item) => item.status === "error");
  const canAddMore = mediaItems.length < MAX_COMMENT_MEDIA_ITEMS;

  return {
    mediaItems,
    addFiles,
    removeMedia,
    resetMedia,
    isUploading,
    hasUploadError,
    canAddMore,
  };
}