export const postKeys = {
  all: ["social", "post"],
  detail: (postId) => [...postKeys.all, "detail", postId],
  comments: (postId, sort = "created_at_asc") => [
    ...postKeys.all,
    "comments",
    postId,
    sort,
  ],
  replies: (postId, parentCommentId, sort = "created_at_asc") => [
    ...postKeys.all,
    "replies",
    postId,
    parentCommentId,
    sort,
  ],
};
