import { useCallback, useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { useSocialWriteBlock } from "../context/SocialWriteBlockContext";

export function useCreatePostModal() {
  const { isWriteBlocked } = useSocialWriteBlock();
  const [searchParams, setSearchParams] = useSearchParams();
  const [isOpen, setIsOpen] = useState(false);
  const [openFilePickerOnMount, setOpenFilePickerOnMount] = useState(false);

  useEffect(() => {
    if (searchParams.get("createPost") === "1" && !isWriteBlocked) {
      setIsOpen(true);
    }
  }, [isWriteBlocked, searchParams]);

  const openCreatePost = useCallback(
    ({ filePicker = false } = {}) => {
      if (isWriteBlocked) return;
      setOpenFilePickerOnMount(filePicker);
      setIsOpen(true);
    },
    [isWriteBlocked]
  );

  const closeCreatePost = useCallback(() => {
    setIsOpen(false);
    setOpenFilePickerOnMount(false);
    setSearchParams(
      (prev) => {
        const next = new URLSearchParams(prev);
        next.delete("createPost");
        return next;
      },
      { replace: true }
    );
  }, [setSearchParams]);

  return {
    isOpen,
    openFilePickerOnMount,
    openCreatePost,
    closeCreatePost,
  };
}
