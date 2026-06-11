import { useInfiniteQuery } from "@tanstack/react-query";
import { fetchHashtagPosts } from "../api/searchHashtagApi";
import { discoveryKeys } from "../api/discoveryKeys";
import { HASHTAG_POSTS_PAGE_SIZE } from "../constants/discoveryConstants";
import { handleSocialQueryError } from "../utils/handleSocialQueryError";
import { isValidHashtagParam, normalizeHashtagParam } from "../utils/normalizeHashtag";

export function useHashtagPosts(rawHashtag) {
  const hashtag = normalizeHashtagParam(rawHashtag);
  const isInvalidHashtag = Boolean(rawHashtag) && !isValidHashtagParam(hashtag);

  const query = useInfiniteQuery({
    queryKey: discoveryKeys.hashtagPosts(hashtag),
    queryFn: async ({ pageParam = 0 }) => {
      try {
        return await fetchHashtagPosts(hashtag, {
          page: pageParam,
          size: HASHTAG_POSTS_PAGE_SIZE,
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
    enabled: Boolean(hashtag) && !isInvalidHashtag,
  });

  const items = query.data?.pages.flatMap((page) => page?.items || []) ?? [];
  const meta = query.data?.pages.at(-1)?.meta ?? null;
  const resolvedHashtag = query.data?.pages?.[0]?.hashtag || hashtag;

  return {
    hashtag,
    resolvedHashtag,
    isInvalidHashtag,
    items,
    meta,
    totalElements: meta?.totalElements ?? 0,
    errorMessage: query.error?.message || "",
    isInitialLoading: query.isLoading,
    isLoadingMore: query.isFetchingNextPage,
    isRefreshing: query.isRefetching && !query.isFetchingNextPage,
    hasNext: Boolean(meta?.hasNext),
    loadMore: query.fetchNextPage,
    retry: query.refetch,
    refetch: query.refetch,
  };
}
