import { useCallback, useEffect, useRef, useState } from "react";
import { requestPostMediaUploadUrl, uploadPostMediaFile } from "../api/createPostApi";
import { updatePost } from "../api/editPostApi";
import { fetchPostDetail } from "../api/postApi";
import {
  IMAGE_MAX_BYTES,
  MAX_CAPTION_LENGTH,
  MAX_HASHTAGS,
  MAX_MEDIA_ITEMS,
  MAX_PRODUCT_TAGS,
  POST_MEDIA_MIME_TYPES,
  VIDEO_MAX_BYTES,
} from "../constants/createPostConstants";
import { buildEditPatchBody, buildPatchSnapshot } from "../utils/buildEditPatch";
import { loadProductCatalogEntry } from "../api/postProductTagApi";
import { mergeTagWithCatalog } from "../utils/postProductTagMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { normalizePostMediaUrl } from "../utils/postMediaUrl";
import { mapPostMediaPayload } from "../utils/postMediaAspectRatio";
import { readFileMediaDimensions } from "../utils/readMediaDimensions";
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

function mapMediaToSlots(media) {
  return (media || []).map((item, index) => {
    const url = normalizePostMediaUrl(item.url);
    return {
      id: `existing-${index}-${url}`,
      file: null,
      previewUrl: url,
      mediaUrl: url,
      type: item.type,
      width: item.width ?? null,
      height: item.height ?? null,
      status: "done",
      progress: 100,
      errorMessage: "",
    };
  });
}

function getReadyMedia(mediaItems) {
  return mediaItems
    .filter((item) => item.status === "done" && item.mediaUrl)
    .map((item) =>
      mapPostMediaPayload({
        url: item.mediaUrl,
        type: item.type,
        width: item.width,
        height: item.height,
      }),
    );
}

export function useEditPost({ postId, onSuccess }) {
  const { showSessionExpired } = useAuthSession();
  const objectUrlsRef = useRef([]);
  const initialSnapshotRef = useRef(null);

  const [caption, setCaption] = useState("");
  const [visibility, setVisibility] = useState("PUBLIC");
  const [allowComments, setAllowComments] = useState(true);
  const [hashtags, setHashtags] = useState([]);
  const [hashtagInput, setHashtagInput] = useState("");
  const [productTags, setProductTags] = useState([]);
  const [mediaItems, setMediaItems] = useState([]);
  const [activeMediaIndex, setActiveMediaIndex] = useState(0);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [loadStatus, setLoadStatus] = useState("idle");
  const [loadError, setLoadError] = useState("");
  const [globalError, setGlobalError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});

  const revokeObjectUrls = useCallback(() => {
    objectUrlsRef.current.forEach((url) => URL.revokeObjectURL(url));
    objectUrlsRef.current = [];
  }, []);

  const resetForm = useCallback(() => {
    revokeObjectUrls();
    initialSnapshotRef.current = null;
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
    setLoadStatus("idle");
    setLoadError("");
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
        let width = null;
        let height = null;
        if (!validationError) {
          try {
            const dimensions = await readFileMediaDimensions(file);
            width = dimensions.width;
            height = dimensions.height;
          } catch {
            width = null;
            height = null;
          }
        }
        newSlots.push({
          id,
          file,
          previewUrl,
          mediaUrl: null,
          type: resolveMediaKind(file),
          width,
          height,
          status: validationError ? "error" : "pending",
          progress: 0,
          errorMessage: validationError,
        });
      }

      setMediaItems((prev) => [...prev, ...newSlots]);

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
      if (target?.previewUrl?.startsWith("blob:")) {
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

  useEffect(() => {
    if (!postId) {
      resetForm();
      return undefined;
    }

    let cancelled = false;

    async function loadPost() {
      setLoadStatus("loading");
      setLoadError("");
      setGlobalError("");
      revokeObjectUrls();

      try {
        const post = await fetchPostDetail(postId);
        if (cancelled) return;

        const snapshot = buildPatchSnapshot(post);
        initialSnapshotRef.current = snapshot;

        setCaption(snapshot.caption);
        setVisibility(snapshot.visibility);
        setAllowComments(snapshot.allowComments);
        setHashtags(snapshot.hashtags);
        const enrichedTags = await Promise.all(
          (post.productTags || []).map(async (tag) => {
            const catalog = await loadProductCatalogEntry(tag.productId);
            return mergeTagWithCatalog(tag, catalog);
          }),
        );
        if (cancelled) return;
        setProductTags(enrichedTags.filter(Boolean));
        setMediaItems(mapMediaToSlots(post.media));
        setActiveMediaIndex(0);
        setLoadStatus("ready");
      } catch (error) {
        if (cancelled) return;
        if (error?.code === 401) {
          showSessionExpired(error?.message);
          return;
        }
        setLoadStatus("error");
        setLoadError(error?.message || "Không tải được bài viết.");
      }
    }

    loadPost();

    return () => {
      cancelled = true;
    };
  }, [postId, resetForm, revokeObjectUrls, showSessionExpired]);

  const validateForm = useCallback(() => {
    const errors = {};
    if (caption.length > MAX_CAPTION_LENGTH) {
      errors.caption = `Mô tả tối đa ${MAX_CAPTION_LENGTH} ký tự.`;
    }
    const hasUploading = mediaItems.some(
      (item) => item.status === "uploading" || item.status === "pending"
    );
    if (hasUploading) {
      errors.media = "Đang tải ảnh hoặc video, vui lòng đợi.";
    }
    const hasMediaError = mediaItems.some((item) => item.status === "error");
    if (hasMediaError) {
      errors.media = "Có file upload lỗi. Xóa hoặc thử lại.";
    }
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  }, [caption, mediaItems]);

  const submitUpdate = useCallback(async () => {
    if (isSubmitting || loadStatus !== "ready" || !initialSnapshotRef.current) return;
    if (!validateForm()) return;

    setIsSubmitting(true);
    setGlobalError("");

    const current = {
      caption,
      visibility,
      allowComments,
      hashtags,
      media: getReadyMedia(mediaItems),
      productTags: productTags.map((item) => ({
        productId: item.productId,
        price: item.price,
      })),
    };

    const patchBody = buildEditPatchBody(initialSnapshotRef.current, current);

    try {
      const updated = await updatePost(postId, patchBody);
      onSuccess?.(updated);
    } catch (error) {
      if (error?.code === 401) {
        showSessionExpired(error?.message);
        return;
      }
      if (error?.errors?.length) {
        const mapped = error.errors.reduce((acc, item) => {
          if (item.field) acc[item.field] = item.reason;
          return acc;
        }, {});
        setFieldErrors(mapped);
      }
      setGlobalError(error?.message || "Không cập nhật được bài viết.");
    } finally {
      setIsSubmitting(false);
    }
  }, [
    allowComments,
    caption,
    hashtags,
    isSubmitting,
    loadStatus,
    mediaItems,
    onSuccess,
    postId,
    productTags,
    showSessionExpired,
    validateForm,
    visibility,
  ]);

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
    submitUpdate,
    resetForm,
    isSubmitting,
    isUploadingMedia,
    isLoadingInitial: loadStatus === "loading",
    isLoadError: loadStatus === "error",
    loadError,
    globalError,
    fieldErrors,
  };
}
