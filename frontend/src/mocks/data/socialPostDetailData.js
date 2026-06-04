import { mockUsers } from "./authData";
import { MOCK_FOLLOWEE_IDS, mockGlobalFeedPosts, mockPostId } from "./socialFeedData";
import { isPostSavedByUser } from "./socialSavedPostsData";

export const MOCK_POST_ID_SUCCESS = mockPostId("01");
export const MOCK_POST_ID_EMPTY_COMMENTS = mockPostId("06");
export const MOCK_POST_ID_FOLLOWERS_403 = mockPostId("403");
export const MOCK_POST_ID_NOT_FOUND = "000000000000000000000000";

const AUTHOR_PROFILES = {
  [MOCK_FOLLOWEE_IDS[0]]: {
    displayName: "Sarah Jenkins",
    avatarUrl: "https://i.pravatar.cc/200?img=5",
    title: "Tax Advisor at FinCorp",
  },
  [MOCK_FOLLOWEE_IDS[1]]: {
    displayName: "David Chen",
    avatarUrl: "https://i.pravatar.cc/200?img=8",
    title: "Freelance Web Developer",
  },
  "b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1": {
    displayName: "Active User",
    avatarUrl: "https://i.pravatar.cc/200?img=3",
    title: "Builder at 2Hands",
  },
  "c0000000-0000-4000-8000-000000000099": {
    displayName: "Admin User",
    avatarUrl: "https://i.pravatar.cc/200?img=12",
    title: "Platform administrator",
  },
};

export function resolveAuthor(authorId) {
  const profile = AUTHOR_PROFILES[authorId];
  if (profile) {
    return {
      userId: authorId,
      displayName: profile.displayName,
      avatarUrl: profile.avatarUrl,
    };
  }
  return {
    userId: authorId,
    displayName: "User",
    avatarUrl: `https://i.pravatar.cc/200?u=${encodeURIComponent(authorId)}`,
  };
}

const EXTRA_POSTS = [
  {
    postId: MOCK_POST_ID_FOLLOWERS_403,
    authorId: MOCK_FOLLOWEE_IDS[1],
    caption: "Noi dung chi danh cho nguoi theo doi (mock 403).",
    visibility: "FOLLOWERS",
    status: "ACTIVE",
    likeCount: 0,
    replyCount: 0,
    hashtags: [],
    allowComments: true,
    media: [],
    createdAt: "2026-05-01T10:00:00Z",
    updatedAt: "2026-05-01T10:00:00Z",
  },
];

const ALL_FEED_POSTS = [...mockGlobalFeedPosts, ...EXTRA_POSTS];

const postProductTagsByPostId = new Map();

function cloneTags(tags) {
  return (tags || []).map((item) => ({ ...item }));
}

function syncFeedPostProductTags(postId, tags) {
  const target = getMutableFeedPost(postId);
  if (target) {
    target.post.productTags = cloneTags(tags);
  }
}

export function setProductTagsForPost(postId, tags) {
  const list = cloneTags(tags);
  if (list.length > 0) {
    postProductTagsByPostId.set(postId, list);
  } else {
    postProductTagsByPostId.delete(postId);
  }
  syncFeedPostProductTags(postId, list);
}

function seedProductTagsFromFeed() {
  for (const feedPost of ALL_FEED_POSTS) {
    if (feedPost.productTags?.length) {
      postProductTagsByPostId.set(feedPost.postId, cloneTags(feedPost.productTags));
    }
  }
}

seedProductTagsFromFeed();

function getMutableFeedPost(postId) {
  const feedIndex = mockGlobalFeedPosts.findIndex((item) => item.postId === postId);
  if (feedIndex >= 0) {
    return { post: mockGlobalFeedPosts[feedIndex], list: mockGlobalFeedPosts, index: feedIndex };
  }
  const extraIndex = EXTRA_POSTS.findIndex((item) => item.postId === postId);
  if (extraIndex >= 0) {
    return { post: EXTRA_POSTS[extraIndex], list: EXTRA_POSTS, index: extraIndex };
  }
  return null;
}

export function findFeedPost(postId) {
  return ALL_FEED_POSTS.find((item) => item.postId === postId) || null;
}

export function updateFeedPostInStore(postId, patch) {
  const target = getMutableFeedPost(postId);
  if (!target) return null;

  const { post } = target;
  const now = new Date().toISOString();

  if (patch.caption !== undefined) {
    post.caption = patch.caption ?? "";
  }
  if (patch.media !== undefined) {
    post.media = [...patch.media];
  }
  if (patch.hashtags !== undefined) {
    post.hashtags = [...patch.hashtags];
  }
  if (patch.visibility !== undefined) {
    post.visibility = patch.visibility;
  }
  if (patch.allowComments !== undefined) {
    post.allowComments = patch.allowComments;
  }
  if (patch.productTags !== undefined) {
    setProductTagsForPost(postId, patch.productTags);
  }

  post.updatedAt = now;
  return post;
}

export function touchFeedPostUpdatedAt(postId) {
  const target = getMutableFeedPost(postId);
  if (!target) return null;
  target.post.updatedAt = new Date().toISOString();
  return target.post;
}

export function incrementPostReplyCount(postId, delta = 1) {
  const target = getMutableFeedPost(postId);
  if (!target) return null;
  target.post.replyCount = (target.post.replyCount || 0) + delta;
  return target.post.replyCount;
}

export function decrementPostReplyCount(postId, delta = 1) {
  const target = getMutableFeedPost(postId);
  if (!target) return null;
  target.post.replyCount = Math.max(0, (target.post.replyCount || 0) - delta);
  return target.post.replyCount;
}

export function softDeletePost(postId, actorUserId) {
  const feedPost = findFeedPost(postId);
  if (!feedPost) {
    return { ok: false, status: 404 };
  }

  if (feedPost.authorId !== actorUserId) {
    return { ok: false, status: 403 };
  }

  const now = new Date().toISOString();
  if (!feedPost.deletedAt) {
    feedPost.deletedAt = now;
  }
  feedPost.status = "DELETED";
  feedPost.updatedAt = now;

  return {
    ok: true,
    data: {
      postId: feedPost.postId,
      status: "DELETED",
      deletedAt: feedPost.deletedAt,
      updatedAt: feedPost.updatedAt,
    },
  };
}

export function buildPostDetail(postId, viewerUserId) {
  const feedPost = findFeedPost(postId);
  if (!feedPost) return null;

  const author = resolveAuthor(feedPost.authorId);
  const isOwner = viewerUserId === feedPost.authorId;

  const productTags =
    feedPost.productTags?.length > 0
      ? cloneTags(feedPost.productTags)
      : postProductTagsByPostId.get(postId) || [];

  return {
    postId: feedPost.postId,
    author,
    caption: feedPost.caption,
    media: feedPost.media || [],
    productTags,
    visibility: feedPost.visibility,
    status: feedPost.status || "ACTIVE",
    likeCount: feedPost.likeCount,
    replyCount: feedPost.replyCount,
    hashtags: feedPost.hashtags || [],
    allowComments: feedPost.allowComments !== false,
    likedByMe: false,
    savedByMe: viewerUserId ? isPostSavedByUser(viewerUserId, postId) : false,
    isOwner,
    createdAt: feedPost.createdAt,
    updatedAt: feedPost.updatedAt,
  };
}

export function canViewerSeePost(post, viewerUserId) {
  if (!post) return { allowed: false, status: 404 };
  if (post.postId === MOCK_POST_ID_NOT_FOUND) return { allowed: false, status: 404 };

  const status = post.status || "ACTIVE";
  if (status === "DELETED") return { allowed: false, status: 404 };

  if (post.visibility === "FOLLOWERS") {
    const isOwner = viewerUserId === post.authorId;
    if (isOwner) return { allowed: true };
    if (post.postId === MOCK_POST_ID_FOLLOWERS_403) {
      return { allowed: false, status: 403 };
    }
    if (MOCK_FOLLOWEE_IDS.includes(post.authorId)) {
      return { allowed: true };
    }
    return { allowed: false, status: 403 };
  }

  if (post.visibility === "PUBLIC" && status === "ACTIVE") {
    return { allowed: true };
  }

  return { allowed: true };
}
