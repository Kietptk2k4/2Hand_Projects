import { useEffect } from "react";
import { useEditPost } from "../hooks/useEditPost";
import { PostFormModal } from "./PostFormModal";

export function EditPostModal({ postId, onClose, onSuccess, onToast }) {
  const editPost = useEditPost({
    postId,
    onSuccess: (updated) => {
      onSuccess?.(updated);
      onClose?.();
    },
  });

  const { resetForm, isLoadError, loadError } = editPost;

  useEffect(() => {
    return () => resetForm();
  }, [resetForm]);

  if (isLoadError) {
    return (
      <div
        className="fixed inset-0 z-[60] flex items-center justify-center bg-on-background/40 p-4 backdrop-blur-md"
        role="presentation"
        onClick={onClose}
      >
        <div
          className="max-w-md rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-lg"
          role="alertdialog"
          onClick={(event) => event.stopPropagation()}
        >
          <p className="text-sm text-on-surface">{loadError}</p>
          <button
            type="button"
            onClick={onClose}
            className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-medium text-on-primary"
          >
            Đóng
          </button>
        </div>
      </div>
    );
  }

  return (
    <PostFormModal
      mode="edit"
      title="Sửa bài viết"
      titleId="edit-post-title"
      onClose={onClose}
      onToast={onToast}
      form={editPost}
      isLoadingInitial={editPost.isLoadingInitial}
      loadError=""
    />
  );
}
