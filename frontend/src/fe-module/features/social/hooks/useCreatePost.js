import { useCallback, useRef, useState } from "react";
import { createPost, requestPostMediaUploadUrl, uploadPostMediaFile } from "../api/createPostApi";
import {
  IMAGE_MAX_BYTES,
  MAX_CAPTION_LENGTH,
  MAX_HASHTAGS,
  MAX_MEDIA_ITEMS,
  MAX_PRODUCT_TAGS,
  POST_MEDIA_MIME_TYPES,
  VIDEO_MAX_BYTES,
} from "../constants/createPostConstants";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { mapSocialWriteError } from "../utils/socialWriteErrors";

function resolveMediaKind(file) {
  return file.type.startsWith("video/") ? "VIDEO" : "IMAGE";
}

function validateFile(file) {
  if (!POST_MEDIA_MIME_TYPES.includes(file.type)) {
    return "Định dạng không được hỗ trợ. Chỉ JPEG, PNG, WEBP, MP4.";
  }
  const kind = resolveMediaKind(file);
  const maxBytes = kind === "VIDEO" ? VIDEO_MAX_BYTES : IMAGE_MAX_BYTES;
  if (file.size > maxBytes) {
    return kind === "VIDEO" ? "Video vượt quá 100MB." : "Ảnh vượt quá 10MB.";
  }
  return "";
}

function normalizeHashtag(raw) {
  const value = (raw || "").trim().replace(/^#/, "");
  if (!value || value.length > 100) return null;
  return value;
}

export function useCreatePost({ onSuccess }) {
  const { showSessionExpired } = useAuthSession();
  const objectUrlsRef = useRef([]);

  const [caption, setCaption] = useState("");
  const [visibility, setVisibility] = useState("PUBLIC");
  const [allowComments, setAllowComments] = useState(true);
  const [hashtags, setHashtags] = useState([]);
  const [hashtagInput, setHashtagInput] = useState("");
  const [productTags, setProductTags] = useState([]);
  const [mediaItems, setMediaItems] = useState([]);
  const [activeMediaIndex, setActiveMediaIndex] = useState(0);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [globalError, setGlobalError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});

  const revokeObjectUrls = useCallback(() => {
    objectUrlsRef.current.forEach((url) => URL.revokeObjectURL(url));
    objectUrlsRef.current = [];
  }, []);

  const resetForm = useCallback(() => {
    revokeObjectUrls();
    setCaption("");
    setVisibility("PUBLIC");
    setAllowComments(true);
    setHashtags([]);
    setHashtagInput("");
    setProductTags([]);
    setMediaItems([]);
    setActiveMediaIndex(0);
    setGlobalError("");
    setFieldErrors({});
    setIsSubmitting(false);
  }, [revokeObjectUrls]);

  const uploadFile = useCallback(
    async (slotId, file) => {
      const kind = resolveMediaKind(file);
      setMediaItems((prev) =>
        prev.map((item) =>
          item.id === slotId ? { ...item, status: "uploading", progress: 10, errorMessage: "" } : item
        )
      );

      try {
        const meta = await requestPostMediaUploadUrl({
          contentType: file.type,
          fileSizeBytes: file.size,
          mediaKind: kind,
        });

        setMediaItems((prev) =>
          prev.map((item) => (item.id === slotId ? { ...item, progress: 50 } : item))
        );

        await uploadPostMediaFile(meta.uploadUrl, file);

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
                  errorMessage: error?.message || "Upload thất bại.",
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

      setGlobalError("");
      const available = MAX_MEDIA_ITEMS - mediaItems.length;
      if (available <= 0) {
        setGlobalError(`Tối đa ${MAX_MEDIA_ITEMS} ảnh hoặc video.`);
        return;
      }

      const toAdd = files.slice(0, available);
      const newSlots = [];

      for (const file of toAdd) {
        const validationError = validateFile(file);
        const previewUrl = URL.createObjectURL(file);
        objectUrlsRef.current.push(previewUrl);
        const id = crypto.randomUUID();
        newSlots.push({
          id,
          file,
          previewUrl,
          mediaUrl: null,
          type: resolveMediaKind(file),
          status: validationError ? "error" : "pending",
          progress: 0,
          errorMessage: validationError,
        });
      }

      setMediaItems((prev) => {
        const merged = [...prev, ...newSlots];
        return merged;
      });

      if (mediaItems.length === 0 && newSlots.length > 0) {
        setActiveMediaIndex(0);
      }

      for (const slot of newSlots) {
        if (slot.status === "pending" && slot.file) {
          await uploadFile(slot.id, slot.file);
        }
      }
    },
    [mediaItems.length, uploadFile]
  );

  const removeMedia = useCallback((slotId) => {
    setMediaItems((prev) => {
      const target = prev.find((item) => item.id === slotId);
      if (target?.previewUrl) {
        URL.revokeObjectURL(target.previewUrl);
        objectUrlsRef.current = objectUrlsRef.current.filter((url) => url !== target.previewUrl);
      }
      const next = prev.filter((item) => item.id !== slotId);
      setActiveMediaIndex((index) => Math.min(index, Math.max(next.length - 1, 0)));
      return next;
    });
  }, []);

  const addHashtag = useCallback((raw) => {
    const tag = normalizeHashtag(raw);
    if (!tag) return;
    setHashtags((prev) => {
      if (prev.length >= MAX_HASHTAGS) return prev;
      if (prev.some((item) => item.toLowerCase() === tag.toLowerCase())) return prev;
      return [...prev, tag];
    });
    setHashtagInput("");
  }, []);

  const removeHashtag = useCallback((tag) => {
    setHashtags((prev) => prev.filter((item) => item !== tag));
  }, []);

  const addProductTag = useCallback((product) => {
    setProductTags((prev) => {
      if (prev.length >= MAX_PRODUCT_TAGS) return prev;
      if (prev.some((item) => item.productId === product.productId)) return prev;
      return [
        ...prev,
        {
          productId: product.productId,
          name: product.name,
          category: product.category,
          price: product.defaultPrice,
        },
      ];
    });
  }, []);

  const removeProductTag = useCallback((productId) => {
    setProductTags((prev) => prev.filter((item) => item.productId !== productId));
  }, []);

  const updateProductTagPrice = useCallback((productId, price) => {
    setProductTags((prev) =>
      prev.map((item) =>
        item.productId === productId ? { ...item, price: Number(price) || 0 } : item
      )
    );
  }, []);

  const validateForm = useCallback(() => {
    const errors = {};
    if (caption.length > MAX_CAPTION_LENGTH) {
      errors.caption = `Mô tả tối đa ${MAX_CAPTION_LENGTH} ký tự.`;
    }
    if (!visibility) {
      errors.visibility = "Chọn quyền hiển thị.";
    }
    const hasUploading = mediaItems.some((item) => item.status === "uploading" || item.status === "pending");
    if (hasUploading) {
      errors.media = "Đang tải ảnh hoặc video, vui lòng đợi.";
    }
    const hasMediaError = mediaItems.some((item) => item.status === "error");
    if (hasMediaError) {
      errors.media = "Có file upload lỗi. Xóa hoặc thử lại.";
    }
    if (!caption.trim() && mediaItems.filter((m) => m.status === "done").length === 0) {
      errors.caption = "Nhập mô tả hoặc thêm ít nhất một ảnh hoặc video.";
    }
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  }, [caption, mediaItems, visibility]);

  const submit = useCallback(
    async (publish) => {
      if (isSubmitting) return;
      if (!validateForm()) return;

      setIsSubmitting(true);
      setGlobalError("");

      const readyMedia = mediaItems
        .filter((item) => item.status === "done" && item.mediaUrl)
        .map((item) => ({ url: item.mediaUrl, type: item.type }));

      const payload = {
        caption: caption.trim() || undefined,
        visibility,
        allowComments,
        hashtags: hashtags.length > 0 ? hashtags : undefined,
        publish,
      };

      if (readyMedia.length > 0) {
        payload.media = readyMedia;
      }

      if (productTags.length > 0) {
        payload.productTags = productTags.map((item) => ({
          productId: item.productId,
          price: item.price,
        }));
      }

      try {
        const created = await createPost(payload);
        resetForm();
        onSuccess?.(created, { publish });
      } catch (error) {
        const mapped = mapSocialWriteError(error);
        if (mapped.type === "session") {
          showSessionExpired(mapped.message);
          return;
        }
        if (mapped.type === "suspended") {
          return;
        }
        if (mapped.raw?.errors?.length) {
          const fieldMapped = mapped.raw.errors.reduce((acc, item) => {
            if (item.field) acc[item.field] = item.reason;
            return acc;
          }, {});
          setFieldErrors(fieldMapped);
        }
        setGlobalError(mapped.message || "Không tạo được bài viết.");
      } finally {
        setIsSubmitting(false);
      }
    },
    [
      allowComments,
      caption,
      hashtags,
      isSubmitting,
      mediaItems,
      onSuccess,
      productTags,
      resetForm,
      showSessionExpired,
      validateForm,
      visibility,
    ]
  );

  const activeMedia = mediaItems[activeMediaIndex] || null;
  const isUploadingMedia = mediaItems.some(
    (item) => item.status === "uploading" || item.status === "pending"
  );

  return {
    caption,
    setCaption,
    visibility,
    setVisibility,
    allowComments,
    setAllowComments,
    hashtags,
    hashtagInput,
    setHashtagInput,
    addHashtag,
    removeHashtag,
    productTags,
    addProductTag,
    removeProductTag,
    updateProductTagPrice,
    mediaItems,
    activeMedia,
    activeMediaIndex,
    setActiveMediaIndex,
    addFiles,
    removeMedia,
    submit,
    resetForm,
    isSubmitting,
    isUploadingMedia,
    globalError,
    fieldErrors,
  };
}
