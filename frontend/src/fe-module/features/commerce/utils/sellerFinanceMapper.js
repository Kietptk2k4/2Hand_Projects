function toNumber(value) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
}

function mapBucket(raw) {
  if (!raw) {
    return { amount: 0, itemCount: 0 };
  }
  return {
    amount: toNumber(raw.amount),
    itemCount: toNumber(raw.item_count ?? raw.itemCount),
  };
}

export function mapSellerRevenueSummaryResponse(raw) {
  if (!raw) {
    return {
      inTransit: { amount: 0, itemCount: 0 },
      pendingConfirm: { amount: 0, itemCount: 0 },
      recognized: { amount: 0, itemCount: 0 },
      totalGross: 0,
      balance: {
        availableBalance: 0,
        totalPlatformFee: 0,
        totalNetCredited: 0,
        pendingPayoutAmount: 0,
        creditEntryCount: 0,
      },
      from: null,
      to: null,
    };
  }

  const balanceRaw = raw.balance ?? {};
  return {
    inTransit: mapBucket(raw.in_transit ?? raw.inTransit),
    pendingConfirm: mapBucket(raw.pending_confirm ?? raw.pendingConfirm),
    recognized: mapBucket(raw.recognized),
    totalGross: toNumber(raw.total_gross ?? raw.totalGross),
    balance: {
      availableBalance: toNumber(balanceRaw.available_balance ?? balanceRaw.availableBalance),
      totalPlatformFee: toNumber(balanceRaw.total_platform_fee ?? balanceRaw.totalPlatformFee),
      totalNetCredited: toNumber(balanceRaw.total_net_credited ?? balanceRaw.totalNetCredited),
      pendingPayoutAmount: toNumber(balanceRaw.pending_payout_amount ?? balanceRaw.pendingPayoutAmount),
      creditEntryCount: toNumber(balanceRaw.credit_entry_count ?? balanceRaw.creditEntryCount),
    },
    from: raw.from ?? null,
    to: raw.to ?? null,
  };
}

export function mapSellerRevenueTrendResponse(raw) {
  if (!raw) {
    return { granularity: "DAY", from: null, to: null, points: [] };
  }

  const points = (raw.points ?? []).map((point) => ({
    periodStart: point.period_start ?? point.periodStart,
    recognizedAmount: toNumber(point.recognized_amount ?? point.recognizedAmount),
    itemCount: toNumber(point.item_count ?? point.itemCount),
  }));

  return {
    granularity: raw.granularity ?? "DAY",
    from: raw.from ?? null,
    to: raw.to ?? null,
    points,
  };
}

export function mapSellerLedgerResponse(raw) {
  if (!raw) {
    return { items: [], pagination: { page: 1, limit: 20, totalItems: 0, totalPages: 0, hasNext: false } };
  }

  const paginationRaw = raw.pagination ?? {};
  return {
    items: (raw.items ?? []).map((entry) => ({
      id: entry.id,
      orderItemId: entry.order_item_id ?? entry.orderItemId,
      entryType: entry.entry_type ?? entry.entryType,
      grossAmount: toNumber(entry.gross_amount ?? entry.grossAmount),
      platformFeeAmount: toNumber(entry.platform_fee_amount ?? entry.platformFeeAmount),
      netAmount: toNumber(entry.net_amount ?? entry.netAmount),
      commissionRateSnapshot: toNumber(entry.commission_rate_snapshot ?? entry.commissionRateSnapshot),
      status: entry.status,
      createdAt: entry.created_at ?? entry.createdAt,
    })),
    pagination: {
      page: toNumber(paginationRaw.page) || 1,
      limit: toNumber(paginationRaw.limit) || 20,
      totalItems: toNumber(paginationRaw.total_items ?? paginationRaw.totalItems),
      totalPages: toNumber(paginationRaw.total_pages ?? paginationRaw.totalPages),
      hasNext: Boolean(paginationRaw.has_next ?? paginationRaw.hasNext),
    },
  };
}
