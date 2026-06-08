const ADMIN_ID = "c0000000-0000-4000-8000-000000000099";
const TARGET_USER_ID = "b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1";
const TARGET_POST_ID = "507f1f77bcf86cd799439011";
const TARGET_SHOP_ID = "f1111111-1111-4111-8111-111111111101";
const TARGET_PRODUCT_ID = "a4444444-4444-4444-8444-444444444401";

export const MOCK_AUDIT_LOG_ID = "d1111111-1111-4111-8111-111111111001";

const ALL_LOGS = [
  {
    log_id: "d1111111-1111-4111-8111-111111111001",
    admin_id: ADMIN_ID,
    action_type: "USER_SUSPEND",
    target_type: "USER",
    target_id: TARGET_USER_ID,
    status: "SUCCESS",
    request_payload: { reason: "Spam report #1001", duration_days: 7 },
    response_payload: { status: "SUCCESS", result: { enforcement_id: "e1111111-1111-4111-8111-111111111001" } },
    ip_address: "10.0.0.1",
    user_agent: "Mozilla/5.0 (Admin Portal)",
    created_at: "2026-05-23T10:00:00Z",
  },
  {
    log_id: "d1111111-1111-4111-8111-111111111002",
    admin_id: ADMIN_ID,
    action_type: "POST_MODERATED",
    target_type: "POST",
    target_id: TARGET_POST_ID,
    status: "SUCCESS",
    request_payload: { action: "HIDE", reason: "Policy violation" },
    response_payload: { status: "SUCCESS", result: { moderation_log_id: "m1111111-1111-4111-8111-111111111001" } },
    ip_address: "10.0.0.2",
    user_agent: "Mozilla/5.0 (Admin Portal)",
    created_at: "2026-05-22T15:30:00Z",
  },
  {
    log_id: "d1111111-1111-4111-8111-111111111003",
    admin_id: ADMIN_ID,
    action_type: "SHOP_SUSPENDED",
    target_type: "SHOP",
    target_id: TARGET_SHOP_ID,
    status: "SUCCESS",
    request_payload: { reason: "Counterfeit listing" },
    response_payload: { status: "SUCCESS", result: { shop_id: TARGET_SHOP_ID } },
    ip_address: "10.0.0.3",
    user_agent: "Mozilla/5.0 (Admin Portal)",
    created_at: "2026-05-21T09:15:00Z",
  },
  {
    log_id: "d1111111-1111-4111-8111-111111111004",
    admin_id: ADMIN_ID,
    action_type: "PRODUCT_REMOVED",
    target_type: "PRODUCT",
    target_id: TARGET_PRODUCT_ID,
    status: "FAILURE",
    request_payload: { reason: "Policy violation" },
    response_payload: { status: "FAILURE", message: "Product already removed" },
    ip_address: "10.0.0.4",
    user_agent: "Mozilla/5.0 (Admin Portal)",
    created_at: "2026-05-20T18:45:00Z",
  },
];

function matchesFilter(log, filters) {
  if (filters.admin_id && log.admin_id !== filters.admin_id) return false;
  if (filters.action && log.action_type !== filters.action) return false;
  if (filters.target_type && log.target_type !== filters.target_type) return false;
  if (filters.target_id && log.target_id !== filters.target_id) return false;
  if (filters.status && log.status !== filters.status) return false;
  if (filters.from && new Date(log.created_at) < new Date(filters.from)) return false;
  if (filters.to && new Date(log.created_at) > new Date(filters.to)) return false;
  return true;
}

export function getMockAdminActionLogs({ admin_id, action, target_type, target_id, status, from, to, page = 1, size = 20 }) {
  const filters = { admin_id, action, target_type, target_id, status, from, to };
  const filtered = ALL_LOGS.filter((log) => matchesFilter(log, filters)).sort(
    (a, b) => new Date(b.created_at) - new Date(a.created_at),
  );
  const safePage = Math.max(1, Number(page) || 1);
  const safeSize = Math.min(100, Math.max(1, Number(size) || 20));
  const start = (safePage - 1) * safeSize;
  const slice = filtered.slice(start, start + safeSize);
  return {
    page: safePage,
    size: safeSize,
    total_elements: filtered.length,
    total_pages: Math.max(1, Math.ceil(filtered.length / safeSize)),
    logs: slice,
  };
}

export function getMockAdminActionLogDetail(logId) {
  return ALL_LOGS.find((log) => log.log_id === logId) || null;
}