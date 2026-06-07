import { useCallback, useState } from "react";
import { deletePost, toggleLikePost } from "../api/postApi";
import { toggleSavePost } from "../api/savePostApi";

export function usePostActions({ onToast, openPostId, closePost } = {}) {
  const [savingPostId, setSavingPostId] = useState(null);
  const [likingPostId, setLikingPostId] = useState(null);
  const [deletingPostId, setDeletingPostId] = useState(null);

  const handleDeletePost = useCallback(
    async (postId, { onRemoved } = {}) => {
      if (!postId) return;

      setDeletingPostId(postId);
      try {
        await deletePost(postId);
        onRemoved?.(postId);
        if (openPostId === postId) {
          closePost?.();
        }
        onToast?.("Đã xóa bài viết.");
      } catch (error) {
        onToast?.(error?.message || "Không xóa được bài viết.");
      } finally {
        setDeletingPostId(null);
      }
    },
    [closePost, onToast, openPostId]
  );

  const handleToggleLikePost = useCallback(
    async (postId, { onLikedChange } = {}) => {
      if (!postId) return null;

      setLikingPostId(postId);
      try {
        const data = await toggleLikePost(postId);
        const liked = Boolean(data?.liked);
        const likeCount = Number(data?.likeCount) || 0;
        onLikedChange?.(postId, liked, likeCount);
        return data;
      } catch (error) {
        onToast?.(error?.message || "Không cập nhật được lượt thích.");
        return null;
      } finally {
        setLikingPostId(null);
      }
    },
    [onToast]
  );

  const handleToggleSavePost = useCallback(
    async (postId, { onSavedChange } = {}) => {
      if (!postId) return null;

      setSavingPostId(postId);
      try {
        const data = await toggleSavePost(postId);
        const saved = Boolean(data?.saved);
        onSavedChange?.(postId, saved);
        onToast?.(saved ? "Đã lưu bài viết." : "Đã bỏ lưu.");
        return data;
      } catch (error) {
        onToast?.(error?.message || "Không cập nhật trạng thái lưu.");
        return null;
      } finally {
        setSavingPostId(null);
      }
    },
    [onToast]
  );

  const isSavingPost = useCallback((postId) => savingPostId === postId, [savingPostId]);
  const isLikingPost = useCallback((postId) => likingPostId === postId, [likingPostId]);
  const isDeletingPost = useCallback((postId) => deletingPostId === postId, [deletingPostId]);

  return {
    handleDeletePost,
    handleToggleLikePost,
    handleToggleSavePost,
    isSavingPost,
    isLikingPost,
    isDeletingPost,
  };
}
