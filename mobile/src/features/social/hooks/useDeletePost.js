import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Alert } from "react-native";
import { router } from "expo-router";
import { deletePost } from "../api/postApi";
import { useSocialToast } from "../../../shared/components/SocialToastProvider";
import { handleSocialQueryError } from "../utils/handleSocialQueryError";
import {
  removePostFromFeedCaches,
  restoreEngagementCaches,
  snapshotEngagementCaches,
} from "../utils/postEngagementCache";
import { mapSocialWriteError } from "../utils/socialWriteErrors";

const DELETE_CONFIRM_MESSAGE =
  "Bạn có chắc muốn xóa bài viết này? Hành động không thể hoàn tác.";

export function useDeletePost({ onDeleted } = {}) {
  const queryClient = useQueryClient();
  const { showToast } = useSocialToast();

  const mutation = useMutation({
    mutationFn: deletePost,
    onMutate: async (postId) => {
      await queryClient.cancelQueries({ queryKey: ["social", "feed"] });
      const snapshot = snapshotEngagementCaches(queryClient, postId);
      removePostFromFeedCaches(queryClient, postId);
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

      showToast(mapped.message || "Không xóa được bài viết.", "error");
    },
    onSuccess: (_data, postId) => {
      showToast("Đã xóa bài viết.");
      onDeleted?.(postId);
    },
  });

  const confirmDelete = (postId, { navigateBack = false } = {}) => {
    if (!postId || mutation.isPending) return;

    Alert.alert("Xóa bài viết", DELETE_CONFIRM_MESSAGE, [
      { text: "Hủy", style: "cancel" },
      {
        text: "Xóa",
        style: "destructive",
        onPress: () => {
          mutation.mutate(postId, {
            onSuccess: () => {
              if (navigateBack) {
                router.back();
              }
            },
          });
        },
      },
    ]);
  };

  return {
    confirmDelete,
    isDeletingPost: (postId) => mutation.isPending && mutation.variables === postId,
    isPending: mutation.isPending,
  };
}
