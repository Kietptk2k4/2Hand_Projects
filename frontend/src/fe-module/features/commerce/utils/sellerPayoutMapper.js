function toNumber(value) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
}

export function mapSellerPayoutAccount(raw) {
  if (!raw) return null;
  return {
    id: raw.id,
    bankName: raw.bank_name ?? raw.bankName,
    bankAccountName: raw.bank_account_name ?? raw.bankAccountName,
    bankAccountNumber: raw.bank_account_number ?? raw.bankAccountNumber,
    isDefault: Boolean(raw.is_default ?? raw.isDefault),
    createdAt: raw.created_at ?? raw.createdAt,
    updatedAt: raw.updated_at ?? raw.updatedAt,
  };
}

export function mapSellerPayoutAccountsResponse(raw) {
  const accounts = raw?.accounts ?? [];
  return accounts.map(mapSellerPayoutAccount).filter(Boolean);
}

export function mapSellerPayoutRequest(raw) {
  if (!raw) return null;
  return {
    id: raw.id,
    sellerId: raw.seller_id ?? raw.sellerId,
    payoutAccountId: raw.payout_account_id ?? raw.payoutAccountId,
    amount: toNumber(raw.amount),
    status: raw.status,
    adminNote: raw.admin_note ?? raw.adminNote,
    bankTransferRef: raw.bank_transfer_ref ?? raw.bankTransferRef,
    requestedAt: raw.requested_at ?? raw.requestedAt,
    approvedAt: raw.approved_at ?? raw.approvedAt,
    paidAt: raw.paid_at ?? raw.paidAt,
    rejectedAt: raw.rejected_at ?? raw.rejectedAt,
    cancelledAt: raw.cancelled_at ?? raw.cancelledAt,
    bankName: raw.bank_name ?? raw.bankName,
    bankAccountName: raw.bank_account_name ?? raw.bankAccountName,
    bankAccountNumber: raw.bank_account_number ?? raw.bankAccountNumber,
  };
}

export function mapSellerPayoutRequestsResponse(raw) {
  if (!raw) {
    return { items: [], pagination: { page: 1, limit: 20, totalItems: 0, totalPages: 0, hasNext: false } };
  }
  const paginationRaw = raw.pagination ?? {};
  return {
    items: (raw.items ?? []).map(mapSellerPayoutRequest).filter(Boolean),
    pagination: {
      page: toNumber(paginationRaw.page) || 1,
      limit: toNumber(paginationRaw.limit) || 20,
      totalItems: toNumber(paginationRaw.total_items ?? paginationRaw.totalItems),
      totalPages: toNumber(paginationRaw.total_pages ?? paginationRaw.totalPages),
      hasNext: Boolean(paginationRaw.has_next ?? paginationRaw.hasNext),
    },
  };
}
