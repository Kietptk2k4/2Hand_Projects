import { useCallback, useState } from "react";
import { useSocialWriteBlock } from "../context/SocialWriteBlockContext";

export function useEditPostModal() {
  const { isWriteBlocked } = useSocialWriteBlock();
  const [editPostId, setEditPostId] = useState(null);

  const openEdit = useCallback(
    (postId) => {
      if (!postId || isWriteBlocked) return;
      setEditPostId(postId);
    },
    [isWriteBlocked]
  );

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
