import { useMutation, useQueryClient } from "@tanstack/react-query";
import { followUser, unfollowUser } from "../api/followApi";
import { profileKeys } from "../api/profileKeys";
import { useSocialToast } from "../../../shared/components/SocialToastProvider";
import { handleSocialQueryError } from "../utils/handleSocialQueryError";
import { mapSocialWriteError } from "../utils/socialWriteErrors";
import {
  computeOptimisticFollow,
  patchProfileCache,
  reconcileFollowSuccess,
  restoreProfileCache,
  snapshotProfileCache,
} from "../utils/profileCache";

function resolveFollowAction(profile) {
  if (!profile?.followStatus || profile.followStatus === "SELF") return null;
  if (profile.followStatus === "NONE") return "follow";
  if (profile.followStatus === "PENDING" || profile.followStatus === "ACCEPTED") {
    return "unfollow";
  }
  return null;
}

export function useFollowUser(userId) {
  const queryClient = useQueryClient();
  const { showToast } = useSocialToast();

  const mutation = useMutation({
    mutationFn: async ({ action }) => {
      if (action === "follow") return followUser(userId);
      return unfollowUser(userId);
    },
    onMutate: async ({ action }) => {
      await queryClient.cancelQueries({ queryKey: profileKeys.detail(userId) });
      const snapshot = snapshotProfileCache(queryClient, userId);
      const profile = snapshot;
      const optimistic = computeOptimisticFollow(profile, action);
      patchProfileCache(queryClient, userId, optimistic);
      return { snapshot, action, profile };
    },
    onError: async (error, _vars, context) => {
      if (context?.snapshot !== undefined) {
        restoreProfileCache(queryClient, userId, context.snapshot);
      }

      const mapped = mapSocialWriteError(error);
      if (mapped.type === "session") {
        await handleSocialQueryError({ code: 401, message: mapped.message });
        return;
      }

      if (mapped.type !== "suspended") {
        showToast(mapped.message || "Không cập nhật được trạng thái theo dõi.", "error");
      }
    },
    onSuccess: (data, { action }, context) => {
      const patch = reconcileFollowSuccess(context?.profile, data, action);
      patchProfileCache(queryClient, userId, patch);

      if (action === "follow") {
        const message =
          data?.status === "PENDING" ? "Đã gửi yêu cầu theo dõi." : "Đã theo dõi.";
        showToast(message);
      } else {
        showToast("Đã hủy theo dõi.");
      }

      if (patch.canViewFullProfile) {
        queryClient.invalidateQueries({
          queryKey: [...profileKeys.all, "posts", userId],
        });
      } else {
        queryClient.removeQueries({
          queryKey: [...profileKeys.all, "posts", userId],
        });
      }

      queryClient.invalidateQueries({
        queryKey: [...profileKeys.all, "relations", userId],
      });
    },
  });

  const toggleFollow = (profile) => {
    if (!userId || !profile || mutation.isPending) return;
    const action = resolveFollowAction(profile);
    if (!action) return;
    mutation.mutate({ action });
  };

  return {
    toggleFollow,
    isFollowLoading: mutation.isPending,
  };
}
