import { mockGlobalFeedPosts } from "./socialFeedData";
import { findFeedPost } from "./socialPostDetailData";

const ACTIVE_USER_ID = "b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1";

/** In-memory saves: userId -> [{ postId, savedAt }] */
const savesByUserId = new Map();

function seedDefaultSaves() {
  const entries = mockGlobalFeedPosts.map((post, index) => ({
    postId: post.postId,
    savedAt: `2026-05-${String(Math.max(1, 28 - index)).padStart(2, "0")}T10:00:00.000Z`,
  }));

  savesByUserId.set(ACTIVE_USER_ID, entries);
}

seedDefaultSaves();

export function getUserSaves(userId) {
  if (!savesByUserId.has(userId)) {
    savesByUserId.set(userId, []);
  }
  return savesByUserId.get(userId);
}

export function isPostSavedByUser(userId, postId) {
  return getUserSaves(userId).some((entry) => entry.postId === postId);
}

export function toggleUserSave(userId, postId) {
  const saves = getUserSaves(userId);
  const existingIndex = saves.findIndex((entry) => entry.postId === postId);

  if (existingIndex >= 0) {
    saves.splice(existingIndex, 1);
    return { postId, saved: false };
  }

  saves.unshift({
    postId,
    savedAt: new Date().toISOString(),
  });
  return { postId, saved: true };
}

function toSavedItem(post, savedAt) {
  return {
    postId: post.postId,
    authorId: post.authorId,
    caption: post.caption || "",
    media: post.media || [],
    visibility: post.visibility || "PUBLIC",
    likeCount: post.likeCount ?? 0,
    replyCount: post.replyCount ?? 0,
    hashtags: post.hashtags || [],
    allowComments: post.allowComments !== false,
    productTags: post.productTags || [],
    savedAt,
    createdAt: post.createdAt,
    updatedAt: post.updatedAt,
  };
}

export function buildSavedPostsPage(userId, { page, size }) {
  const saves = [...getUserSaves(userId)].sort(
    (a, b) => new Date(b.savedAt).getTime() - new Date(a.savedAt).getTime()
  );

  const items = [];
  for (const save of saves) {
    const post = findFeedPost(save.postId);
    if (!post) continue;
    if ((post.status || "ACTIVE") === "DELETED") continue;
    items.push(toSavedItem(post, save.savedAt));
  }

  const totalElements = items.length;
  const totalPages = totalElements === 0 ? 0 : Math.ceil(totalElements / size);
  const start = page * size;
  const slice = items.slice(start, start + size);

  return {
    items: slice,
    meta: {
      page,
      size,
      totalElements,
      totalPages,
      hasNext: start + size < totalElements,
    },
  };
}

export { ACTIVE_USER_ID };
