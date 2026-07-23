const refundApprovals = [
  {
    id: "aaaaaaaa-aaaa-4aaa-aaaa-aaaaaaaa0001",
    payment_id: "bbbbbbbb-bbbb-4bbb-bbbb-bbbbbbbb0001",
    order_id: "cccccccc-cccc-4ccc-cccc-cccccccc0001",
    buyer_id: "dddddddd-dddd-4ddd-dddd-dddddddd0001",
    requested_by: "BUYER",
    requested_by_user_id: "dddddddd-dddd-4ddd-dddd-dddddddd0001",
    status: "REQUESTED",
    amount: 350000,
    reason: "BUYER_CANCELLED",
    admin_note: null,
    payment_method: "VNPAY",
    order_payment_status: "PAID",
    order_status: "PROCESSING",
    requested_at: "2026-06-10T08:30:00.000Z",
    confirmed_at: null,
    rejected_at: null,
  },
  {
    id: "aaaaaaaa-aaaa-4aaa-aaaa-aaaaaaaa0002",
    payment_id: "bbbbbbbb-bbbb-4bbb-bbbb-bbbbbbbb0002",
    order_id: "cccccccc-cccc-4ccc-cccc-cccccccc0002",
    buyer_id: "dddddddd-dddd-4ddd-dddd-dddddddd0002",
    requested_by: "SELLER",
    requested_by_user_id: "eeeeeeee-eeee-4eee-eeee-eeeeeeee0002",
    status: "CONFIRMED",
    amount: 520000,
    reason: "BUYER_CANCELLED",
    admin_note: "Đã hoàn trên VNPay",
    payment_method: "VNPAY",
    order_payment_status: "REFUNDED",
    order_status: "CANCELLED",
    requested_at: "2026-05-01T10:00:00.000Z",
    confirmed_at: "2026-05-02T11:00:00.000Z",
    rejected_at: null,
  },
  {
    id: "aaaaaaaa-aaaa-4aaa-aaaa-aaaaaaaa0003",
    payment_id: "bbbbbbbb-bbbb-4bbb-bbbb-bbbbbbbb0003",
    order_id: "cccccccc-cccc-4ccc-cccc-cccccccc0003",
    buyer_id: "dddddddd-dddd-4ddd-dddd-dddddddd0003",
    requested_by: "BUYER",
    requested_by_user_id: "dddddddd-dddd-4ddd-dddd-dddddddd0003",
    status: "REJECTED",
    amount: 180000,
    reason: "BUYER_CANCELLED",
    admin_note: "Chưa đủ điều kiện",
    payment_method: "PAYOS",
    order_payment_status: "PAID",
    order_status: "PROCESSING",
    requested_at: "2026-04-15T09:00:00.000Z",
    confirmed_at: null,
    rejected_at: "2026-04-16T09:00:00.000Z",
  },
];

function findRefund(id) {
  return refundApprovals.find((item) => item.id === id);
}

function matchesQuery(item, q) {
  if (!q) return true;
  const needle = q.toLowerCase();
  return [item.id, item.payment_id, item.order_id].some((value) =>
    String(value).toLowerCase().includes(needle),
  );
}

function matchesDate(item, from, to) {
  const requestedAt = new Date(item.requested_at).getTime();
  if (from) {
    const fromTs = new Date(from.length === 10 ? `${from}T00:00:00.000Z` : from).getTime();
    if (requestedAt < fromTs) return false;
  }
  if (to) {
    const toTs = new Date(to.length === 10 ? `${to}T23:59:59.999Z` : to).getTime();
    if (requestedAt > toTs) return false;
  }
  return true;
}

export function getAdminRefundApproval(id) {
  const item = findRefund(id);
  if (!item) {
    return { error: "COMMERCE-404-REFUND-REQUEST", message: "Refund request not found", status: 404 };
  }
  return { data: { ...item } };
}

function filterItems({ status, q, requested_by, payment_method, from, to }) {
  return refundApprovals.filter((item) => {
    if (status && item.status !== status) return false;
    if (requested_by && item.requested_by !== requested_by) return false;
    if (payment_method && item.payment_method !== payment_method) return false;
    if (!matchesQuery(item, q)) return false;
    if (!matchesDate(item, from, to)) return false;
    return true;
  });
}

export function listAdminRefundApprovals({
  status,
  q,
  requested_by,
  payment_method,
  from,
  to,
  page = "1",
  limit = "20",
}) {
  const items = filterItems({ status, q, requested_by, payment_method, from, to });
  const pageNum = Number(page) || 1;
  const limitNum = Number(limit) || 20;
  const start = (pageNum - 1) * limitNum;
  const slice = items.slice(start, start + limitNum);
  return {
    data: {
      items: slice,
      pagination: {
        page: pageNum,
        limit: limitNum,
        total_items: items.length,
        total_pages: Math.max(1, Math.ceil(items.length / limitNum)),
        has_next: start + limitNum < items.length,
      },
    },
  };
}

export function confirmAdminRefundApproval(id) {
  const item = findRefund(id);
  if (!item) return { error: "COMMERCE-404-REFUND-REQUEST", message: "Refund request not found", status: 404 };
  if (item.status !== "REQUESTED") {
    return { error: "COMMERCE-409-REFUND-STATE", message: "Invalid refund state", status: 409 };
  }
  item.status = "CONFIRMED";
  item.confirmed_at = new Date().toISOString();
  item.order_status = "CANCELLED";
  item.order_payment_status = "REFUNDED";
  return { data: { ...item } };
}

export function rejectAdminRefundApproval(id, adminNote = "") {
  const item = findRefund(id);
  if (!item) return { error: "COMMERCE-404-REFUND-REQUEST", message: "Refund request not found", status: 404 };
  if (item.status !== "REQUESTED") {
    return { error: "COMMERCE-409-REFUND-STATE", message: "Invalid refund state", status: 409 };
  }
  item.status = "REJECTED";
  item.rejected_at = new Date().toISOString();
  item.admin_note = adminNote;
  return { data: { ...item } };
}

export function registerRefundApprovalFromOrderCancel(orderId, summary) {
  if (!summary?.refund_request_id) return;
  const existing = findRefund(summary.refund_request_id);
  if (existing) return;
  refundApprovals.unshift({
    id: summary.refund_request_id,
    payment_id: summary.payment_id || "bbbbbbbb-bbbb-4bbb-bbbb-bbbbbbbb0001",
    order_id: orderId,
    buyer_id: summary.buyer_id || "dddddddd-dddd-4ddd-dddd-dddddddd0001",
    requested_by: summary.requested_by || "BUYER",
    requested_by_user_id: summary.requested_by_user_id || summary.buyer_id,
    status: "REQUESTED",
    amount: summary.amount || 0,
    reason: summary.reason || "CANCELLED",
    admin_note: null,
    payment_method: "VNPAY",
    order_payment_status: "PAID",
    order_status: "PROCESSING",
    requested_at: new Date().toISOString(),
    confirmed_at: null,
    rejected_at: null,
  });
}
