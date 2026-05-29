const DEFAULT_AVATAR_BASE = "https://i.pravatar.cc/96";

export function shortAuthorId(authorId) {
  if (!authorId) return "unknown";
  return authorId.length > 8 ? `${authorId.slice(0, 8)}…` : authorId;
}

export function authorAvatarUrl(authorId) {
  if (!authorId) return `${DEFAULT_AVATAR_BASE}?u=0`;
  const seed = encodeURIComponent(authorId);
  return `${DEFAULT_AVATAR_BASE}?u=${seed}`;
}

export function authorDisplayName(authorId) {
  return `User ${shortAuthorId(authorId)}`;
}
