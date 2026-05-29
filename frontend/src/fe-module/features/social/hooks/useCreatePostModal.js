import { useCallback, useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";

export function useCreatePostModal() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [isOpen, setIsOpen] = useState(false);
  const [openFilePickerOnMount, setOpenFilePickerOnMount] = useState(false);

  useEffect(() => {
    if (searchParams.get("createPost") === "1") {
      setIsOpen(true);
    }
  }, [searchParams]);

  const openCreatePost = useCallback(({ filePicker = false } = {}) => {
    setOpenFilePickerOnMount(filePicker);
    setIsOpen(true);
  }, []);

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
