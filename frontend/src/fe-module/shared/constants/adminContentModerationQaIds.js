import { MOCK_POST_IDS } from "../../features/social/constants/mockPostIds.js";

/**
 * QA fixture IDs — keep in sync with mocks/data/adminContentModerationData.js
 * Use when VITE_USE_MOCK=true (admin@2hands.vn).
 */
export const ADMIN_CONTENT_MODERATION_QA = {
  audit: {
    logId: "d1111111-1111-4111-8111-111111111001",
    filterAdminId: "c0000000-0000-4000-8000-000000000099",
  },
  post: {
    sample: MOCK_POST_IDS.SUCCESS,
    notFound: MOCK_POST_IDS.NOT_FOUND,
  },
  comment: {
    sample: "674b100000000000000001",
    notFound: MOCK_POST_IDS.NOT_FOUND,
  },
  shop: {
    active: "s2000000-0000-4000-8000-000000000101",
    suspended: "s2000000-0000-4000-8000-000000000102",
    closed: "s2000000-0000-4000-8000-000000000103",
  },
  product: {
    active: "c1000000-0000-4000-8000-000000000001",
    removed: "cap-0000-4000-8000-000000000001",
  },
  review: {
    visible: "arv-0000-4000-8000-000000000001",
    hidden: "arv-0000-4000-8000-000000000002",
  },
};

export function buildAdminContentModerationQaUrl({ section, tab, params = {} }) {
  const search = new URLSearchParams({ section, tab, ...params });
  return `/admin?${search.toString()}`;
}