import { MOCK_CART_DEMO_USER_ID } from "./commerceCartData";
import { buildSellerRevenueSummary, buildSellerRevenueTrend, listSellerLedger } from "./commerceSellerFinanceData";
import { getShopBySellerId } from "./commerceSellerShopData";
import { getAllSellerOrderItemRecords } from "./commerceSellerOrderData";

const COMMISSION_RATE = 0.1;
const EXCLUDED_STATUSES = new Set(["CANCELLED", "FAILED", "RETURNED", "PENDING"]);

function isRecognizedItem(item) {
  return item.item_status === "COMPLETED" && item.payment?.status === "PAID";
}

function bucketForItem(item) {
  if (EXCLUDED_STATUSES.has(item.item_status)) return null;
  if (item.item_status === "PROCESSING" || item.item_status === "SHIPPED") return "in_transit";
  if (item.item_status === "DELIVERED") return "pending_confirm";
  if (isRecognizedItem(item)) return "recognized";
  return null;
}

function emptyBucket() {
  return { amount: 0, item_count: 0 };
}

function getPlatformItems() {
  return getAllSellerOrderItemRecords(MOCK_CART_DEMO_USER_ID);
}

function buildPlatformBuckets() {
  const buckets = {
    in_transit: emptyBucket(),
    pending_confirm: emptyBucket(),
    recognized: emptyBucket(),
  };
  for (const item of getPlatformItems()) {
    const key = bucketForItem(item);
    if (!key) continue;
    buckets[key].amount += item.final_price || 0;
    buckets[key].item_count += 1;
  }
  return buckets;
}

function defaultRange() {
  const to = new Date();
  const from = new Date(to);
  from.setUTCDate(from.getUTCDate() - 30);
  return { from: from.toISOString(), to: to.toISOString() };
}

export function buildPlatformFinanceSummary() {
  const buckets = buildPlatformBuckets();
  const recognized = buckets.recognized;
  const totalPlatformFee = Math.round(recognized.amount * COMMISSION_RATE);
  const codPipeline = buckets.in_transit.amount + buckets.pending_confirm.amount;
  const sellerSummary = buildSellerRevenueSummary(MOCK_CART_DEMO_USER_ID);
  const balance = sellerSummary.data?.balance ?? {};
  const range = defaultRange();

  return {
    data: {
      recognized_gmv: recognized.amount,
      recognized_item_count: recognized.item_count,
      total_platform_fee: totalPlatformFee,
      cod_pipeline_amount: codPipeline,
      pending_payout_count: 0,
      pending_payout_amount: balance.pending_payout_amount ?? 0,
      paid_payout_amount: 0,
      from: range.from,
      to: range.to,
    },
  };
}

export function buildPlatformCodPipeline() {
  const buckets = buildPlatformBuckets();
  return {
    data: {
      in_transit: buckets.in_transit,
      pending_confirm: buckets.pending_confirm,
      recognized: buckets.recognized,
    },
  };
}

export function buildPlatformRevenueTrend({ granularity = "DAY" } = {}) {
  const trend = buildSellerRevenueTrend(MOCK_CART_DEMO_USER_ID, { granularity });
  if (trend.error) return trend;
  const points = (trend.data?.points ?? []).map((point) => ({
    period_start: point.period_start,
    gmv_amount: point.recognized_amount,
    platform_fee_amount: Math.round((point.recognized_amount || 0) * COMMISSION_RATE),
    item_count: point.item_count,
  }));
  return {
    data: {
      granularity: trend.data.granularity,
      from: trend.data.from,
      to: trend.data.to,
      points,
    },
  };
}

export function buildPlatformTopSellers({ limit = 10 } = {}) {
  const shop = getShopBySellerId(MOCK_CART_DEMO_USER_ID);
  const buckets = buildPlatformBuckets();
  const recognizedGross = buckets.recognized.amount;
  const items = shop
    ? [
        {
          seller_id: MOCK_CART_DEMO_USER_ID,
          shop_name: shop.shop_name,
          recognized_gross: recognizedGross,
          platform_fee: Math.round(recognizedGross * COMMISSION_RATE),
          completed_item_count: buckets.recognized.item_count,
        },
      ]
    : [];
  return {
    data: {
      items: items.slice(0, Math.max(1, Number(limit) || 10)),
    },
  };
}

export function buildAdminSellerFinanceSummary(sellerId) {
  return buildSellerRevenueSummary(sellerId);
}

export function buildAdminSellerFinanceLedger(sellerId, { page = 1, limit = 20 } = {}) {
  return listSellerLedger(sellerId, { page, limit });
}
