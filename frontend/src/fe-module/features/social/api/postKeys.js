export const postKeys = {
  all: ["social", "post"],
  detail: (postId) => [...postKeys.all, "detail", postId],
  comments: (postId, page, size, parentCommentId) => [
    ...postKeys.all,
    "comments",
    postId,
    page,
    size,
    parentCommentId || "top",
  ],
};
