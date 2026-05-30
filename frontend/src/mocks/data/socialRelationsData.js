import { MOCK_SOCIAL_USER_IDS } from "../../fe-module/features/social/constants/socialProfileConstants";
import { MOCK_FOLLOWEE_IDS } from "./socialFeedData";
import { buildSocialProfile } from "./socialProfileData";

export const MOCK_RELATIONS_EMPTY_USER_ID = MOCK_SOCIAL_USER_IDS.RELATIONS_EMPTY;

const ACTIVE_USER_ID = MOCK_SOCIAL_USER_IDS.ACTIVE;
const PRIVATE_USER_ID = MOCK_SOCIAL_USER_IDS.PRIVATE;

const RELATION_USER_POOL = [
  {
    userId: MOCK_FOLLOWEE_IDS[0],
    displayName: "Sarah Jenkins",
    avatarUrl: "https://i.pravatar.cc/200?img=5",
  },
  {
    userId: MOCK_FOLLOWEE_IDS[1],
    displayName: "David Chen",
    avatarUrl: "https://i.pravatar.cc/200?img=8",
  },
  {
    userId: ACTIVE_USER_ID,
    displayName: "Active User",
    avatarUrl: "https://i.pravatar.cc/200?img=3",
  },
  {
    userId: "c0000000-0000-4000-8000-000000000099",
    displayName: "Admin User",
    avatarUrl: "https://i.pravatar.cc/200?img=12",
  },
  {
    userId: PRIVATE_USER_ID,
    displayName: "Chloe Smith",
    avatarUrl: "https://i.pravatar.cc/200?img=45",
  },
];

function relationItem(user, index) {
  const day = Math.max(1, 28 - (index % 28));
  return {
    userId: user.userId,
    displayName: user.displayName,
    avatarUrl: user.avatarUrl,
    followedAt: `2026-05-${String(day).padStart(2, "0")}T10:00:00.000Z`,
  };
}

function buildActiveFollowers() {
  const base = [
    relationItem(RELATION_USER_POOL[0], 0),
    relationItem(RELATION_USER_POOL[1], 1),
    relationItem(RELATION_USER_POOL[3], 2),
    relationItem(RELATION_USER_POOL[4], 3),
  ];
  const extra = Array.from({ length: 21 }, (_, index) =>
    relationItem(
      {
        userId: `a1000000-0000-4000-8000-${(index + 1).toString(16).padStart(12, "0")}`,
        displayName: `Follower ${index + 1}`,
        avatarUrl: `https://i.pravatar.cc/200?img=${(index % 70) + 1}`,
      },
      index + 4
    )
  );
  return [...base, ...extra];
}

const RELATIONS_BY_TARGET = {
  [ACTIVE_USER_ID]: {
    followers: buildActiveFollowers(),
    following: [
      relationItem(RELATION_USER_POOL[0], 0),
      relationItem(RELATION_USER_POOL[1], 1),
      relationItem(RELATION_USER_POOL[3], 2),
    ],
  },
  [MOCK_FOLLOWEE_IDS[0]]: {
    followers: [
      relationItem(RELATION_USER_POOL[2], 0),
      relationItem(RELATION_USER_POOL[1], 1),
      relationItem(RELATION_USER_POOL[3], 2),
      relationItem(RELATION_USER_POOL[4], 3),
    ],
    following: [
      relationItem(RELATION_USER_POOL[2], 0),
      relationItem(RELATION_USER_POOL[1], 1),
    ],
  },
  [MOCK_FOLLOWEE_IDS[1]]: {
    followers: [relationItem(RELATION_USER_POOL[2], 0), relationItem(RELATION_USER_POOL[0], 1)],
    following: [
      relationItem(RELATION_USER_POOL[0], 0),
      relationItem(RELATION_USER_POOL[4], 1),
    ],
  },
  [MOCK_SOCIAL_USER_IDS.RELATIONS_EMPTY]: {
    followers: [],
    following: [],
  },
};

export function canViewerAccessRelations(targetUserId, viewerUserId) {
  const profile = buildSocialProfile(targetUserId, viewerUserId);
  if (!profile) return { ok: false, status: 404 };
  if (viewerUserId === targetUserId) return { ok: true, profile };
  if (!profile.canViewFullProfile) return { ok: false, status: 403 };
  return { ok: true, profile };
}

export function buildUserRelationsPage(targetUserId, viewerUserId, { type, page, size }) {
  const access = canViewerAccessRelations(targetUserId, viewerUserId);
  if (!access.ok) {
    return { error: access.status };
  }

  const lists = RELATIONS_BY_TARGET[targetUserId] || {
    followers: RELATION_USER_POOL.slice(0, 3).map((user, index) => relationItem(user, index)),
    following: RELATION_USER_POOL.slice(1, 4).map((user, index) => relationItem(user, index)),
  };

  const allItems = lists[type] || [];
  const totalElements = allItems.length;
  const totalPages = totalElements === 0 ? 0 : Math.ceil(totalElements / size);
  const start = page * size;
  const slice = allItems.slice(start, start + size);

  return {
    targetUserId,
    type,
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
