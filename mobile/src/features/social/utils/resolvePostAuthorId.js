export function resolvePostAuthorId(post) {
  if (!post) return null;
  return post.authorId || post.author?.userId || null;
}

export function resolvePostIsOwner(post, currentUserId) {
  if (!post) return false;
  if (typeof post.isOwner === "boolean") return post.isOwner;
  const authorId = resolvePostAuthorId(post);
  return Boolean(currentUserId && authorId && currentUserId === authorId);
}
