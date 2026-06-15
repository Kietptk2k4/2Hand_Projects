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
    adminNote: raw.admin_note ?? raw.adminNote,
    bankTransferRef: raw.bank_transfer_ref ?? raw.bankTransferRef,
  };
}

export function mapPayoutQueueResponse(raw) {
  const pagination = raw?.pagination ?? {};
  return {
    items: (raw?.items ?? []).map(mapPayoutQueueItem).filter(Boolean),
    pagination: {
      page: Number(pagination.page) || 1,
      totalItems: Number(pagination.total_items ?? pagination.totalItems) || 0,
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