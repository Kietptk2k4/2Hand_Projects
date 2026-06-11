import { useCallback } from "react";
import * as ImagePicker from "expo-image-picker";
import { requestPostMediaUploadUrl, uploadPostMediaFile } from "../api/createPostApi";
import {
  IMAGE_MAX_BYTES,
  MAX_MEDIA_ITEMS,
  POST_MEDIA_MIME_TYPES,
  VIDEO_MAX_BYTES,
} from "../constants/createPostConstants";
import { handleSocialQueryError } from "../utils/handleSocialQueryError";

function createSlotId() {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`;
}

function resolveMediaKind(mimeType) {
  return String(mimeType || "").startsWith("video/") ? "VIDEO" : "IMAGE";
}

function normalizeMimeType(asset) {
  if (asset.mimeType) return asset.mimeType;
  if (asset.type === "video") return "video/mp4";
  return "image/jpeg";
}

function validateAsset(asset) {
  const mimeType = normalizeMimeType(asset);
  if (!POST_MEDIA_MIME_TYPES.includes(mimeType)) {
    return "Định dạng không được hỗ trợ. Chỉ JPEG, PNG, WEBP, MP4.";
  }

  const kind = resolveMediaKind(mimeType);
  const size = asset.fileSize || 0;
  const maxBytes = kind === "VIDEO" ? VIDEO_MAX_BYTES : IMAGE_MAX_BYTES;

  if (size > maxBytes) {
    return kind === "VIDEO" ? "Video vượt quá 100MB." : "Ảnh vượt quá 10MB.";
  }

  return "";
}

export function usePostMediaUpload({ mediaItems, setMediaItems, setGlobalError }) {
  const uploadAsset = useCallback(
    async (slotId, asset) => {
      const mimeType = normalizeMimeType(asset);
      const kind = resolveMediaKind(mimeType);
      const fileSizeBytes = asset.fileSize || 0;

      setMediaItems((prev) =>
        prev.map((item) =>
          item.id === slotId
            ? { ...item, status: "uploading", progress: 10, errorMessage: "" }
            : item
        )
      );

      try {
        const meta = await requestPostMediaUploadUrl({
          contentType: mimeType,
          fileSizeBytes,
          mediaKind: kind,
        });

        setMediaItems((prev) =>
          prev.map((item) => (item.id === slotId ? { ...item, progress: 50 } : item))
        );

        await uploadPostMediaFile(meta.uploadUrl, {
          uri: asset.uri,
          mimeType,
        });

        setMediaItems((prev) =>
          prev.map((item) =>
            item.id === slotId
              ? {
                  ...item,
                  status: "done",
                  progress: 100,
                  mediaUrl: meta.mediaUrl,
                  type: meta.mediaKind,
                }
              : item
          )
        );
      } catch (error) {
        const handled = await handleSocialQueryError(error);
        if (handled) return;

        setMediaItems((prev) =>
          prev.map((item) =>
            item.id === slotId
              ? {
                  ...item,
                  status: "error",
                  errorMessage: error?.message || "Upload thất bại.",
                }
              : item
          )
        );
      }
    },
    [setMediaItems]
  );

  const pickAndAddMedia = useCallback(async () => {
    setGlobalError?.("");

    const permission = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (!permission.granted) {
      setGlobalError?.("Cần quyền truy cập thư viện ảnh để thêm media.");
      return;
    }

    const available = MAX_MEDIA_ITEMS - mediaItems.length;
    if (available <= 0) {
      setGlobalError?.(`Tối đa ${MAX_MEDIA_ITEMS} ảnh hoặc video.`);
      return;
    }

    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ["images", "videos"],
      allowsMultipleSelection: true,
      selectionLimit: available,
      quality: 0.9,
    });

    if (result.canceled || !result.assets?.length) return;

    const newSlots = result.assets.map((asset) => {
      const validationError = validateAsset(asset);
      const mimeType = normalizeMimeType(asset);
      return {
        id: createSlotId(),
        previewUrl: asset.uri,
        mediaUrl: null,
        type: resolveMediaKind(mimeType),
        status: validationError ? "error" : "pending",
        progress: 0,
        errorMessage: validationError,
        asset,
      };
    });

    setMediaItems((prev) => [...prev, ...newSlots]);

    for (const slot of newSlots) {
      if (slot.status === "pending" && slot.asset) {
        await uploadAsset(slot.id, slot.asset);
      }
    }
  }, [mediaItems.length, setGlobalError, setMediaItems, uploadAsset]);

  const removeMedia = useCallback(
    (slotId) => {
      setMediaItems((prev) => prev.filter((item) => item.id !== slotId));
    },
    [setMediaItems]
  );

  return {
    pickAndAddMedia,
    removeMedia,
    uploadAsset,
  };
}
