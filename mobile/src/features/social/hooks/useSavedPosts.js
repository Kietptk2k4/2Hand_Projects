import { useCallback } from "react";
import { useInfiniteQuery, useQueryClient } from "@tanstack/react-query";
import { fetchSavedPosts } from "../api/savedPostsApi";
import { discoveryKeys } from "../api/discoveryKeys";
import { SAVED_POSTS_PAGE_SIZE } from "../constants/discoveryConstants";
import { handleSocialQueryError } from "../utils/handleSocialQueryError";
import { removeUnsavedPostFromSavedList } from "../utils/listPostCache";

export function useSavedPosts() {
  const queryClient = useQueryClient();

  const query = useInfiniteQuery({
    queryKey: discoveryKeys.savedPosts,
    queryFn: async ({ pageParam = 0 }) => {
      try {
        return await fetchSavedPosts({ page: pageParam, size: SAVED_POSTS_PAGE_SIZE });
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

  const items = query.data?.pages.flatMap((page) => page?.items || []) ?? [];
  const meta = query.data?.pages.at(-1)?.meta ?? null;

  const removeItem = useCallback(
    (postId) => {
      removeUnsavedPostFromSavedList(queryClient, postId);
    },
    [queryClient]
  );

  return {
    items,
    meta,
    errorMessage: query.error?.message || "",
    isInitialLoading: query.isLoading,
    isLoadingMore: query.isFetchingNextPage,
    isRefreshing: query.isRefetching && !query.isFetchingNextPage,
    hasNext: Boolean(meta?.hasNext),
    loadMore: query.fetchNextPage,
    retry: query.refetch,
    refetch: query.refetch,
    removeItem,
  };
}
