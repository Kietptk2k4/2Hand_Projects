import { MOCK_FOLLOWEE_IDS, mockGlobalFeedPosts, mockPostId } from "./socialFeedData";

const EMPTY_QUERY = "zzznone";

/** Stable QA matches for demo keywords */
const DEMO_KEYWORD_POST_IDS = {
  travel: [mockPostId("01"), mockPostId("05"), mockPostId("09")],
};

function postMatchesKeyword(post, keywordLower) {
  const caption = (post.caption || "").toLowerCase();
  if (caption.includes(keywordLower)) return true;
  return (post.hashtags || []).some((tag) => tag.toLowerCase().includes(keywordLower));
}

function isVisibleToViewer(post) {
  if ((post.status || "ACTIVE") === "DELETED") return false;
  if (post.visibility === "PUBLIC") return true;
  if (post.visibility === "FOLLOWERS") {
    return MOCK_FOLLOWEE_IDS.includes(post.authorId);
  }
  return false;
}

function toSearchItem(post) {
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

function resolveMatches(keyword) {
  const trimmed = keyword.trim();
  const keywordLower = trimmed.toLowerCase();

  if (keywordLower === EMPTY_QUERY) {
    return [];
  }

  const demoIds = DEMO_KEYWORD_POST_IDS[keywordLower];
  if (demoIds) {
    return demoIds
      .map((postId) => mockGlobalFeedPosts.find((post) => post.postId === postId))
      .filter(Boolean)
      .filter(isVisibleToViewer)
      .map(toSearchItem);
  }

  return mockGlobalFeedPosts
    .filter(isVisibleToViewer)
    .filter((post) => postMatchesKeyword(post, keywordLower))
    .map(toSearchItem);
}

export function buildSearchPostsPage(keyword, { page, size }) {
  const trimmed = keyword.trim();
  const items = resolveMatches(trimmed);
  const totalElements = items.length;
  const totalPages = totalElements === 0 ? 0 : Math.ceil(totalElements / size);
  const start = page * size;
  const slice = items.slice(start, start + size);

  return {
    keyword: trimmed,
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
