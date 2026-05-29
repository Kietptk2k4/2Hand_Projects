import { MOCK_FOLLOWEE_IDS } from "./socialFeedData";
import {
  MOCK_POST_ID_EMPTY_COMMENTS,
  MOCK_POST_ID_SUCCESS,
  resolveAuthor,
} from "./socialPostDetailData";

const COMMENT_PREFIX = "674b100000000000000000";
const REPLY_PREFIX = "674c100000000000000000";

function mockCommentId(suffix, prefix = COMMENT_PREFIX) {
  const id = `${prefix}${String(suffix).padStart(2, "0")}`;
  if (id.length !== 24) {
    throw new Error(`Invalid mock commentId "${id}" (length ${id.length})`);
  }
  return id;
}

function comment(overrides) {
  return {
    media: [],
    likeCount: 0,
    replyCount: 0,
    createdAt: "2026-05-21T10:00:00Z",
    updatedAt: "2026-05-21T10:00:00Z",
    ...overrides,
  };
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

export function getCommentsForPost(postId, { parentCommentId } = {}) {
  if (postId === MOCK_POST_ID_EMPTY_COMMENTS) {
    return [];
  }

  if (postId !== MOCK_POST_ID_SUCCESS) {
    return [
      comment({
        commentId: mockCommentId("99"),
        postId,
        parentCommentId: null,
        author: resolveAuthor("b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1"),
        contentText: "Sample comment on this post.",
        likeCount: 0,
        replyCount: 0,
      }),
    ];
  }

  if (parentCommentId) {
    return REPLIES_BY_PARENT[parentCommentId] || [];
  }

  return TOP_LEVEL_FOR_SUCCESS;
}
