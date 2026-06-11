import { useCallback, useMemo, useState } from "react";
import { MAX_HASHTAGS } from "../constants/createPostConstants";
import { mapPostMediaPayload } from "../utils/mapPostMediaPayload";
import { validatePostForm } from "../utils/validatePostForm";
import { getPostMediaUrl } from "../utils/postMediaType";
import { usePostMediaUpload } from "./usePostMediaUpload";

function normalizeHashtag(raw) {
  const value = (raw || "").trim().replace(/^#/, "");
  if (!value || value.length > 100) return null;
  return value;
}

export function mapExistingMediaToSlots(media) {
  return (media || []).map((item, index) => {
    const url = getPostMediaUrl(item);
    return {
      id: `existing-${index}-${url}`,
      previewUrl: url,
      mediaUrl: url,
      type: item.type,
      status: "done",
      progress: 100,
      errorMessage: "",
    };
  });
}

export function usePostComposer({ initialSnapshot = null } = {}) {
  const [caption, setCaption] = useState(initialSnapshot?.caption || "");
  const [visibility, setVisibility] = useState(initialSnapshot?.visibility || "PUBLIC");
  const [allowComments, setAllowComments] = useState(
    initialSnapshot?.allowComments !== false
  );
  const [hashtags, setHashtags] = useState(initialSnapshot?.hashtags || []);
  const [hashtagInput, setHashtagInput] = useState("");
  const [mediaItems, setMediaItems] = useState(
    initialSnapshot?.media ? mapExistingMediaToSlots(initialSnapshot.media) : []
  );
  const [activeMediaIndex, setActiveMediaIndex] = useState(0);
  const [fieldErrors, setFieldErrors] = useState({});
  const [globalError, setGlobalError] = useState("");

  const { pickAndAddMedia, removeMedia } = usePostMediaUpload({
    mediaItems,
    setMediaItems,
    setGlobalError,
  });

  const populateFromSnapshot = useCallback((snapshot) => {
    setCaption(snapshot.caption || "");
    setVisibility(snapshot.visibility || "PUBLIC");
    setAllowComments(snapshot.allowComments !== false);
    setHashtags(snapshot.hashtags || []);
    setHashtagInput("");
    setMediaItems(mapExistingMediaToSlots(snapshot.media));
    setActiveMediaIndex(0);
    setFieldErrors({});
    setGlobalError("");
  }, []);

  const resetForm = useCallback(() => {
    setCaption("");
    setVisibility("PUBLIC");
    setAllowComments(true);
    setHashtags([]);
    setHashtagInput("");
    setMediaItems([]);
    setActiveMediaIndex(0);
    setFieldErrors({});
    setGlobalError("");
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

  const getReadyMedia = useCallback(() => {
    return mediaItems
      .filter((item) => item.status === "done" && item.mediaUrl)
      .map((item) =>
        mapPostMediaPayload({
          url: item.mediaUrl,
          type: item.type,
        })
      );
  }, [mediaItems]);

  const validateForm = useCallback(
    ({ requireContent = true } = {}) => {
      const errors = validatePostForm({ caption, mediaItems, requireContent });
      setFieldErrors(errors);
      return Object.keys(errors).length === 0;
    },
    [caption, mediaItems]
  );

  const activeMedia = mediaItems[activeMediaIndex] || null;
  const isUploadingMedia = mediaItems.some(
    (item) => item.status === "uploading" || item.status === "pending"
  );

  const currentSnapshot = useMemo(
    () => ({
      caption: (caption || "").trim(),
      visibility,
      allowComments,
      hashtags: [...hashtags],
      media: getReadyMedia(),
    }),
    [allowComments, caption, getReadyMedia, hashtags, visibility]
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
    mediaItems,
    activeMedia,
    activeMediaIndex,
    setActiveMediaIndex,
    pickAndAddMedia,
    removeMedia,
    populateFromSnapshot,
    resetForm,
    validateForm,
    getReadyMedia,
    currentSnapshot,
    isUploadingMedia,
    globalError,
    setGlobalError,
    fieldErrors,
    setFieldErrors,
  };
}
