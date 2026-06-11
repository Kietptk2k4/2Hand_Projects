import { useCallback, useState } from "react";
import { useInfiniteQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { createCommentReply, createPostComment } from "../api/commentApi";
import { fetchPostComments } from "../api/postApi";
import { postKeys } from "../api/postKeys";
import {
  COMMENT_PAGE_SIZE,
  DEFAULT_COMMENT_SORT,
} from "../constants/commentConstants";
import { handleSocialQueryError } from "../utils/handleSocialQueryError";
import { mapApiCommentToListItem } from "../utils/mapCommentItem";
import { mapSocialWriteError } from "../utils/socialWriteErrors";
import { validateCommentContent } from "../utils/validateCommentContent";

export function usePostComments(postId, enabled, { onReplyCountChange } = {}) {
  const queryClient = useQueryClient();
  const [commentSort, setCommentSort] = useState(DEFAULT_COMMENT_SORT);
  const [repliesByParent, setRepliesByParent] = useState({});
  const [replyStatusByParent, setReplyStatusByParent] = useState({});
  const [replyingToId, setReplyingToId] = useState(null);
  const [submitError, setSubmitError] = useState("");

  const commentsQuery = useInfiniteQuery({
    queryKey: postKeys.comments(postId, commentSort),
    queryFn: async ({ pageParam = 0 }) => {
      try {
        return await fetchPostComments(postId, {
          page: pageParam,
          size: COMMENT_PAGE_SIZE,
          sort: commentSort,
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
    enabled: Boolean(postId) && enabled,
  });

  const comments =
    commentsQuery.data?.pages.flatMap((page) => page?.items || []) ?? [];
  const meta = commentsQuery.data?.pages.at(-1)?.meta ?? null;

  const refreshComments = useCallback(() => {
    return queryClient.invalidateQueries({
      queryKey: postKeys.comments(postId, commentSort),
    });
  }, [commentSort, postId, queryClient]);

  const expandReplies = useCallback(
    async (parentCommentId) => {
      if (!postId || repliesByParent[parentCommentId] !== undefined) return;

      setReplyStatusByParent((prev) => ({ ...prev, [parentCommentId]: "loading" }));

      try {
        const data = await fetchPostComments(postId, {
          page: 0,
          size: COMMENT_PAGE_SIZE,
          parentCommentId,
          sort: commentSort,
        });
        setRepliesByParent((prev) => ({
          ...prev,
          [parentCommentId]: data?.items || [],
        }));
        setReplyStatusByParent((prev) => ({ ...prev, [parentCommentId]: "ready" }));
      } catch (error) {
        const handled = await handleSocialQueryError(error);
        if (handled) return;
        setReplyStatusByParent((prev) => ({ ...prev, [parentCommentId]: "error" }));
      }
    },
    [commentSort, postId, repliesByParent]
  );

  const createCommentMutation = useMutation({
    mutationFn: async ({ contentText, parentCommentId = null }) => {
      const payload = { contentText, media: [] };
      if (parentCommentId) {
        return createCommentReply(parentCommentId, payload);
      }
      return createPostComment(postId, payload);
    },
    onSuccess: async (data, variables) => {
      if (variables.parentCommentId) {
        const listItem = mapApiCommentToListItem({
          ...data,
          parentCommentId: variables.parentCommentId,
        });
        setRepliesByParent((prev) => ({
          ...prev,
          [variables.parentCommentId]: [
            ...(prev[variables.parentCommentId] || []),
            listItem,
          ],
        }));
        setReplyStatusByParent((prev) => ({
          ...prev,
          [variables.parentCommentId]: "ready",
        }));
        setReplyingToId(null);
      }

      onReplyCountChange?.(1);
      await refreshComments();
    },
  });

  const submitComment = useCallback(
    async (rawText, { parentCommentId = null } = {}) => {
      if (!postId || createCommentMutation.isPending) return { ok: false };

      const validation = validateCommentContent(rawText);
      if (!validation.valid) {
        setSubmitError(validation.message);
        return { ok: false };
      }

      setSubmitError("");

      try {
        await createCommentMutation.mutateAsync({
          contentText: validation.value,
          parentCommentId,
        });
        return { ok: true };
      } catch (error) {
        const mapped = mapSocialWriteError(error);
        if (mapped.type === "session") {
          await handleSocialQueryError({ code: 401, message: mapped.message });
          return { ok: false };
        }
        setSubmitError(mapped.message);
        return { ok: false };
      }
    },
    [createCommentMutation, postId]
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

  const handleSortChange = useCallback((nextSort) => {
    setCommentSort(nextSort);
    setRepliesByParent({});
    setReplyStatusByParent({});
    setReplyingToId(null);
  }, []);

  const isSubmittingTopLevel =
    createCommentMutation.isPending && !createCommentMutation.variables?.parentCommentId;
  const isSubmittingReplyId = createCommentMutation.isPending
    ? createCommentMutation.variables?.parentCommentId ?? null
    : null;

  return {
    comments,
    meta,
    repliesByParent,
    replyStatusByParent,
    replyingToId,
    submitError,
    commentSort,
    setCommentSort: handleSortChange,
    isLoading: commentsQuery.isLoading,
    isLoadingMore: commentsQuery.isFetchingNextPage,
    isEmpty: commentsQuery.isSuccess && comments.length === 0,
    hasNext: Boolean(meta?.hasNext),
    errorMessage: commentsQuery.error?.message || "",
    loadMore: commentsQuery.fetchNextPage,
    retry: commentsQuery.refetch,
    refreshComments,
    expandReplies,
    startReply,
    cancelReply,
    clearSubmitError,
    submitTopLevel: (text) => submitComment(text),
    submitReply: (parentCommentId, text) =>
      submitComment(text, { parentCommentId }),
    isSubmittingTopLevel,
    isSubmittingReplyId,
  };
}
