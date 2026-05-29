import { mockUsers } from "./authData";
import { MOCK_FOLLOWEE_IDS, mockGlobalFeedPosts, mockPostId } from "./socialFeedData";

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

export function findFeedPost(postId) {
  return ALL_FEED_POSTS.find((item) => item.postId === postId) || null;
}

export function buildPostDetail(postId, viewerUserId) {
  const feedPost = findFeedPost(postId);
  if (!feedPost) return null;

  const author = resolveAuthor(feedPost.authorId);
  const isOwner = viewerUserId === feedPost.authorId;

  const productTags =
    postId === MOCK_POST_ID_SUCCESS
      ? [{ productId: "a1b2c3d4-e5f6-7890-abcd-ef1234567890", price: 500000 }]
      : [];

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
    savedByMe: postId === MOCK_POST_ID_SUCCESS,
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
