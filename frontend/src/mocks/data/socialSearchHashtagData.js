import { MOCK_FOLLOWEE_IDS, mockGlobalFeedPosts } from "./socialFeedData";

function normalizeHashtag(raw) {
  return String(raw || "")
    .replace(/^#+/, "")
    .trim();
}

function isVisibleToViewer(post) {
  if ((post.status || "ACTIVE") === "DELETED") return false;
  if (post.visibility === "PUBLIC") return true;
  if (post.visibility === "FOLLOWERS") {
    return MOCK_FOLLOWEE_IDS.includes(post.authorId);
  }
  return false;
}

function postHasExactHashtag(post, normalizedTag) {
  const target = normalizedTag.toLowerCase();
  return (post.hashtags || []).some((tag) => {
    const value = String(tag).replace(/^#+/, "").trim().toLowerCase();
    return value === target;
  });
}

function toHashtagItem(post) {
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
    createdAt: post.createdAt,
    updatedAt: post.updatedAt,
  };
}

export function buildHashtagPostsPage(hashtag, { page, size }) {
  const normalized = normalizeHashtag(hashtag);
  const items = mockGlobalFeedPosts
    .filter(isVisibleToViewer)
    .filter((post) => postHasExactHashtag(post, normalized))
    .map(toHashtagItem);

  const totalElements = items.length;
  const totalPages = totalElements === 0 ? 0 : Math.ceil(totalElements / size);
  const start = page * size;
  const slice = items.slice(start, start + size);

  return {
    hashtag: normalized,
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
