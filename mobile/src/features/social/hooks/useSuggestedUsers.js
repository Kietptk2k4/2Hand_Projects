import { useMutation, useQueryClient, useInfiniteQuery } from "@tanstack/react-query";
import { followUser, unfollowUser } from "../api/followApi";
import { fetchSuggestedUsers } from "../api/discoveryApi";
import { discoveryKeys } from "../api/discoveryKeys";
import { profileKeys } from "../api/profileKeys";
import { SUGGESTED_USERS_PAGE_SIZE } from "../constants/discoveryConstants";
import { useSocialWriteBlock } from "../context/SocialWriteBlockContext";
import { useSocialToast } from "../../../shared/components/SocialToastProvider";
import { handleSocialQueryError } from "../utils/handleSocialQueryError";
import { mapSocialWriteError } from "../utils/socialWriteErrors";
import {
  followButtonLabel,
  mapSuggestedUser,
  suggestionSubtitle,
} from "../utils/mapSuggestedUser";
import { patchProfileCache, reconcileFollowSuccess } from "../utils/profileCache";

function patchSuggestedUserFollowStatus(queryClient, userId, followStatus) {
  queryClient.setQueriesData({ queryKey: discoveryKeys.suggestedUsers }, (old) => {
    if (!old?.pages) return old;
    return {
      ...old,
      pages: old.pages.map((page) => ({
        ...page,
        items: (page.items || []).map((item) => {
          const id = item?.user_id ?? item?.userId;
          if (id !== userId) return item;
          return { ...item, followStatus, follow_status: followStatus };
        }),
      })),
    };
  });
}

export function useSuggestedUsers() {
  const queryClient = useQueryClient();
  const { showToast } = useSocialToast();
  const { isWriteBlocked, suspendMessage } = useSocialWriteBlock();

  const listQuery = useInfiniteQuery({
    queryKey: discoveryKeys.suggestedUsers,
    queryFn: async ({ pageParam = 0 }) => {
      try {
        return await fetchSuggestedUsers({
          page: pageParam,
          limit: SUGGESTED_USERS_PAGE_SIZE,
        });
      } catch (error) {
        const handled = await handleSocialQueryError(error);
        if (handled) throw error;
        throw error;
      }
    },
    initialPageParam: 0,
    getNextPageParam: (lastPage) => {
      if (!lastPage?.meta?.hasNext) return undefined;
      return (lastPage.meta.page ?? 0) + 1;
    },
  });

  const followMutation = useMutation({
    mutationFn: async ({ userId, action }) => {
      if (action === "follow") return followUser(userId);
      return unfollowUser(userId);
    },
    onMutate: async ({ userId, action, previousStatus }) => {
      await queryClient.cancelQueries({ queryKey: discoveryKeys.suggestedUsers });
      const nextStatus = action === "follow" ? "ACCEPTED" : "NONE";
      patchSuggestedUserFollowStatus(queryClient, userId, nextStatus);
      return { userId, action, previousStatus };
    },
    onError: async (error, { userId, previousStatus }, context) => {
      if (context?.previousStatus) {
        patchSuggestedUserFollowStatus(queryClient, userId, context.previousStatus);
      }

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
    onSuccess: (data, { userId, action }) => {
      const profile = queryClient.getQueryData(profileKeys.detail(userId));
      const patch = reconcileFollowSuccess(profile, data, action);
      patchProfileCache(queryClient, userId, patch);

      const nextStatus =
        action === "follow" ? data?.status || patch.followStatus || "ACCEPTED" : "NONE";
      patchSuggestedUserFollowStatus(queryClient, userId, nextStatus);

      if (action === "follow") {
        showToast(
          data?.status === "PENDING" ? "Đã gửi yêu cầu theo dõi." : "Đã theo dõi."
        );
      } else {
        showToast("Đã hủy theo dõi.");
      }

      queryClient.invalidateQueries({
        queryKey: [...profileKeys.all, "relations", userId],
      });
    },
  });

  const rawItems = listQuery.data?.pages.flatMap((page) => page?.items || []) ?? [];
  const items = rawItems.map(mapSuggestedUser).filter((item) => item.userId);
  const meta = listQuery.data?.pages.at(-1)?.meta ?? null;

  const toggleFollow = (user) => {
    if (!user?.userId || followMutation.isPending || isWriteBlocked) return;
    if (user.followStatus === "SELF") return;

    const action =
      user.followStatus === "NONE"
        ? "follow"
        : user.followStatus === "PENDING" || user.followStatus === "ACCEPTED"
          ? "unfollow"
          : null;

    if (!action) return;

    followMutation.mutate({
      userId: user.userId,
      action,
      previousStatus: user.followStatus,
    });
  };

  return {
    items,
    meta,
    errorMessage: listQuery.error?.message || "",
    isInitialLoading: listQuery.isLoading,
    isLoadingMore: listQuery.isFetchingNextPage,
    isRefreshing: listQuery.isRefetching && !listQuery.isFetchingNextPage,
    hasNext: Boolean(meta?.hasNext),
    loadMore: listQuery.fetchNextPage,
    retry: listQuery.refetch,
    refetch: listQuery.refetch,
    toggleFollow,
    isFollowLoading: (userId) =>
      followMutation.isPending && followMutation.variables?.userId === userId,
    followButtonLabel,
    suggestionSubtitle,
    followDisabled: isWriteBlocked,
    followDisabledTitle: suspendMessage,
  };
}
