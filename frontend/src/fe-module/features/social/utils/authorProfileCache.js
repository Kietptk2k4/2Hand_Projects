const authorCache = new Map();

export function getCachedAuthorProfile(authorId) {
  if (!authorId) return null;
  return authorCache.get(authorId) ?? null;
}

export function setCachedAuthorProfile(authorId, profile) {
  if (!authorId || !profile) return;
  authorCache.set(authorId, profile);
}