import { useMemo, useState } from "react";
import { useInfiniteQuery } from "@tanstack/react-query";
import { fetchUserRelations } from "../api/relationsApi";
import { profileKeys } from "../api/profileKeys";
import { RELATIONS_PAGE_SIZE } from "../constants/profileConstants";
import { DEFAULT_USER_DISPLAY_NAME } from "../constants/socialUiStrings";
import { handleSocialQueryError } from "../utils/handleSocialQueryError";

export function useFollowList(userId, relationType, { enabled = true } = {}) {
  const [searchQuery, setSearchQuery] = useState("");

  const query = useInfiniteQuery({
    queryKey: profileKeys.relations(userId, relationType),
    queryFn: async ({ pageParam = 0 }) => {
      try {
        return await fetchUserRelations(userId, {
          type: relationType,
          page: pageParam,
          size: RELATIONS_PAGE_SIZE,
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
    enabled: Boolean(userId) && Boolean(relationType) && enabled,
  });

  const items = query.data?.pages.flatMap((page) => page?.items || []) ?? [];
  const meta = query.data?.pages.at(-1)?.meta ?? null;

  const filteredItems = useMemo(() => {
    const queryText = searchQuery.trim().toLowerCase();
    if (!queryText) return items;
    return items.filter((item) =>
      (item.displayName || DEFAULT_USER_DISPLAY_NAME).toLowerCase().includes(queryText)
    );
  }, [items, searchQuery]);

  return {
    items: filteredItems,
    rawItems: items,
    meta,
    searchQuery,
    setSearchQuery,
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

export function useFollowers(userId, options) {
  return useFollowList(userId, "followers", options);
}

export function useFollowing(userId, options) {
  return useFollowList(userId, "following", options);
}
