import { feeRatePercent } from "./financeOverviewHelpers.js";

export function computeTopSellersListMetrics(sellers = []) {
  const list = Array.isArray(sellers) ? sellers : [];
  const totalGross = list.reduce((sum, s) => sum + (Number(s.recognizedGross) || 0), 0);
  const totalFee = list.reduce((sum, s) => sum + (Number(s.platformFee) || 0), 0);
  const totalItems = list.reduce((sum, s) => sum + (Number(s.completedItemCount) || 0), 0);
  const topGross = Number(list[0]?.recognizedGross) || 0;
  const topShare = totalGross > 0 ? (topGross / totalGross) * 100 : 0;

  return {
    count: list.length,
    totalGross,
    totalFee,
    totalItems,
    feeRate: feeRatePercent(totalGross, totalFee),
    topShare,
    topShopName: list[0]?.shopName || "",
  };
}

export function sellerFeeRatePercent(seller) {
  return feeRatePercent(seller?.recognizedGross, seller?.platformFee);
}

export function truncateSellerId(sellerId, head = 8, tail = 4) {
  const id = String(sellerId || "");
  if (id.length <= head + tail + 1) return id;
  return `${id.slice(0, head)}…${id.slice(-tail)}`;
}

export function parseTopSellersLimit(raw, fallback = 25) {
  const n = Number(raw);
  if (n === 10 || n === 25 || n === 50) return n;
  return fallback;
}
