import { useCallback, useEffect, useRef, useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { updatePost } from "../api/editPostApi";
import { fetchPostDetail } from "../api/postApi";
import { buildEditPatchBody, buildPatchSnapshot } from "../utils/buildEditPatch";
import { applyPostEditToCaches } from "../utils/postFormCache";
import { handleSocialQueryError } from "../utils/handleSocialQueryError";
import { mapSocialWriteError } from "../utils/socialWriteErrors";
import { resolvePostIsOwner } from "../utils/resolvePostAuthorId";
import { useCurrentUserId } from "./useCurrentUserId";
import { usePostComposer } from "./usePostComposer";

export function useEditPost({ postId, onSuccess } = {}) {
  const queryClient = useQueryClient();
  const currentUserId = useCurrentUserId();
  const composer = usePostComposer();
  const initialSnapshotRef = useRef(null);

  const [loadStatus, setLoadStatus] = useState("idle");
  const [loadError, setLoadError] = useState("");
  const [isUnauthorized, setIsUnauthorized] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [reloadKey, setReloadKey] = useState(0);

  const mutation = useMutation({
    mutationFn: ({ postId: id, patchBody }) => updatePost(id, patchBody),
    onSuccess: (updated) => {
      applyPostEditToCaches(queryClient, updated);
      onSuccess?.(updated);
    },
  });

  useEffect(() => {
    if (!postId) {
      composer.resetForm();
      initialSnapshotRef.current = null;
      setLoadStatus("idle");
      return undefined;
    }

    let cancelled = false;

    async function loadPost() {
      setLoadStatus("loading");
      setLoadError("");
      setIsUnauthorized(false);
      composer.setGlobalError("");

      try {
        const post = await fetchPostDetail(postId);
        if (cancelled) return;

        if (!resolvePostIsOwner(post, currentUserId)) {
          setIsUnauthorized(true);
          setLoadStatus("error");
          setLoadError("Bạn không có quyền chỉnh sửa bài viết này.");
          return;
        }

        const snapshot = buildPatchSnapshot(post);
        initialSnapshotRef.current = snapshot;
        composer.populateFromSnapshot(snapshot);
        setLoadStatus("ready");
      } catch (error) {
        if (cancelled) return;
        const handled = await handleSocialQueryError(error);
        if (handled) return;

        setLoadStatus("error");
        setLoadError(error?.message || "Không tải được bài viết.");
      }
    }

    loadPost();

    return () => {
      cancelled = true;
    };
  }, [postId, currentUserId, reloadKey]);

  const submitUpdate = useCallback(async () => {
    if (isSubmitting || mutation.isPending || loadStatus !== "ready") return;
    if (!initialSnapshotRef.current) return;
    if (!composer.validateForm({ requireContent: false })) return;

    composer.setGlobalError("");
    setIsSubmitting(true);

    const patchBody = buildEditPatchBody(
      initialSnapshotRef.current,
      composer.currentSnapshot
    );

    if (Object.keys(patchBody).length === 0) {
      composer.setGlobalError("Không có thay đổi để lưu.");
      setIsSubmitting(false);
      return;
    }

    try {
      await mutation.mutateAsync({ postId, patchBody });
    } catch (error) {
      const handled = await handleSocialQueryError(error);
      if (handled) return;

      const mapped = mapSocialWriteError(error);
      if (mapped.type === "suspended") {
        composer.setGlobalError(mapped.message);
        return;
      }

      if (mapped.raw?.errors?.length) {
        const fieldMapped = mapped.raw.errors.reduce((acc, item) => {
          if (item.field) acc[item.field] = item.reason;
          return acc;
        }, {});
        composer.setFieldErrors(fieldMapped);
      }

      composer.setGlobalError(mapped.message || "Không cập nhật được bài viết.");
    } finally {
      setIsSubmitting(false);
    }
  }, [composer, isSubmitting, loadStatus, mutation, postId]);

  return {
    ...composer,
    submitUpdate,
    isSubmitting: isSubmitting || mutation.isPending,
    isLoadingInitial: loadStatus === "loading",
    isLoadError: loadStatus === "error",
    loadError,
    isUnauthorized,
    retryLoad: () => setReloadKey((value) => value + 1),
  };
}
