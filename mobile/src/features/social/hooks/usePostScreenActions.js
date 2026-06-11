import { useCallback } from "react";
import { router } from "expo-router";
import { useCurrentUserId } from "./useCurrentUserId";
import { useDeletePost } from "./useDeletePost";
import { useLikePost } from "./useLikePost";
import { useSavePost } from "./useSavePost";
import { ROUTES } from "../../../shared/constants/routes";

export function usePostScreenActions({ onPostRemoved } = {}) {
  const currentUserId = useCurrentUserId();
  const { toggleLike, isLikingPost } = useLikePost();
  const { toggleSave, isSavingPost } = useSavePost();
  const { confirmDelete, isDeletingPost } = useDeletePost({ onDeleted: onPostRemoved });

  const onOpenPost = useCallback((postId, options = {}) => {
    router.push(ROUTES.postDetail(postId, options));
  }, []);

  const onViewProfile = useCallback((userId) => {
    if (!userId) return;
    router.push(ROUTES.userProfile(userId));
  }, []);

  const onEditPost = useCallback((postId) => {
    if (!postId) return;
    router.push(ROUTES.postEdit(postId));
  }, []);

  const onHashtagClick = useCallback((tag) => {
    if (!tag) return;
    router.push(ROUTES.hashtag(tag));
  }, []);

  return {
    currentUserId,
    toggleLike,
    toggleSave,
    confirmDelete,
    isLikingPost,
    isSavingPost,
    isDeletingPost,
    onOpenPost,
    onViewProfile,
    onEditPost,
    onHashtagClick,
  };
}
