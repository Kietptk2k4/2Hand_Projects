import { getAllSellerOrderItemRecords } from "./commerceSellerOrderData";
import { getShopBySellerId } from "./commerceSellerShopData";
import { getSellerPaidPayoutAmount, getSellerPendingPayoutAmount } from "./commerceSellerPayoutData";

const EXCLUDED_STATUSES = new Set(["CANCELLED", "FAILED", "RETURNED", "PENDING"]);
const VALID_GRANULARITIES = new Set(["DAY", "WEEK", "MONTH"]);
const COMMISSION_RATE = 0.1;

function calcLedgerAmounts(gross) {
  const platformFee = Math.round(gross * COMMISSION_RATE);
  return {
    gross_amount: gross,
    platform_fee_amount: platformFee,
    net_amount: gross - platformFee,
  };
}

export function buildLedgerEntriesForUser(userId) {
  return getAllSellerOrderItemRecords(userId)
    .filter(isRecognizedItem)
    .map((item) => {
      const amounts = calcLedgerAmounts(item.final_price || 0);
      return {
        id: `ledger-${item.order_item_id}`,
        order_item_id: item.order_item_id,
        entry_type: "CREDIT",
        gross_amount: amounts.gross_amount,
        platform_fee_amount: amounts.platform_fee_amount,
        net_amount: amounts.net_amount,
        commission_rate_snapshot: COMMISSION_RATE,
        status: "POSTED",
        created_at: item.item_updated_at || item.item_created_at,
      };
    })
    .sort((a, b) => String(b.created_at).localeCompare(String(a.created_at)));
}

export function buildBalanceFromLedger(entries) {
  const totals = entries.reduce(
    (acc, entry) => {
      acc.available_balance += entry.net_amount;
      acc.total_platform_fee += entry.platform_fee_amount;
      acc.total_net_credited += entry.net_amount;
      acc.credit_entry_count += 1;
      return acc;
    },
    {
      available_balance: 0,
      total_platform_fee: 0,
      total_net_credited: 0,
      credit_entry_count: 0,
    },
  );
  return totals;
}

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

function addBucket(target, bucketKey, amount) {
  target[bucketKey].amount += amount;
  target[bucketKey].item_count += 1;
}

export function buildSellerRevenueSummary(userId) {
  const shop = getShopBySellerId(userId);
  if (!shop) {
    return { error: "COMMERCE-409-SELLER-SHOP", status: 409, message: "Seller chua co shop." };
  }

  const buckets = {
    in_transit: emptyBucket(),
    pending_confirm: emptyBucket(),
    recognized: emptyBucket(),
  };

  for (const item of getAllSellerOrderItemRecords(userId)) {
    const bucketKey = bucketForItem(item);
    if (!bucketKey) continue;
    addBucket(buckets, bucketKey, item.final_price || 0);
  }

  const totalGross =
    buckets.in_transit.amount + buckets.pending_confirm.amount + buckets.recognized.amount;
  const ledgerBalance = buildBalanceFromLedger(buildLedgerEntriesForUser(userId));
  const pendingPayoutAmount = getSellerPendingPayoutAmount(userId);
  const paidPayoutAmount = getSellerPaidPayoutAmount(userId);
  const balance = {
    ...ledgerBalance,
    pending_payout_amount: pendingPayoutAmount,
    available_balance: Math.max(
      0,
      ledgerBalance.available_balance - pendingPayoutAmount - paidPayoutAmount,
    ),
  };

  return {
    data: {
      in_transit: buckets.in_transit,
      pending_confirm: buckets.pending_confirm,
      recognized: buckets.recognized,
      total_gross: totalGross,
      balance,
      from: null,
      to: null,
    },
  };
}

function startOfUtcDay(date) {
  return new Date(Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate()));
}

function truncatePeriod(date, granularity) {
  const utc = startOfUtcDay(date);
  if (granularity === "MONTH") {
    return new Date(Date.UTC(utc.getUTCFullYear(), utc.getUTCMonth(), 1)).toISOString();
  }
  if (granularity === "WEEK") {
    const day = utc.getUTCDay();
    const diff = day === 0 ? -6 : 1 - day;
    utc.setUTCDate(utc.getUTCDate() + diff);
    return utc.toISOString();
  }
  return utc.toISOString();
}

function defaultTrendRange() {
  const to = startOfUtcDay(new Date());
  to.setUTCDate(to.getUTCDate() + 1);
  const from = new Date(to);
  from.setUTCDate(from.getUTCDate() - 30);
  return { from, to };
}

export function buildSellerRevenueTrend(userId, { granularity = "DAY" } = {}) {
  const shop = getShopBySellerId(userId);
  if (!shop) {
    return { error: "COMMERCE-409-SELLER-SHOP", status: 409, message: "Seller chua co shop." };
  }

  const parsedGranularity = String(granularity || "DAY").toUpperCase();
  if (!VALID_GRANULARITIES.has(parsedGranularity)) {
    return {
      error: "COMMERCE-400-VALIDATION",
      status: 400,
      message: "granularity must be one of DAY, WEEK, MONTH",
    };
  }

  const { from, to } = defaultTrendRange();
  const grouped = new Map();

  for (const entry of buildLedgerEntriesForUser(userId)) {
    const completedAt = new Date(entry.created_at);
    if (Number.isNaN(completedAt.getTime()) || completedAt < from || completedAt >= to) continue;

    const key = truncatePeriod(completedAt, parsedGranularity);
    const current = grouped.get(key) || { recognized_amount: 0, item_count: 0 };
    current.recognized_amount += entry.gross_amount || 0;
    current.item_count += 1;
    grouped.set(key, current);
  }

  const points = [...grouped.entries()]
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([periodStart, value]) => ({
      period_start: periodStart,
      recognized_amount: value.recognized_amount,
      item_count: value.item_count,
    }));

  return {
    data: {
      granularity: parsedGranularity,
      from: from.toISOString(),
      to: to.toISOString(),
      points,
    },
  };
}

export function listSellerLedger(userId, { page = 1, limit = 20 } = {}) {
  const shop = getShopBySellerId(userId);
  if (!shop) {
    return { error: "COMMERCE-409-SELLER-SHOP", status: 409, message: "Seller chua co shop." };
  }

  const entries = buildLedgerEntriesForUser(userId);
  const safePage = Math.max(1, Number(page) || 1);
  const safeLimit = Math.min(50, Math.max(1, Number(limit) || 20));
  const offset = (safePage - 1) * safeLimit;
  const items = entries.slice(offset, offset + safeLimit);
  const totalItems = entries.length;
  const totalPages = totalItems === 0 ? 0 : Math.ceil(totalItems / safeLimit);

  return {
    data: {
      items,
      pagination: {
        page: safePage,
        limit: safeLimit,
        total_items: totalItems,
        total_pages: totalPages,
        has_next: safePage < totalPages,
      },
    },
  };
}
