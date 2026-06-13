import { mockUsers } from "./authData";
import { MOCK_SOCIAL_USER_IDS as FE_MOCK_USER_IDS } from "../../fe-module/features/social/constants/socialProfileConstants";
import { MOCK_FOLLOWEE_IDS, mockGlobalFeedPosts, mockPostId } from "./socialFeedData";
export const MOCK_SOCIAL_USER_IDS = FE_MOCK_USER_IDS;

const ACTIVE_USER_ID = MOCK_SOCIAL_USER_IDS.ACTIVE;
const PRIVATE_USER_ID = MOCK_SOCIAL_USER_IDS.PRIVATE;

const PROFILE_BY_USER_ID = {
  [ACTIVE_USER_ID]: {
    displayName: "Active User",
    avatarUrl: "https://i.pravatar.cc/200?img=3",
    isPrivate: false,
    followerCount: 128,
    followingCount: 42,
  },
  [MOCK_FOLLOWEE_IDS[0]]: {
    displayName: "Sarah Jenkins",
    avatarUrl: "https://i.pravatar.cc/200?img=5",
    isPrivate: false,
    followerCount: 1248,
    followingCount: 342,
  },
  [MOCK_FOLLOWEE_IDS[1]]: {
    displayName: "David Chen",
    avatarUrl: "https://i.pravatar.cc/200?img=8",
    isPrivate: false,
    followerCount: 890,
    followingCount: 210,
  },
  [PRIVATE_USER_ID]: {
    displayName: "Chloe Smith",
    avatarUrl: "https://i.pravatar.cc/200?img=45",
    isPrivate: true,
    followerCount: 56,
    followingCount: 12,
  },
  "c0000000-0000-4000-8000-000000000099": {
    displayName: "Admin User",
    avatarUrl: "https://i.pravatar.cc/200?img=12",
    isPrivate: false,
    followerCount: 500,
    followingCount: 12,
  },
  [MOCK_SOCIAL_USER_IDS.RELATIONS_EMPTY]: {
    displayName: "Empty Relations User",
    avatarUrl: "https://i.pravatar.cc/200?img=20",
    isPrivate: false,
    followerCount: 0,
    followingCount: 0,
  },
};

/** followerId -> Map(followeeId -> { status, createdAt }) */
const followRelationsByFollower = new Map();

function followKey(followerId, followeeId) {
  return `${followerId}:${followeeId}`;
}

function seedFollowStore() {
  const activeFollows = new Map();
  activeFollows.set(MOCK_FOLLOWEE_IDS[0], {
    status: "ACCEPTED",
    createdAt: "2026-05-10T08:00:00.000Z",
  });
  followRelationsByFollower.set(ACTIVE_USER_ID, activeFollows);
}

seedFollowStore();

export function getMockFollowRelation(followerId, followeeId) {
  const byFollowee = followRelationsByFollower.get(followerId);
  if (!byFollowee) return null;
  return byFollowee.get(followeeId) || null;
}

export function applyMockFollow(followerId, followeeId, status, createdAt) {
  if (!followRelationsByFollower.has(followerId)) {
    followRelationsByFollower.set(followerId, new Map());
  }
  followRelationsByFollower.get(followerId).set(followeeId, { status, createdAt });

  const followeeBase = PROFILE_BY_USER_ID[followeeId];
  const followerBase = PROFILE_BY_USER_ID[followerId];
  if (followeeBase) {
    followeeBase.followerCount = (followeeBase.followerCount || 0) + 1;
  }
  if (followerBase) {
    followerBase.followingCount = (followerBase.followingCount || 0) + 1;
  }
}

export function applyMockUnfollow(followerId, followeeId) {
  const byFollowee = followRelationsByFollower.get(followerId);
  if (!byFollowee || !byFollowee.has(followeeId)) {
    return false;
  }

  byFollowee.delete(followeeId);

  const followeeBase = PROFILE_BY_USER_ID[followeeId];
  const followerBase = PROFILE_BY_USER_ID[followerId];
  if (followeeBase) {
    followeeBase.followerCount = Math.max(0, (followeeBase.followerCount || 0) - 1);
  }
  if (followerBase) {
    followerBase.followingCount = Math.max(0, (followerBase.followingCount || 0) - 1);
  }

  return true;
}

const DRAFT_POSTS_BY_AUTHOR = {
  [ACTIVE_USER_ID]: [
    {
      postId: mockPostId("d1"),
      authorId: ACTIVE_USER_ID,
      caption: "Draft: upcoming service package preview",
      media: [
        {
          url: "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=800&q=80",
          type: "IMAGE",
        },
      ],
      visibility: "PUBLIC",
      status: "DRAFT",
      likeCount: 0,
      replyCount: 0,
      hashtags: [],
      createdAt: "2026-05-29T08:00:00Z",
      updatedAt: "2026-05-29T08:00:00Z",
    },
  ],
};

function isKnownUser(userId) {
  if (!userId) return false;
  if (userId === MOCK_SOCIAL_USER_IDS.NOT_FOUND) return false;
  return Boolean(PROFILE_BY_USER_ID[userId]) || mockUsers.some((user) => user.id === userId && user.status !== "DELETED");
}

function resolveFollowStatus(viewerId, targetId) {
  if (viewerId === targetId) return "SELF";
  const relation = getMockFollowRelation(viewerId, targetId);
  if (relation) return relation.status;
  return "NONE";
}

function canViewFullProfile(viewerId, targetId, profile, followStatus) {
  if (followStatus === "SELF") return true;
  if (!profile.isPrivate) return true;
  return followStatus === "ACCEPTED";
}

function resolveMockCoverUrl(userId, base) {
  if (base.coverUrl) return base.coverUrl;
  const authUser = mockUsers.find((user) => user.id === userId);
  return authUser?.cover_url || null;
}

export function buildSocialProfile(userId, viewerId) {
  if (!isKnownUser(userId)) return null;

  const base = PROFILE_BY_USER_ID[userId] || {
    displayName: "User",
    avatarUrl: `https://i.pravatar.cc/200?u=${encodeURIComponent(userId)}`,
    isPrivate: false,
    followerCount: 0,
    followingCount: 0,
  };

  const followStatus = resolveFollowStatus(viewerId, userId);
  const canView = canViewFullProfile(viewerId, userId, base, followStatus);
  const hideCounters = !canView;

  return {
    userId,
    displayName: base.displayName,
    avatarUrl: base.avatarUrl,
    coverUrl: resolveMockCoverUrl(userId, base),
    isPrivate: base.isPrivate,
    followerCount: hideCounters ? null : base.followerCount,
    followingCount: hideCounters ? null : base.followingCount,
    followStatus,
    canViewFullProfile: canView,
  };
}

function collectAuthorPosts(authorId) {
  const fromFeed = mockGlobalFeedPosts.filter((post) => post.authorId === authorId);
  const drafts = DRAFT_POSTS_BY_AUTHOR[authorId] || [];
  return [...fromFeed, ...drafts].sort(
    (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  );
}

function canViewerSeePost(post, viewerId, authorId, followStatus) {
  const status = post.status || "ACTIVE";
  if (status === "DELETED") return false;
  if (viewerId === authorId) return true;
  if (status === "DRAFT") return false;

  if (post.visibility === "PUBLIC") return true;
  if (post.visibility === "FOLLOWERS") {
    return followStatus === "ACCEPTED" || followStatus === "SELF";
  }
  return false;
}

function toGridItem(post) {
  return {
    postId: post.postId,
    caption: post.caption || "",
    media: post.media || [],
    visibility: post.visibility || "PUBLIC",
    likeCount: post.likeCount ?? 0,
    replyCount: post.replyCount ?? 0,
    hashtags: post.hashtags || [],
    createdAt: post.createdAt,
    status: post.status || "ACTIVE",
  };
}

export function buildUserPostsPage(userId, viewerId, { page, size, statusFilter }) {
  const profile = buildSocialProfile(userId, viewerId);
  if (!profile) return { error: 404 };
  if (!profile.canViewFullProfile) return { error: 403 };

  const isOwner = viewerId === userId;
  if (statusFilter === "all" && !isOwner) {
    return { error: 400, code: "SOCIAL-400-PAGINATION" };
  }

  let posts = collectAuthorPosts(userId);
  posts = posts.filter((post) => (post.status || "ACTIVE") !== "DELETED");

  if (statusFilter === "published") {
    posts = posts.filter((post) => (post.status || "ACTIVE") === "ACTIVE");
  }

  posts = posts.filter((post) =>
    canViewerSeePost(post, viewerId, userId, profile.followStatus)
  );

  const totalElements = posts.length;
  const totalPages = totalElements === 0 ? 0 : Math.ceil(totalElements / size);
  const start = page * size;
  const slice = posts.slice(start, start + size).map(toGridItem);

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

