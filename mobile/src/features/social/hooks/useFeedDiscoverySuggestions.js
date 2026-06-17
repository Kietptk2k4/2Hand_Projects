import { useCallback, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { followUser, unfollowUser } from "../api/followApi";
import { fetchSuggestedUsers } from "../api/discoveryApi";
import { discoveryKeys } from "../api/discoveryKeys";
import { FEED_DISCOVERY_SUGGESTIONS_LIMIT } from "../constants/discoveryConstants";
import { useSocialWriteBlock } from "../context/SocialWriteBlockContext";
import { useSocialToast } from "../../../shared/components/SocialToastProvider";
import { handleSocialQueryError } from "../utils/handleSocialQueryError";
import { mapSocialWriteError } from "../utils/socialWriteErrors";
import {
  followButtonLabel,
  mapSuggestedUser,
  suggestionSubtitle,
} from "../utils/mapSuggestedUser";

function mapFeedSuggestionsResponse(data) {
  return (data?.items ?? []).map(mapSuggestedUser);
}

export function useFeedDiscoverySuggestions() {
  const queryClient = useQueryClient();
  const { showToast } = useSocialToast();
  const { isWriteBlocked } = useSocialWriteBlock();
  const [loadingUserId, setLoadingUserId] = useState("");

  const query = useQuery({
    queryKey: discoveryKeys.feedSuggestions,
    queryFn: async () => {
      try {
        return await fetchSuggestedUsers({
          page: 0,
          limit: FEED_DISCOVERY_SUGGESTIONS_LIMIT,
        });
      } catch (error) {
        const handled = await handleSocialQueryError(error);
        if (handled) throw error;
        throw error;
      }
    },
  });

  const items = mapFeedSuggestionsResponse(query.data);
  const hasMore = Boolean(query.data?.meta?.hasNext);

  const followMutation = useMutation({
    mutationFn: async ({ userId, followStatus }) => {
      if (followStatus === "NONE") {
        return followUser(userId);
      }
      if (followStatus === "PENDING" || followStatus === "ACCEPTED") {
        await unfollowUser(userId);
        return { status: "NONE" };
      }
      return null;
    },
    onMutate: ({ userId }) => {
      setLoadingUserId(userId);
    },
    onSettled: () => {
      setLoadingUserId("");
    },
    onSuccess: (data, { userId, followStatus }) => {
      const nextStatus =
        followStatus === "NONE" ? data?.status || "ACCEPTED" : "NONE";
      const message =
        followStatus === "NONE"
          ? nextStatus === "PENDING"
            ? "Đã gửi yêu cầu theo dõi."
            : "Đã theo dõi."
          : "Đã hủy theo dõi.";
      showToast(message);

      queryClient.setQueryData(discoveryKeys.feedSuggestions, (old) => {
        if (!old?.items) return old;
        return {
          ...old,
          items: old.items.map((item) => {
            const id = item?.user_id ?? item?.userId;
            if (id !== userId) return item;
            return {
              ...item,
              followStatus: nextStatus,
              follow_status: nextStatus,
            };
          }),
        };
      });
    },
    onError: async (error) => {
      const mapped = mapSocialWriteError(error);
      if (mapped.type === "session") {
        await handleSocialQueryError({ code: 401, message: mapped.message });
        return;
      }
      if (mapped.type === "suspended") {
        return;
      }
      showToast(mapped.message || "Không cập nhật được trạng thái theo dõi.", "error");
    },
  });

  const toggleFollow = useCallback(
    (user) => {
      if (!user?.userId || isWriteBlocked || loadingUserId) return;
      followMutation.mutate({
        userId: user.userId,
        followStatus: user.followStatus || "NONE",
      });
    },
    [followMutation, isWriteBlocked, loadingUserId]
  );

  return {
    items,
    hasMore,
    isLoading: query.isLoading,
    isError: query.isError,
    errorMessage: query.error?.message || "",
    retry: query.refetch,
    toggleFollow,
    isFollowLoading: (userId) => loadingUserId === userId,
    followButtonLabel,
    suggestionSubtitle,
    followDisabled: isWriteBlocked,
  };
}
