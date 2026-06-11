import { useInfiniteQuery } from "@tanstack/react-query";
import { fetchUserPosts } from "../api/userPostsApi";
import { profileKeys } from "../api/profileKeys";
import { PROFILE_POSTS_PAGE_SIZE } from "../constants/profileConstants";
import { handleSocialQueryError } from "../utils/handleSocialQueryError";

export function useProfilePosts(userId, { enabled = true, statusFilter = "published" } = {}) {
  const query = useInfiniteQuery({
    queryKey: profileKeys.posts(userId, statusFilter),
    queryFn: async ({ pageParam = 0 }) => {
      try {
        return await fetchUserPosts(userId, {
          page: pageParam,
          size: PROFILE_POSTS_PAGE_SIZE,
          statusFilter,
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
    enabled: Boolean(userId) && enabled,
  });

  const items = query.data?.pages.flatMap((page) => page?.items || []) ?? [];
  const meta = query.data?.pages.at(-1)?.meta ?? null;

  return {
    items,
    meta,
    errorMessage: query.error?.message || "",
    errorCode: query.error?.code ?? null,
    isInitialLoading: query.isLoading,
    isLoadingMore: query.isFetchingNextPage,
    isRefreshing: query.isRefetching && !query.isFetchingNextPage,
    hasNext: Boolean(meta?.hasNext),
    loadMore: query.fetchNextPage,
    retry: query.refetch,
    refetch: query.refetch,
  };
}
