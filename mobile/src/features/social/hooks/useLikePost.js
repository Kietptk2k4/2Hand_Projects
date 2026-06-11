import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toggleLikePost } from "../api/postApi";
import { useSocialToast } from "../../../shared/components/SocialToastProvider";
import { handleSocialQueryError } from "../utils/handleSocialQueryError";
import {
  computeOptimisticLike,
  findPostInFeedCaches,
  patchPostEngagement,
  restoreEngagementCaches,
  snapshotEngagementCaches,
} from "../utils/postEngagementCache";
import { mapSocialWriteError } from "../utils/socialWriteErrors";

export function useLikePost() {
  const queryClient = useQueryClient();
  const { showToast } = useSocialToast();

  const mutation = useMutation({
    mutationFn: toggleLikePost,
    onMutate: async (postId) => {
      await queryClient.cancelQueries({ queryKey: ["social", "post", "detail", postId] });
      await queryClient.cancelQueries({ queryKey: ["social", "feed"] });

      const snapshot = snapshotEngagementCaches(queryClient, postId);
      const current =
        snapshot.detail || findPostInFeedCaches(queryClient, postId) || {};
      const optimistic = computeOptimisticLike(current);

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

      showToast(mapped.message || "Không cập nhật được lượt thích.", "error");
    },
    onSuccess: (data) => {
      if (!data?.postId) return;
      patchPostEngagement(queryClient, data.postId, {
        likedByMe: Boolean(data.liked),
        likeCount: Number(data.likeCount) || 0,
      });
    },
  });

  return {
    toggleLike: mutation.mutate,
    isLikingPost: (postId) => mutation.isPending && mutation.variables === postId,
    isPending: mutation.isPending,
  };
}
