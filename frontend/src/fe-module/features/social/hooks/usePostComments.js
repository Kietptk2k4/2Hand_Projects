import { useCallback, useEffect, useState } from "react";
import { createCommentReply, createPostComment, deleteOwnComment } from "../api/commentApi";
import { canDeleteCommentItem } from "../utils/commentPermissions";
import { fetchPostComments } from "../api/postApi";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { buildAuthorFromSessionUser, mapApiCommentToListItem } from "../utils/mapCommentItem";
import { validateCommentContent } from "../utils/validateCommentContent";

function mapSubmitError(error) {
  if (error?.code === 401) {
    return { type: "session", message: error?.message };
  }
  if (error?.code === 400 || String(error?.code).includes("400")) {
    return { type: "validation", message: error?.message || "Nội dung không hợp lệ." };
  }
  if (error?.code === 403 || String(error?.code).includes("403")) {
    return { type: "forbidden", message: error?.message || "Không thể bình luận." };
  }
  if (error?.code === 404) {
    return { type: "notFound", message: error?.message || "Không tìm thấy nội dung." };
  }
  return { type: "generic", message: error?.message || "Không gửi được bình luận. Vui lòng thử lại." };
}

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

  const loadTopLevel = useCallback(async () => {
    if (!postId || !enabled) return;

    setStatus("loading");
    setErrorMessage("");
    setRepliesByParent({});
    setReplyStatusByParent({});
    setReplyingToId(null);
    setSubmitError("");

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
      setReplyingToId(null);
      setSubmitError("");
      return;
    }
    loadTopLevel();
  }, [enabled, loadTopLevel, postId]);

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
        const author = buildAuthorFromSessionUser(user);
        const listItem = mapApiCommentToListItem(data, author);
        setComments((prev) => [listItem, ...prev]);
        onReplyCountChange?.(1);
        return { ok: true };
      } catch (error) {
        const mapped = mapSubmitError(error);
        if (mapped.type === "session") {
          showSessionExpired(mapped.message);
          return { ok: false };
        }
        setSubmitError(mapped.message);
        return { ok: false };
      } finally {
        setIsSubmittingTopLevel(false);
      }
    },
    [isSubmittingTopLevel, onReplyCountChange, postId, showSessionExpired, user]
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
        const author = buildAuthorFromSessionUser(user);
        const listItem = mapApiCommentToListItem(
          { ...data, parentCommentId },
          author
        );

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
        const mapped = mapSubmitError(error);
        if (mapped.type === "session") {
          showSessionExpired(mapped.message);
          return { ok: false };
        }
        setSubmitError(mapped.message);
        return { ok: false };
      } finally {
        setIsSubmittingReplyId(null);
      }
    },
    [isSubmittingReplyId, onReplyCountChange, showSessionExpired, user]
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

      const confirmed = window.confirm("Bạn có chắc muốn xóa bình luận này?");
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
        if (error?.code === 401) {
          showSessionExpired(error?.message);
          return { ok: false };
        }

        if (error?.code === 404) {
          removeCommentFromState(commentId, parentCommentId);
          onReplyCountChange?.(-1);
          return { ok: true, notFound: true, message: error?.message };
        }

        return {
          ok: false,
          message: error?.message || "Không xóa được bình luận.",
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
  };
}
