import { useCallback, useEffect, useState } from "react";
import { createCommentReply, createPostComment, deleteOwnComment } from "../api/commentApi";
import { canDeleteCommentItem } from "../utils/commentPermissions";
import { fetchPostComments } from "../api/postApi";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { mapApiCommentToListItem } from "../utils/mapCommentItem";
import { DEFAULT_COMMENT_SORT } from "../constants/commentConstants";
import { validateCommentContent } from "../utils/validateCommentContent";
import { mapSocialWriteError } from "../utils/socialWriteErrors";

export function usePostComments(postId, enabled, { onReplyCountChange } = {}) {
  const { user, showSessionExpired } = useAuthSession();
  const [comments, setComments] = useState([]);
  const [meta, setMeta] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [repliesByParent, setRepliesByParent] = useState({});
  const [replyStatusByParent, setReplyStatusByParent] = useState({});
  const [replyingToId, setReplyingToId] = useState(null);
  const [isSubmittingTopLevel, setIsSubmittingTopLevel] = useState(false);
  const [isSubmittingReplyId, setIsSubmittingReplyId] = useState(null);
  const [submitError, setSubmitError] = useState("");
  const [deletingCommentId, setDeletingCommentId] = useState(null);
  const [commentSort, setCommentSort] = useState(DEFAULT_COMMENT_SORT);

  const loadTopLevel = useCallback(async () => {
    if (!postId || !enabled) return;

    setStatus("loading");
    setErrorMessage("");
    setRepliesByParent({});
    setReplyStatusByParent({});
    setReplyingToId(null);
    setSubmitError("");

    try {
      const data = await fetchPostComments(postId, { page: 0, size: 20, sort: commentSort });
      setComments(data?.items || []);
      setMeta(data?.meta || null);
      setStatus("ready");
    } catch (error) {
      if (error?.code === 401) {
        showSessionExpired(error?.message);
        return;
      }
      setStatus("error");
      setErrorMessage(error?.message || "KhÃ´ng táº£i Ä‘Æ°á»£c bÃ¬nh luáº­n.");
    }
  }, [commentSort, enabled, postId, showSessionExpired]);

  useEffect(() => {
    if (!enabled) {
      setComments([]);
      setMeta(null);
      setStatus("idle");
      setReplyingToId(null);
      setSubmitError("");
      return;
    }
    loadTopLevel();
  }, [commentSort, enabled, loadTopLevel, postId]);

  const loadMore = useCallback(async () => {
    if (!postId || !meta?.hasNext || status === "loadingMore") return;

    setStatus("loadingMore");
    try {
      const nextPage = (meta?.page || 0) + 1;
      const data = await fetchPostComments(postId, {
        page: nextPage,
        size: meta?.size || 20,
        sort: commentSort,
      });
      setComments((prev) => [...prev, ...(data?.items || [])]);
      setMeta(data?.meta || null);
      setStatus("ready");
    } catch (error) {
      if (error?.code === 401) {
        showSessionExpired(error?.message);
        return;
      }
      setErrorMessage(error?.message || "KhÃ´ng táº£i thÃªm bÃ¬nh luáº­n.");
      setStatus("ready");
    }
  }, [commentSort, meta, postId, showSessionExpired, status]);

  const expandReplies = useCallback(
    async (parentCommentId) => {
      if (!postId || repliesByParent[parentCommentId] !== undefined) return;

      setReplyStatusByParent((prev) => ({ ...prev, [parentCommentId]: "loading" }));

      try {
        const data = await fetchPostComments(postId, {
          page: 0,
          size: 20,
          parentCommentId,
          sort: commentSort,
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
    [commentSort, postId, repliesByParent, showSessionExpired]
  );

  const handleCommentSortChange = useCallback((nextSort) => {
    setCommentSort(nextSort);
  }, []);

  const startReply = useCallback((parentCommentId) => {
    setReplyingToId(parentCommentId);
    setSubmitError("");
  }, []);

  const cancelReply = useCallback(() => {
    setReplyingToId(null);
    setSubmitError("");
  }, []);

  const clearSubmitError = useCallback(() => {
    setSubmitError("");
  }, []);

  const submitTopLevel = useCallback(
    async (rawText) => {
      if (!postId || isSubmittingTopLevel) return { ok: false };

      const validation = validateCommentContent(rawText);
      if (!validation.valid) {
        setSubmitError(validation.message);
        return { ok: false };
      }

      setIsSubmittingTopLevel(true);
      setSubmitError("");

      try {
        const data = await createPostComment(postId, {
          contentText: validation.value,
          media: [],
        });
        const listItem = mapApiCommentToListItem(data);
        setComments((prev) => [listItem, ...prev]);
        onReplyCountChange?.(1);
        return { ok: true };
      } catch (error) {
        const mapped = mapSocialWriteError(error);
        if (mapped.type === "session") {
          showSessionExpired(mapped.message);
          return { ok: false };
        }
        if (mapped.type === "suspended") {
          return { ok: false };
        }
        setSubmitError(mapped.message);
        return { ok: false };
      } finally {
        setIsSubmittingTopLevel(false);
      }
    },
    [isSubmittingTopLevel, onReplyCountChange, postId, showSessionExpired]
  );

  const submitReply = useCallback(
    async (parentCommentId, rawText) => {
      if (!parentCommentId || isSubmittingReplyId) return { ok: false };

      const validation = validateCommentContent(rawText);
      if (!validation.valid) {
        setSubmitError(validation.message);
        return { ok: false };
      }

      setIsSubmittingReplyId(parentCommentId);
      setSubmitError("");

      try {
        const data = await createCommentReply(parentCommentId, {
          contentText: validation.value,
          media: [],
        });
        const listItem = mapApiCommentToListItem({
          ...data,
          parentCommentId,
        });

        setRepliesByParent((prev) => ({
          ...prev,
          [parentCommentId]: [...(prev[parentCommentId] || []), listItem],
        }));
        setReplyStatusByParent((prev) => ({ ...prev, [parentCommentId]: "ready" }));

        setComments((prev) =>
          prev.map((item) =>
            item.commentId === parentCommentId
              ? { ...item, replyCount: (item.replyCount || 0) + 1 }
              : item
          )
        );

        onReplyCountChange?.(1);
        setReplyingToId(null);
        return { ok: true };
      } catch (error) {
        const mapped = mapSocialWriteError(error);
        if (mapped.type === "session") {
          showSessionExpired(mapped.message);
          return { ok: false };
        }
        if (mapped.type === "suspended") {
          return { ok: false };
        }
        setSubmitError(mapped.message);
        return { ok: false };
      } finally {
        setIsSubmittingReplyId(null);
      }
    },
    [isSubmittingReplyId, onReplyCountChange, showSessionExpired]
  );

  const canDeleteComment = useCallback(
    (commentItem) => canDeleteCommentItem(commentItem, user),
    [user]
  );

  const removeCommentFromState = useCallback((commentId, parentCommentId) => {
    if (parentCommentId) {
      setRepliesByParent((prev) => ({
        ...prev,
        [parentCommentId]: (prev[parentCommentId] || []).filter(
          (item) => item.commentId !== commentId
        ),
      }));
      setComments((prev) =>
        prev.map((item) =>
          item.commentId === parentCommentId
            ? { ...item, replyCount: Math.max(0, (item.replyCount || 0) - 1) }
            : item
        )
      );
    } else {
      setComments((prev) => prev.filter((item) => item.commentId !== commentId));
      setRepliesByParent((prev) => {
        const next = { ...prev };
        delete next[commentId];
        return next;
      });
    }
  }, []);

  const deleteComment = useCallback(
    async (commentId, { parentCommentId = null } = {}) => {
      if (!commentId || deletingCommentId) {
        return { ok: false };
      }

      const confirmed = window.confirm("Báº¡n cÃ³ cháº¯c muá»‘n xÃ³a bÃ¬nh luáº­n nÃ y?");
      if (!confirmed) {
        return { ok: false, cancelled: true };
      }

      setDeletingCommentId(commentId);

      try {
        await deleteOwnComment(commentId);
        removeCommentFromState(commentId, parentCommentId);
        onReplyCountChange?.(-1);
        return { ok: true };
      } catch (error) {
        const mapped = mapSocialWriteError(error);
        if (mapped.type === "session") {
          showSessionExpired(mapped.message);
          return { ok: false };
        }
        if (mapped.type === "suspended") {
          return { ok: false };
        }

        if (mapped.type === "notFound" || error?.code === 404) {
          removeCommentFromState(commentId, parentCommentId);
          onReplyCountChange?.(-1);
          return { ok: true, notFound: true, message: error?.message };
        }

        return {
          ok: false,
          message: error?.message || "KhÃ´ng xÃ³a Ä‘Æ°á»£c bÃ¬nh luáº­n.",
          code: error?.code,
        };
      } finally {
        setDeletingCommentId(null);
      }
    },
    [
      deletingCommentId,
      onReplyCountChange,
      removeCommentFromState,
      showSessionExpired,
    ]
  );

  return {
    comments,
    meta,
    status,
    errorMessage,
    repliesByParent,
    replyStatusByParent,
    replyingToId,
    isSubmittingTopLevel,
    isSubmittingReplyId,
    submitError,
    isLoading: status === "loading",
    isLoadingMore: status === "loadingMore",
    isEmpty: status === "ready" && comments.length === 0,
    hasNext: Boolean(meta?.hasNext),
    loadMore,
    expandReplies,
    retry: loadTopLevel,
    loadTopLevel,
    startReply,
    cancelReply,
    clearSubmitError,
    submitTopLevel,
    submitReply,
    deleteComment,
    canDeleteComment,
    deletingCommentId,
    commentSort,
    setCommentSort: handleCommentSortChange,
  };
}
