import { useCreatePost } from "../hooks/useCreatePost";
import { PostFormModal } from "./PostFormModal";

export function CreatePostModal({
  onClose,
  openFilePickerOnMount = false,
  onSuccess,
  onToast,
}) {
  const createPost = useCreatePost({
    onSuccess: (created, meta) => {
      onSuccess?.(created, meta);
      onClose?.();
    },
  });

  return (
    <PostFormModal
      mode="create"
      title="Tạo bài viết"
      titleId="create-post-title"
      onClose={onClose}
      onToast={onToast}
      form={createPost}
      openFilePickerOnMount={openFilePickerOnMount}
    />
  );
}
