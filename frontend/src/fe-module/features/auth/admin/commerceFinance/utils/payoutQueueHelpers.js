import { PAYOUT_STATUS_LABELS } from "./adminFinancePayoutMapper.js";

/**
 * Sprint 2 API enrichments (not blocking FE):
 * - shop_name on list items
 * - seller_id / from / to / q filters on GET payout-requests
 */

export const PAYOUT_QUEUE_PAGE_SIZE = 20;

export const PAYOUT_STATUS_OPTIONS = [
  { value: "", label: "Tất cả" },
  { value: "REQUESTED", label: "Chờ duyệt" },
  { value: "APPROVED", label: "Đã duyệt" },
  { value: "PAID", label: "Đã chuyển" },
  { value: "REJECTED", label: "Từ chối" },
  { value: "CANCELLED", label: "Đã hủy" },
];

export const PAYOUT_HERO_KPI_META = [
  {
    key: "REQUESTED",
    label: "Chờ duyệt",
    hint: "Yêu cầu cần xử lý",
    icon: "hourglass_top",
    accentClass: "text-[#8b7355]",
    surfaceClass: "bg-[#8b7355]/10",
  },
  {
    key: "APPROVED",
    label: "Chờ chuyển",
    hint: "Đã duyệt, chưa ghi nhận CK",
    icon: "account_balance",
    accentClass: "text-[#5b7c6a]",
    surfaceClass: "bg-[#5b7c6a]/10",
  },
  {
    key: "PAID",
    label: "Đã chuyển",
    hint: "Trong kỳ đã chọn",
    icon: "payments",
    accentClass: "text-[#8b7355]",
    surfaceClass: "bg-[#8b7355]/10",
  },
  {
    key: "TOTAL",
    label: "Tổng tiền kỳ",
    hint: "Tất cả trạng thái",
    icon: "stacked_bar_chart",
    accentClass: "text-admin-text-secondary",
    surfaceClass: "bg-admin-surface-muted",
  },
];

export function parsePayoutPage(raw, fallback = 1) {
  const page = Number.parseInt(String(raw ?? ""), 10);
  return Number.isFinite(page) && page > 0 ? page : fallback;
}

export function findPayoutOverviewRow(overview = [], status) {
  return (overview || []).find((row) => row.status === status) || null;
}

export function computePayoutHeroMetrics(overview = []) {
  const rows = Array.isArray(overview) ? overview : [];
  const requested = findPayoutOverviewRow(rows, "REQUESTED");
  const approved = findPayoutOverviewRow(rows, "APPROVED");
  const paid = findPayoutOverviewRow(rows, "PAID");
  const totalAmount = rows.reduce((sum, row) => sum + (Number(row.totalAmount) || 0), 0);
  const totalCount = rows.reduce((sum, row) => sum + (Number(row.requestCount) || 0), 0);

  return {
    requested: {
      count: Number(requested?.requestCount) || 0,
      amount: Number(requested?.totalAmount) || 0,
    },
    approved: {
      count: Number(approved?.requestCount) || 0,
      amount: Number(approved?.totalAmount) || 0,
    },
    paid: {
      count: Number(paid?.requestCount) || 0,
      amount: Number(paid?.totalAmount) || 0,
    },
    total: {
      count: totalCount,
      amount: totalAmount,
    },
  };
}

export function buildPayoutPaginationSummary({ page, limit, totalItems }) {
  if (!totalItems) return "Không có yêu cầu";
  const start = (page - 1) * limit + 1;
  const end = Math.min(page * limit, totalItems);
  return `Hiển thị ${start}–${end} / ${totalItems} yêu cầu`;
}

export function getPayoutEmptyMessage(statusFilter) {
  if (!statusFilter) {
    return "Không có yêu cầu rút tiền trong bộ lọc hiện tại.";
  }
  const label = PAYOUT_STATUS_LABELS[statusFilter] || statusFilter;
  return `Không có yêu cầu ở trạng thái "${label}".`;
}

export function buildPayoutTimeline(item) {
  if (!item) return [];
  const steps = [
    { key: "requested", label: "Yêu cầu", at: item.requestedAt },
    { key: "approved", label: "Duyệt", at: item.approvedAt },
    { key: "paid", label: "Chuyển khoản", at: item.paidAt },
    { key: "rejected", label: "Từ chối", at: item.rejectedAt },
    { key: "cancelled", label: "Hủy", at: item.cancelledAt },
  ];
  return steps.filter((step) => step.at);
}
