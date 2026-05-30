import { useCallback, useState } from "react";

export function useEditPostModal() {
  const [editPostId, setEditPostId] = useState(null);

  const openEdit = useCallback((postId) => {
    if (!postId) return;
    setEditPostId(postId);
  }, []);

  const closeEdit = useCallback(() => {
    setEditPostId(null);
  }, []);

  return {
    editPostId,
    isEditOpen: Boolean(editPostId),
    openEdit,
    closeEdit,
  };
}
