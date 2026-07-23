function toNumber(value) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
}

function mapBucket(raw) {
  if (!raw) return { amount: 0, itemCount: 0 };
  return {
    amount: toNumber(raw.amount),
    itemCount: toNumber(raw.item_count ?? raw.itemCount),
  };
}

export function mapPlatformFinanceSummary(raw) {
  if (!raw) {
    return {
      recognizedGmv: 0,
      recognizedItemCount: 0,
      totalPlatformFee: 0,
      codPipelineAmount: 0,
      pendingPayoutCount: 0,
      pendingPayoutAmount: 0,
      paidPayoutAmount: 0,
      from: null,
      to: null,
    };
  }
  return {
    recognizedGmv: toNumber(raw.recognized_gmv ?? raw.recognizedGmv),
    recognizedItemCount: toNumber(raw.recognized_item_count ?? raw.recognizedItemCount),
    totalPlatformFee: toNumber(raw.total_platform_fee ?? raw.totalPlatformFee),
    codPipelineAmount: toNumber(raw.cod_pipeline_amount ?? raw.codPipelineAmount),
    pendingPayoutCount: toNumber(raw.pending_payout_count ?? raw.pendingPayoutCount),
    pendingPayoutAmount: toNumber(raw.pending_payout_amount ?? raw.pendingPayoutAmount),
    paidPayoutAmount: toNumber(raw.paid_payout_amount ?? raw.paidPayoutAmount),
    from: raw.from ?? null,
    to: raw.to ?? null,
  };
}

export function mapPlatformRevenueTrend(raw) {
  if (!raw) return { granularity: "DAY", from: null, to: null, points: [] };
  return {
    granularity: raw.granularity ?? "DAY",
    from: raw.from ?? null,
    to: raw.to ?? null,
    points: (raw.points ?? []).map((point) => ({
      periodStart: point.period_start ?? point.periodStart,
      gmvAmount: toNumber(point.gmv_amount ?? point.gmvAmount),
      platformFeeAmount: toNumber(point.platform_fee_amount ?? point.platformFeeAmount),
      itemCount: toNumber(point.item_count ?? point.itemCount),
    })),
  };
}

export function mapPlatformCodPipeline(raw) {
  if (!raw) {
    return {
      inTransit: { amount: 0, itemCount: 0 },
      pendingConfirm: { amount: 0, itemCount: 0 },
      recognized: { amount: 0, itemCount: 0 },
    };
  }
  return {
    inTransit: mapBucket(raw.in_transit ?? raw.inTransit),
    pendingConfirm: mapBucket(raw.pending_confirm ?? raw.pendingConfirm),
    recognized: mapBucket(raw.recognized),
  };
}

export function mapPlatformTopSellers(raw) {
  const items = raw?.items ?? raw ?? [];
  return items.map((item) => ({
    sellerId: item.seller_id ?? item.sellerId,
    shopName: item.shop_name ?? item.shopName,
    recognizedGross: toNumber(item.recognized_gross ?? item.recognizedGross),
    platformFee: toNumber(item.platform_fee ?? item.platformFee),
    completedItemCount: toNumber(item.completed_item_count ?? item.completedItemCount),
  }));
}

export function mapPlatformPayoutOverview(raw) {
  const items = raw?.items ?? raw ?? [];
  return items.map((item) => ({
    status: item.status ?? "",
    requestCount: toNumber(item.request_count ?? item.requestCount),
    totalAmount: toNumber(item.total_amount ?? item.totalAmount),
  }));
}

export function mapSellerFinanceSummary(raw) {
  const balanceRaw = raw?.balance ?? {};
  return {
    inTransit: mapBucket(raw?.in_transit ?? raw?.inTransit),
    pendingConfirm: mapBucket(raw?.pending_confirm ?? raw?.pendingConfirm),
    recognized: mapBucket(raw?.recognized),
    totalGross: toNumber(raw?.total_gross ?? raw?.totalGross),
    balance: {
      availableBalance: toNumber(balanceRaw.available_balance ?? balanceRaw.availableBalance),
      totalPlatformFee: toNumber(balanceRaw.total_platform_fee ?? balanceRaw.totalPlatformFee),
      totalNetCredited: toNumber(balanceRaw.total_net_credited ?? balanceRaw.totalNetCredited),
      pendingPayoutAmount: toNumber(balanceRaw.pending_payout_amount ?? balanceRaw.pendingPayoutAmount),
    },
    from: raw?.from ?? null,
    to: raw?.to ?? null,
  };
}

export function mapSellerFinanceLedger(raw) {
  if (!raw) return { items: [], pagination: { page: 1, totalItems: 0 } };
  const paginationRaw = raw.pagination ?? {};
  return {
    items: (raw.items ?? []).map((entry) => ({
      id: entry.id,
      orderItemId: entry.order_item_id ?? entry.orderItemId,
      entryType: entry.entry_type ?? entry.entryType,
      grossAmount: toNumber(entry.gross_amount ?? entry.grossAmount),
      platformFeeAmount: toNumber(entry.platform_fee_amount ?? entry.platformFeeAmount),
      netAmount: toNumber(entry.net_amount ?? entry.netAmount),
      commissionRateSnapshot: toNumber(
        entry.commission_rate_snapshot ?? entry.commissionRateSnapshot,
      ),
      status: entry.status ?? "",
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
