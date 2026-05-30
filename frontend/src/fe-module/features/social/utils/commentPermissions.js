export function isModeratorUser(user) {
  if (!user) return false;
  if (user.is_admin) return true;
  const roles = user.roles || [];
  return roles.includes("MODERATOR") || roles.includes("ADMIN");
}

export function canDeleteCommentItem(comment, user) {
  if (!comment?.author?.userId || !user?.id) {
    return isModeratorUser(user);
  }
  const isOwner = comment.author.userId === user.id;
  return isOwner || isModeratorUser(user);
}
