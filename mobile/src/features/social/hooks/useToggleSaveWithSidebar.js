import { useCallback } from "react";

/**
 * Wraps toggle-save with optimistic sidebar saved-count updates.
 */
export function useToggleSaveWithSidebar({
  items,
  patchSaved,
  adjustSavedCount,
  handleToggleSavePost,
  getWasSaved,
  onSavedChange,
}) {
  return useCallback(
    (targetPostId) => {
      const wasSaved =
        getWasSaved?.(targetPostId) ??
        items?.find((post) => post.postId === targetPostId)?.savedByMe ??
        false;

      handleToggleSavePost(targetPostId, {
        onSavedChange: (id, saved) => {
          patchSaved?.(id, saved);
          onSavedChange?.(id, saved, wasSaved);
          if (adjustSavedCount) {
            if (saved && !wasSaved) adjustSavedCount(1);
            if (!saved && wasSaved) adjustSavedCount(-1);
          }
        },
      });
    },
    [adjustSavedCount, getWasSaved, handleToggleSavePost, items, onSavedChange, patchSaved]
  );
}
