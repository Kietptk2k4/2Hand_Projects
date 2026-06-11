import { discoveryKeys } from "../api/discoveryKeys";

function patchInfinitePostData(old, postId, patch) {
  if (!old?.pages) return old;

  return {
    ...old,
    pages: old.pages.map((page) => ({
      ...page,
      items: (page.items || []).map((item) =>
        item.postId === postId ? { ...item, ...patch } : item
      ),
    })),
  };
}

function removeFromInfinitePostData(old, postId) {
  if (!old?.pages) return old;

  return {
    ...old,
    pages: old.pages.map((page) => ({
      ...page,
      items: (page.items || []).filter((item) => item.postId !== postId),
      meta: page.meta
        ? {
            ...page.meta,
            totalElements: Math.max(0, (page.meta.totalElements || 0) - 1),
          }
        : page.meta,
    })),
  };
}

export function patchDiscoveryPostLists(queryClient, postId, patch) {
  queryClient.setQueriesData({ queryKey: discoveryKeys.savedPosts }, (old) =>
    patchInfinitePostData(old, postId, patch)
  );
  queryClient.setQueriesData({ queryKey: ["social", "discovery", "search-posts"] }, (old) =>
    patchInfinitePostData(old, postId, patch)
  );
  queryClient.setQueriesData({ queryKey: ["social", "discovery", "hashtag-posts"] }, (old) =>
    patchInfinitePostData(old, postId, patch)
  );
}

export function removePostFromDiscoveryLists(queryClient, postId) {
  queryClient.setQueriesData({ queryKey: discoveryKeys.savedPosts }, (old) =>
    removeFromInfinitePostData(old, postId)
  );
  queryClient.setQueriesData({ queryKey: ["social", "discovery", "search-posts"] }, (old) =>
    removeFromInfinitePostData(old, postId)
  );
  queryClient.setQueriesData({ queryKey: ["social", "discovery", "hashtag-posts"] }, (old) =>
    removeFromInfinitePostData(old, postId)
  );
}

export function removeUnsavedPostFromSavedList(queryClient, postId) {
  queryClient.setQueriesData({ queryKey: discoveryKeys.savedPosts }, (old) =>
    removeFromInfinitePostData(old, postId)
  );
}
