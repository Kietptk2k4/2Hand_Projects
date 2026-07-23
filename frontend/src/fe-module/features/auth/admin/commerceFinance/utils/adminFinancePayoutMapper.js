import { formatVndPrice } from "../../../../social/utils/formatPrice";

export const PAYOUT_STATUS_LABELS = {
  REQUESTED: "Chờ duyệt",
  APPROVED: "Đã duyệt",
  PAID: "Đã chuyển",
  REJECTED: "Từ chối",
  CANCELLED: "Đã hủy",
};

export function mapPayoutQueueItem(raw) {
  if (!raw) return null;
  return {
    id: raw.id,
    sellerId: raw.seller_id ?? raw.sellerId,
    amount: Number(raw.amount) || 0,
    amountLabel: formatVndPrice(Number(raw.amount) || 0),
    status: raw.status,
    bankName: raw.bank_name ?? raw.bankName,
    bankAccountName: raw.bank_account_name ?? raw.bankAccountName,
    bankAccountNumber: raw.bank_account_number ?? raw.bankAccountNumber,
    requestedAt: raw.requested_at ?? raw.requestedAt,
    approvedAt: raw.approved_at ?? raw.approvedAt,
    paidAt: raw.paid_at ?? raw.paidAt,
    rejectedAt: raw.rejected_at ?? raw.rejectedAt,
    cancelledAt: raw.cancelled_at ?? raw.cancelledAt,
    adminNote: raw.admin_note ?? raw.adminNote,
    bankTransferRef: raw.bank_transfer_ref ?? raw.bankTransferRef,
  };
}

export function mapPayoutQueueResponse(raw) {
  const pagination = raw?.pagination ?? {};
  const page = Number(pagination.page) || 1;
  const limit = Number(pagination.limit) || 20;
  const totalItems = Number(pagination.total_items ?? pagination.totalItems) || 0;
  const totalPages = Number(pagination.total_pages ?? pagination.totalPages) || 0;
  const hasNext = Boolean(pagination.has_next ?? pagination.hasNext);

  return {
    items: (raw?.items ?? []).map(mapPayoutQueueItem).filter(Boolean),
    pagination: {
      page,
      limit,
      totalItems,
      totalPages: totalPages || Math.max(1, Math.ceil(totalItems / limit) || 1),
      hasNext,
    },
  };
}

export function applyPayoutQueueAfterAction(queue, updatedItem, statusFilter) {
  const mapped = mapPayoutQueueItem(updatedItem);
  if (!mapped) return queue;

  const matchesFilter = !statusFilter || mapped.status === statusFilter;
  if (!matchesFilter) {
    return {
      items: queue.items.filter((item) => item.id !== mapped.id),
      pagination: {
        ...queue.pagination,
        totalItems: Math.max(0, queue.pagination.totalItems - 1),
      },
    };
  }

  const hasItem = queue.items.some((item) => item.id === mapped.id);
  const items = hasItem
    ? queue.items.map((item) => (item.id === mapped.id ? mapped : item))
    : [mapped, ...queue.items];

  return { ...queue, items };
}