import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toggleSavePost } from "../api/savePostApi";
import { useSocialToast } from "../../../shared/components/SocialToastProvider";
import { handleSocialQueryError } from "../utils/handleSocialQueryError";
import {
  computeOptimisticSave,
  findPostInFeedCaches,
  patchPostEngagement,
  restoreEngagementCaches,
  snapshotEngagementCaches,
} from "../utils/postEngagementCache";
import { removeUnsavedPostFromSavedList } from "../utils/listPostCache";
import { mapSocialWriteError } from "../utils/socialWriteErrors";

export function useSavePost() {
  const queryClient = useQueryClient();
  const { showToast } = useSocialToast();

  const mutation = useMutation({
    mutationFn: toggleSavePost,
    onMutate: async (postId) => {
      await queryClient.cancelQueries({ queryKey: ["social", "post", "detail", postId] });
      await queryClient.cancelQueries({ queryKey: ["social", "feed"] });

      const snapshot = snapshotEngagementCaches(queryClient, postId);
      const current =
        snapshot.detail || findPostInFeedCaches(queryClient, postId) || {};
      const optimistic = computeOptimisticSave(current);

      patchPostEngagement(queryClient, postId, optimistic);

      return { snapshot, postId };
    },
    onError: async (error, postId, context) => {
      if (context?.snapshot) {
        restoreEngagementCaches(queryClient, postId, context.snapshot);
      }

      const mapped = mapSocialWriteError(error);
      if (mapped.type === "session") {
        await handleSocialQueryError({ code: 401, message: mapped.message });
        return;
      }

      showToast(mapped.message || "Không cập nhật trạng thái lưu.", "error");
    },
    onSuccess: (data) => {
      if (!data?.postId) return;

      const saved = Boolean(data.saved);
      patchPostEngagement(queryClient, data.postId, { savedByMe: saved });
      if (!saved) {
        removeUnsavedPostFromSavedList(queryClient, data.postId);
      }
      showToast(saved ? "Đã lưu bài viết." : "Đã bỏ lưu.");
    },
  });

  return {
    toggleSave: mutation.mutate,
    isSavingPost: (postId) => mutation.isPending && mutation.variables === postId,
    isPending: mutation.isPending,
  };
}
