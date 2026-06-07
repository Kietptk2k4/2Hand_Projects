import { useCallback, useEffect, useRef, useState } from "react";
import { useParams } from "react-router-dom";
import { fetchHashtagPosts } from "../api/searchHashtagApi";
import { HASHTAG_POSTS_PAGE_SIZE } from "../constants/hashtagPostsConstants";
import {
  isValidHashtagParam,
  normalizeHashtagParam,
} from "../utils/socialHashtagRoutes";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

export function useHashtagPosts() {
  const { hashtag: rawHashtag } = useParams();
  const hashtag = normalizeHashtagParam(rawHashtag);
  const isInvalidHashtag = Boolean(rawHashtag) && !isValidHashtagParam(hashtag);

  const { showSessionExpired } = useAuthSession();
  const [page, setPage] = useState(0);
  const [items, setItems] = useState([]);
  const [resolvedHashtag, setResolvedHashtag] = useState("");
  const [meta, setMeta] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const requestIdRef = useRef(0);

  const loadPage = useCallback(
    async (targetPage, { append = false, tag = hashtag } = {}) => {
      if (!tag || !isValidHashtagParam(tag)) {
        setItems([]);
        setMeta(null);
        setResolvedHashtag("");
        setStatus("idle");
        setErrorMessage("");
        return;
      }

      const requestId = ++requestIdRef.current;
      setStatus(append ? "loadingMore" : "loading");
      setErrorMessage("");

      try {
        const data = await fetchHashtagPosts(tag, {
          page: targetPage,
          size: HASHTAG_POSTS_PAGE_SIZE,
        });
        if (requestId !== requestIdRef.current) return;

        const nextItems = data?.items || [];
        setItems((prev) => (append ? [...prev, ...nextItems] : nextItems));
        setMeta(data?.meta || null);
        setResolvedHashtag(data?.hashtag || tag);
        setPage(targetPage);
        setStatus("ready");
      } catch (error) {
        if (requestId !== requestIdRef.current) return;

        if (error?.code === 401) {
          showSessionExpired(error?.message);
          return;
        }

        setStatus("error");
        setErrorMessage(error?.message || "Không tải được bài viết theo hashtag.");
      }
    },
    [hashtag, showSessionExpired]
  );

  useEffect(() => {
    if (isInvalidHashtag) {
      setItems([]);
      setMeta(null);
      setResolvedHashtag("");
      setStatus("idle");
      setErrorMessage("");
      return;
    }
    loadPage(0, { append: false, tag: hashtag });
  }, [hashtag, isInvalidHashtag, loadPage]);

  const loadMore = useCallback(() => {
    if (status === "loadingMore" || !meta?.hasNext || !hashtag) return;
    loadPage(page + 1, { append: true });
  }, [hashtag, loadPage, meta?.hasNext, page, status]);

  const retry = useCallback(() => {
    loadPage(items.length > 0 ? page : 0, { append: false });
  }, [items.length, loadPage, page]);

  const refetch = useCallback(() => {
    loadPage(0, { append: false });
  }, [loadPage]);

  const removeItem = useCallback((targetPostId) => {
    setItems((prev) => prev.filter((item) => item.postId !== targetPostId));
    setMeta((prev) =>
      prev
        ? {
            ...prev,
            totalElements: Math.max(0, (prev.totalElements || 0) - 1),
          }
        : prev
    );
  }, []);

  const patchSaved = useCallback((targetPostId, saved) => {
    setItems((prev) =>
      prev.map((item) =>
        item.postId === targetPostId ? { ...item, savedByMe: saved } : item
      )
    );
  }, []);

  const patchLiked = useCallback((targetPostId, liked, likeCount) => {
    setItems((prev) =>
      prev.map((item) =>
        item.postId === targetPostId ? { ...item, likedByMe: liked, likeCount } : item
      )
    );
  }, []);

  return {
    hashtag,
    resolvedHashtag,
    isInvalidHashtag,
    items,
    meta,
    status,
    errorMessage,
    isInitialLoading: status === "loading" && items.length === 0,
    isLoadingMore: status === "loadingMore",
    hasNext: Boolean(meta?.hasNext),
    totalElements: meta?.totalElements ?? 0,
    loadMore,
    retry,
    refetch,
    removeItem,
    patchSaved,
    patchLiked,
  };
}
