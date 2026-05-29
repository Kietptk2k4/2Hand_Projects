import { useCallback, useEffect, useState } from "react";
import { fetchPostComments } from "../api/postApi";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

export function usePostComments(postId, enabled) {
  const { showSessionExpired } = useAuthSession();
  const [comments, setComments] = useState([]);
  const [meta, setMeta] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [repliesByParent, setRepliesByParent] = useState({});
  const [replyStatusByParent, setReplyStatusByParent] = useState({});

  const loadTopLevel = useCallback(async () => {
    if (!postId || !enabled) return;

    setStatus("loading");
    setErrorMessage("");
    setRepliesByParent({});
    setReplyStatusByParent({});

    try {
      const data = await fetchPostComments(postId, { page: 0, size: 20 });
      setComments(data?.items || []);
      setMeta(data?.meta || null);
      setStatus("ready");
    } catch (error) {
      if (error?.code === 401) {
        showSessionExpired(error?.message);
        return;
      }
      setStatus("error");
      setErrorMessage(error?.message || "Không tải được bình luận.");
    }
  }, [enabled, postId, showSessionExpired]);

  useEffect(() => {
    if (!enabled) {
      setComments([]);
      setMeta(null);
      setStatus("idle");
      return;
    }
    loadTopLevel();
  }, [enabled, loadTopLevel]);

  const loadMore = useCallback(async () => {
    if (!postId || !meta?.hasNext || status === "loadingMore") return;

    setStatus("loadingMore");
    try {
      const nextPage = (meta?.page || 0) + 1;
      const data = await fetchPostComments(postId, { page: nextPage, size: meta?.size || 20 });
      setComments((prev) => [...prev, ...(data?.items || [])]);
      setMeta(data?.meta || null);
      setStatus("ready");
    } catch (error) {
      if (error?.code === 401) {
        showSessionExpired(error?.message);
        return;
      }
      setErrorMessage(error?.message || "Không tải thêm bình luận.");
      setStatus("ready");
    }
  }, [meta, postId, showSessionExpired, status]);

  const expandReplies = useCallback(
    async (parentCommentId) => {
      if (!postId || repliesByParent[parentCommentId] !== undefined) return;

      setReplyStatusByParent((prev) => ({ ...prev, [parentCommentId]: "loading" }));

      try {
        const data = await fetchPostComments(postId, {
          page: 0,
          size: 20,
          parentCommentId,
        });
        setRepliesByParent((prev) => ({
          ...prev,
          [parentCommentId]: data?.items || [],
        }));
        setReplyStatusByParent((prev) => ({ ...prev, [parentCommentId]: "ready" }));
      } catch (error) {
        if (error?.code === 401) {
          showSessionExpired(error?.message);
          return;
        }
        setReplyStatusByParent((prev) => ({ ...prev, [parentCommentId]: "error" }));
      }
    },
    [postId, repliesByParent, showSessionExpired]
  );

  return {
    comments,
    meta,
    status,
    errorMessage,
    repliesByParent,
    replyStatusByParent,
    isLoading: status === "loading",
    isLoadingMore: status === "loadingMore",
    isEmpty: status === "ready" && comments.length === 0,
    hasNext: Boolean(meta?.hasNext),
    loadMore,
    expandReplies,
    retry: loadTopLevel,
  };
}
