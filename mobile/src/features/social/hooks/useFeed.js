import { useInfiniteQuery } from "@tanstack/react-query";
import { router } from "expo-router";
import { fetchFollowingFeed, fetchGlobalFeed } from "../api/feedApi";
import { feedKeys } from "../api/feedKeys";
import { FEED_TABS, FEED_PAGE_SIZE } from "../constants/feedTabs";
import { clearSessionTokens } from "../../../services/auth/tokenStorage";
import { ROUTES } from "../../../shared/constants/routes";
import { setSessionExpiredMessage } from "../../auth/utils/authNavigationState";

const FETCH_BY_TAB = {
  [FEED_TABS.GLOBAL]: fetchGlobalFeed,
  [FEED_TABS.FOLLOWING]: fetchFollowingFeed,
};

async function handleSessionExpired(message) {
  setSessionExpiredMessage(message);
  await clearSessionTokens();
  router.replace(ROUTES.sessionExpired);
}

export function useFeed(activeTab) {
  const query = useInfiniteQuery({
    queryKey: feedKeys.tab(activeTab),
    queryFn: async ({ pageParam = 0 }) => {
      const fetchFn = FETCH_BY_TAB[activeTab];
      if (!fetchFn) {
        return { items: [], meta: null };
      }

      try {
        return await fetchFn({ page: pageParam, size: FEED_PAGE_SIZE });
      } catch (error) {
        if (error?.code === 401) {
          await handleSessionExpired(error?.message);
        }
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

  return {
    items,
    meta,
    errorMessage: query.error?.message || "",
    isInitialLoading: query.isLoading,
    isLoadingMore: query.isFetchingNextPage,
    hasNext: Boolean(meta?.hasNext),
    loadMore: query.fetchNextPage,
    retry: query.refetch,
  };
}
