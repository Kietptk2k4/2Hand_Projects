import { useInfiniteQuery } from "@tanstack/react-query";
import { searchPosts } from "../api/searchPostsApi";
import { discoveryKeys } from "../api/discoveryKeys";
import { SEARCH_POSTS_PAGE_SIZE } from "../constants/searchPostsConstants";
import { addSearchHistory } from "../utils/searchHistoryStorage";
import { handleSocialQueryError } from "../utils/handleSocialQueryError";

export function useSearchPosts(debouncedQuery) {
  const q = (debouncedQuery || "").trim();

  const query = useInfiniteQuery({
    queryKey: discoveryKeys.searchPosts(q),
    queryFn: async ({ pageParam = 0 }) => {
      try {
        const data = await searchPosts({ q, page: pageParam, size: SEARCH_POSTS_PAGE_SIZE });
        if (pageParam === 0 && q) {
          await addSearchHistory(data?.keyword || q);
        }
        return data;
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
    enabled: q.length > 0,
  });

  const items = query.data?.pages.flatMap((page) => page?.items || []) ?? [];
  const meta = query.data?.pages.at(-1)?.meta ?? null;
  const keyword = query.data?.pages?.[0]?.keyword || q;

  return {
    q,
    keyword,
    items,
    meta,
    totalElements: meta?.totalElements ?? 0,
    errorMessage: query.error?.message || "",
    isInitialLoading: query.isLoading && q.length > 0,
    isLoadingMore: query.isFetchingNextPage,
    isRefreshing: query.isRefetching && !query.isFetchingNextPage,
    hasNext: Boolean(meta?.hasNext),
    loadMore: query.fetchNextPage,
    retry: query.refetch,
    refetch: query.refetch,
  };
}
