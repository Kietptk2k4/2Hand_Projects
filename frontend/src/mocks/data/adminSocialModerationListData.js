import { ADMIN_CONTENT_MODERATION_QA } from "../../fe-module/shared/constants/adminContentModerationQaIds.js";
import { MOCK_POST_IDS } from "../../fe-module/features/social/constants/mockPostIds.js";

const MOCK_AUTHOR_ID = "b1000000-0000-4000-8000-000000000001";

export const MOCK_MODERATION_POST_LIST = [
  {
    id: MOCK_POST_IDS.SUCCESS,
    author_id: MOCK_AUTHOR_ID,
    caption_preview: "Bai viet mau cho QA moderation",
    status: "ACTIVE",
    moderation_status: "NONE",
    like_count: 24,
    created_at: "2025-11-10T08:30:00.000Z",
    updated_at: "2025-11-10T08:30:00.000Z",
  },
  {
    id: "674a10000000000000000002",
    author_id: MOCK_AUTHOR_ID,
    caption_preview: "Noi dung da bi an boi admin",
    status: "ACTIVE",
    moderation_status: "HIDDEN",
    like_count: 3,
    created_at: "2025-10-05T14:20:00.000Z",
    updated_at: "2025-10-06T09:00:00.000Z",
  },
  {
    id: "674a10000000000000000003",
    author_id: MOCK_AUTHOR_ID,
    caption_preview: "Bai viet da go khoi he thong",
    status: "ACTIVE",
    moderation_status: "REMOVED",
    like_count: 0,
    created_at: "2025-09-01T11:00:00.000Z",
    updated_at: "2025-09-02T16:45:00.000Z",
  },
  {
    id: "674a10000000000000000004",
    author_id: MOCK_AUTHOR_ID,
    caption_preview: "Bai nhap chua dang",
    status: "DRAFT",
    moderation_status: "NONE",
    like_count: 0,
    created_at: "2025-12-01T07:15:00.000Z",
    updated_at: "2025-12-01T07:15:00.000Z",
  },
];

export const MOCK_MODERATION_COMMENT_LIST = [
  {
    id: ADMIN_CONTENT_MODERATION_QA.comment.sample,
    post_id: MOCK_POST_IDS.SUCCESS,
    author_id: MOCK_AUTHOR_ID,
    content_preview: "Binh luan mau cho QA moderation",
    status: "ACTIVE",
    like_count: 5,
    created_at: "2025-11-10T09:00:00.000Z",
    updated_at: "2025-11-10T09:00:00.000Z",
  },
  {
    id: "674b100000000000000002",
    post_id: MOCK_POST_IDS.SUCCESS,
    author_id: MOCK_AUTHOR_ID,
    content_preview: "Noi dung spam can kiem duyet",
    status: "ACTIVE",
    like_count: 0,
    created_at: "2025-11-11T10:30:00.000Z",
    updated_at: "2025-11-11T10:30:00.000Z",
  },
  {
    id: "674b100000000000000003",
    post_id: "674a10000000000000000002",
    author_id: MOCK_AUTHOR_ID,
    content_preview: "Binh luan tren bai da an",
    status: "ACTIVE",
    like_count: 1,
    created_at: "2025-10-06T08:00:00.000Z",
    updated_at: "2025-10-06T08:00:00.000Z",
  },
];

function includesQuery(text, q) {
  return String(text || "").toLowerCase().includes(q);
}

export function listModerationPosts({ status, moderation_status, q, sort, page, size }) {
  let items = [...MOCK_MODERATION_POST_LIST];
  if (status) items = items.filter((item) => item.status === status);
  if (moderation_status) items = items.filter((item) => item.moderation_status === moderation_status);
  if (q) {
    const needle = q.toLowerCase();
    items = items.filter(
      (item) => includesQuery(item.id, needle) || includesQuery(item.caption_preview, needle),
    );
  }
  items = sortModerationPosts(items, sort);
  return paginate(items, page, size);
}

export function listModerationComments({ status, post_id, q, sort, page, size }) {
  let items = [...MOCK_MODERATION_COMMENT_LIST];
  if (status) items = items.filter((item) => item.status === status);
  if (post_id) items = items.filter((item) => item.post_id === post_id);
  if (q) {
    const needle = q.toLowerCase();
    items = items.filter(
      (item) =>
        includesQuery(item.id, needle) ||
        includesQuery(item.content_preview, needle) ||
        includesQuery(item.post_id, needle),
    );
  }
  items = sortModerationComments(items, sort);
  return paginate(items, page, size);
}

function sortModerationPosts(items, sort) {
  const sorted = [...items];
  switch (sort) {
    case "updated_at":
      sorted.sort((a, b) => String(b.updated_at).localeCompare(String(a.updated_at)));
      break;
    case "moderation_status":
      sorted.sort((a, b) => a.moderation_status.localeCompare(b.moderation_status));
      break;
    case "like_count":
      sorted.sort((a, b) => b.like_count - a.like_count);
      break;
    case "created_at":
    default:
      sorted.sort((a, b) => String(b.created_at).localeCompare(String(a.created_at)));
      break;
  }
  return sorted;
}

function sortModerationComments(items, sort) {
  const sorted = [...items];
  switch (sort) {
    case "updated_at":
      sorted.sort((a, b) => String(b.updated_at).localeCompare(String(a.updated_at)));
      break;
    case "like_count":
      sorted.sort((a, b) => b.like_count - a.like_count);
      break;
    case "created_at":
    default:
      sorted.sort((a, b) => String(b.created_at).localeCompare(String(a.created_at)));
      break;
  }
  return sorted;
}

function paginate(items, page, size) {
  const safeSize = Math.max(1, Math.min(50, Number(size) || 20));
  const safePage = Math.max(1, Number(page) || 1);
  const totalItems = items.length;
  const totalPages = Math.max(1, Math.ceil(totalItems / safeSize));
  const currentPage = Math.min(safePage, totalPages);
  const start = (currentPage - 1) * safeSize;
  const pageItems = items.slice(start, start + safeSize);
  return {
    items: pageItems,
    pagination: {
      page: currentPage,
      size: safeSize,
      total_items: totalItems,
      total_pages: totalPages,
      has_next: currentPage < totalPages,
    },
  };
}
