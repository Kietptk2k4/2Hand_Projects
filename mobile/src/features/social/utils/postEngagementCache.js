import { feedKeys } from "../api/feedKeys";
import { postKeys } from "../api/postKeys";
import { patchDiscoveryPostLists, removePostFromDiscoveryLists } from "./listPostCache";

function patchFeedInfiniteData(old, postId, patch) {
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

export function patchPostInFeedCaches(queryClient, postId, patch) {
  queryClient.setQueriesData({ queryKey: feedKeys.all }, (old) =>
    patchFeedInfiniteData(old, postId, patch)
  );
}

export function patchPostDetailCache(queryClient, postId, patch) {
  queryClient.setQueryData(postKeys.detail(postId), (old) =>
    old ? { ...old, ...patch } : old
  );
}

export function patchPostEngagement(queryClient, postId, patch) {
  patchPostInFeedCaches(queryClient, postId, patch);
  patchPostDetailCache(queryClient, postId, patch);
  patchDiscoveryPostLists(queryClient, postId, patch);
}

export function removePostFromFeedCaches(queryClient, postId) {
  removePostFromDiscoveryLists(queryClient, postId);

  queryClient.setQueriesData({ queryKey: feedKeys.all }, (old) => {
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
  });

  queryClient.removeQueries({ queryKey: postKeys.detail(postId) });
  queryClient.removeQueries({ queryKey: postKeys.comments(postId) });
}

export function snapshotEngagementCaches(queryClient, postId) {
  return {
    detail: queryClient.getQueryData(postKeys.detail(postId)),
    feeds: queryClient.getQueriesData({ queryKey: feedKeys.all }),
  };
}

export function restoreEngagementCaches(queryClient, postId, snapshot) {
  if (snapshot?.detail !== undefined) {
    queryClient.setQueryData(postKeys.detail(postId), snapshot.detail);
  }
  snapshot?.feeds?.forEach(([key, data]) => {
    queryClient.setQueryData(key, data);
  });
}

export function findPostInFeedCaches(queryClient, postId) {
  const entries = queryClient.getQueriesData({ queryKey: feedKeys.all });

  for (const [, data] of entries) {
    if (!data?.pages) continue;
    for (const page of data.pages) {
      const match = (page.items || []).find((item) => item.postId === postId);
      if (match) return match;
    }
  }

  return null;
}

export function computeOptimisticLike(current) {
  const likedByMe = !Boolean(current?.likedByMe);
  const baseCount = Number(current?.likeCount) || 0;
  const likeCount = Math.max(0, baseCount + (likedByMe ? 1 : -1));
  return { likedByMe, likeCount };
}

export function computeOptimisticSave(current) {
  return { savedByMe: !Boolean(current?.savedByMe) };
}
