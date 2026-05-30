import { MOCK_FOLLOWEE_IDS } from "./socialFeedData";
import {
  MOCK_POST_ID_EMPTY_COMMENTS,
  MOCK_POST_ID_SUCCESS,
  resolveAuthor,
} from "./socialPostDetailData";

const COMMENT_PREFIX = "674b100000000000000000";
export const REPLY_PREFIX = "674c100000000000000000";

let nextDynamicCommentSeq = 100;

function mockCommentId(suffix, prefix = COMMENT_PREFIX) {
  const id = `${prefix}${String(suffix).padStart(2, "0")}`;
  if (id.length !== 24) {
    throw new Error(`Invalid mock commentId "${id}" (length ${id.length})`);
  }
  return id;
}

function comment(overrides) {
  return {
    status: "ACTIVE",
    media: [],
    likeCount: 0,
    replyCount: 0,
    createdAt: "2026-05-21T10:00:00Z",
    updatedAt: "2026-05-21T10:00:00Z",
    ...overrides,
  };
}

function isActiveComment(item) {
  return (item.status || "ACTIVE") !== "DELETED";
}

function filterActiveComments(items) {
  return items.filter(isActiveComment);
}

const TOP_LEVEL_FOR_SUCCESS = [
  comment({
    commentId: mockCommentId("01"),
    postId: MOCK_POST_ID_SUCCESS,
    parentCommentId: null,
    author: resolveAuthor(MOCK_FOLLOWEE_IDS[0]),
    contentText: "Hay qua! Cam on ban da chia se.",
    likeCount: 3,
    replyCount: 2,
    createdAt: "2026-05-29T09:00:00Z",
  }),
  comment({
    commentId: mockCommentId("02"),
    postId: MOCK_POST_ID_SUCCESS,
    parentCommentId: null,
    author: resolveAuthor(MOCK_FOLLOWEE_IDS[1]),
    contentText: "Toi can tu van them ve thue xuyen bien gioi.",
    likeCount: 1,
    replyCount: 1,
    createdAt: "2026-05-29T08:30:00Z",
  }),
  comment({
    commentId: mockCommentId("03"),
    postId: MOCK_POST_ID_SUCCESS,
    parentCommentId: null,
    author: resolveAuthor("b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1"),
    contentText: "Slide deck co san khong a?",
    likeCount: 0,
    replyCount: 0,
    createdAt: "2026-05-29T08:00:00Z",
  }),
  comment({
    commentId: mockCommentId("04"),
    postId: MOCK_POST_ID_SUCCESS,
    parentCommentId: null,
    author: resolveAuthor("c0000000-0000-4000-8000-000000000099"),
    contentText: "Rất hữu ích cho SME.",
    likeCount: 5,
    replyCount: 0,
    createdAt: "2026-05-28T20:00:00Z",
  }),
  comment({
    commentId: mockCommentId("05"),
    postId: MOCK_POST_ID_SUCCESS,
    parentCommentId: null,
    author: resolveAuthor(MOCK_FOLLOWEE_IDS[0]),
    contentText: "Bookmarked!",
    likeCount: 2,
    replyCount: 0,
    createdAt: "2026-05-28T18:00:00Z",
  }),
];

for (let i = 6; i <= 30; i += 1) {
  TOP_LEVEL_FOR_SUCCESS.push(
    comment({
      commentId: mockCommentId(String(i).padStart(2, "0")),
      postId: MOCK_POST_ID_SUCCESS,
      parentCommentId: null,
      author: resolveAuthor(MOCK_FOLLOWEE_IDS[i % MOCK_FOLLOWEE_IDS.length]),
      contentText: `Mock comment #${i} for pagination testing.`,
      likeCount: i % 5,
      replyCount: 0,
      createdAt: `2026-05-${String(Math.max(10, 28 - Math.floor(i / 3))).padStart(2, "0")}T12:00:00Z`,
    })
  );
}

const REPLIES_BY_PARENT = {
  [mockCommentId("01")]: [
    comment({
      commentId: mockCommentId("01", REPLY_PREFIX),
      postId: MOCK_POST_ID_SUCCESS,
      parentCommentId: mockCommentId("01"),
      author: resolveAuthor("b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1"),
      contentText: "Dong y, rat chi tiet.",
      likeCount: 1,
      replyCount: 0,
      createdAt: "2026-05-29T09:15:00Z",
    }),
    comment({
      commentId: mockCommentId("02", REPLY_PREFIX),
      postId: MOCK_POST_ID_SUCCESS,
      parentCommentId: mockCommentId("01"),
      author: resolveAuthor(MOCK_FOLLOWEE_IDS[1]),
      contentText: "Minh cung thay vay.",
      likeCount: 0,
      replyCount: 0,
      createdAt: "2026-05-29T09:20:00Z",
    }),
  ],
  [mockCommentId("02")]: [
    comment({
      commentId: mockCommentId("03", REPLY_PREFIX),
      postId: MOCK_POST_ID_SUCCESS,
      parentCommentId: mockCommentId("02"),
      author: resolveAuthor(MOCK_FOLLOWEE_IDS[0]),
      contentText: "Inbox cho minh nhe.",
      likeCount: 0,
      replyCount: 0,
      createdAt: "2026-05-29T08:45:00Z",
    }),
  ],
};

const extraTopLevelByPostId = new Map();

function generateCommentId(prefix = COMMENT_PREFIX) {
  nextDynamicCommentSeq += 1;
  const suffix = String(nextDynamicCommentSeq % 90).padStart(2, "0");
  return mockCommentId(suffix, prefix);
}

export function findCommentById(commentId) {
  const top = TOP_LEVEL_FOR_SUCCESS.find((item) => item.commentId === commentId);
  if (top) {
    return { comment: top, isTopLevel: true };
  }

  for (const replies of Object.values(REPLIES_BY_PARENT)) {
    const reply = replies.find((item) => item.commentId === commentId);
    if (reply) {
      return { comment: reply, isTopLevel: false, parentCommentId: reply.parentCommentId };
    }
  }

  for (const list of extraTopLevelByPostId.values()) {
    const extra = list.find((item) => item.commentId === commentId);
    if (extra) {
      return { comment: extra, isTopLevel: !extra.parentCommentId };
    }
  }

  return null;
}

export function isReplyCommentId(commentId) {
  return String(commentId).startsWith(REPLY_PREFIX);
}

export function appendTopLevelComment(postId, authorId, contentText) {
  const now = new Date().toISOString();
  const item = comment({
    commentId: generateCommentId(COMMENT_PREFIX),
    postId,
    parentCommentId: null,
    author: resolveAuthor(authorId),
    contentText,
    likeCount: 0,
    replyCount: 0,
    createdAt: now,
    updatedAt: now,
  });

  if (postId === MOCK_POST_ID_SUCCESS) {
    TOP_LEVEL_FOR_SUCCESS.unshift(item);
  } else {
    const list = extraTopLevelByPostId.get(postId) || [];
    list.unshift(item);
    extraTopLevelByPostId.set(postId, list);
  }

  return {
    commentId: item.commentId,
    postId: item.postId,
    authorId,
    contentText: item.contentText,
    media: [],
    status: "ACTIVE",
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
  };
}

export function appendReplyComment(parentCommentId, authorId, contentText) {
  const parentInfo = findCommentById(parentCommentId);
  if (!parentInfo?.isTopLevel) {
    return { error: "NESTED_REPLY_NOT_ALLOWED" };
  }

  const parent = parentInfo.comment;
  const now = new Date().toISOString();
  const item = comment({
    commentId: generateCommentId(REPLY_PREFIX),
    postId: parent.postId,
    parentCommentId,
    author: resolveAuthor(authorId),
    contentText,
    likeCount: 0,
    replyCount: 0,
    createdAt: now,
    updatedAt: now,
  });

  if (!REPLIES_BY_PARENT[parentCommentId]) {
    REPLIES_BY_PARENT[parentCommentId] = [];
  }
  REPLIES_BY_PARENT[parentCommentId].push(item);
  parent.replyCount = (parent.replyCount || 0) + 1;

  return {
    commentId: item.commentId,
    postId: item.postId,
    parentCommentId,
    authorId,
    contentText: item.contentText,
    media: [],
    status: "ACTIVE",
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
  };
}

export function softDeleteComment(commentId, userId, { isModerator = false } = {}) {
  const info = findCommentById(commentId);
  if (!info) {
    return { error: "NOT_FOUND" };
  }

  const target = info.comment;
  const authorId = target.author?.userId;
  if (!isModerator && authorId !== userId) {
    return { error: "FORBIDDEN" };
  }

  const wasActive = isActiveComment(target);
  const now = new Date().toISOString();

  if (wasActive) {
    target.status = "DELETED";
    target.deletedAt = now;
    target.updatedAt = now;
  }

  return {
    data: {
      commentId: target.commentId,
      postId: target.postId,
      status: "DELETED",
      deletedAt: target.deletedAt || now,
      updatedAt: target.updatedAt || now,
    },
    shouldDecrementPost: wasActive,
    postId: target.postId,
    parentCommentId: target.parentCommentId || null,
  };
}

export function getCommentsForPost(postId, { parentCommentId } = {}) {
  if (postId === MOCK_POST_ID_EMPTY_COMMENTS) {
    return [];
  }

  if (parentCommentId) {
    const parentInfo = findCommentById(parentCommentId);
    if (!parentInfo || !isActiveComment(parentInfo.comment)) {
      return [];
    }
    return filterActiveComments(REPLIES_BY_PARENT[parentCommentId] || []);
  }

  if (postId === MOCK_POST_ID_SUCCESS) {
    return filterActiveComments(TOP_LEVEL_FOR_SUCCESS);
  }

  const extra = extraTopLevelByPostId.get(postId) || [];
  if (extra.length > 0) {
    return filterActiveComments(extra);
  }

  return filterActiveComments([
    comment({
      commentId: mockCommentId("99"),
      postId,
      parentCommentId: null,
      author: resolveAuthor("b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1"),
      contentText: "Sample comment on this post.",
      likeCount: 0,
      replyCount: 0,
    }),
  ]);
}
