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
];

function findRefund(id) {
  return refundApprovals.find((item) => item.id === id);
}

export function getAdminRefundApproval(id) {
  const item = findRefund(id);
  if (!item) {
    return { error: "COMMERCE-404-REFUND-REQUEST", message: "Refund request not found", status: 404 };
  }
  return { data: { ...item } };
}

function filterByStatus(status) {
  if (!status) return [...refundApprovals];
  return refundApprovals.filter((item) => item.status === status);
}

export function listAdminRefundApprovals({ status, page = "1", limit = "20" }) {
  const items = filterByStatus(status);
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
