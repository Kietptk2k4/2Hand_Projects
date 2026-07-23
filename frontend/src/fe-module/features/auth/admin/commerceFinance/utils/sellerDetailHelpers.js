import { feeRatePercent } from "./financeOverviewHelpers.js";
import { computeCodPipelineMetrics } from "./codPipelineHelpers.js";

const UUID_PATTERN =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

export function isValidSellerUuid(value) {
  return UUID_PATTERN.test(String(value || "").trim());
}

export function summaryToPipelineShape(summary) {
  if (!summary) {
    return {
      inTransit: { amount: 0, itemCount: 0 },
      pendingConfirm: { amount: 0, itemCount: 0 },
      recognized: { amount: 0, itemCount: 0 },
    };
  }
  return {
    inTransit: summary.inTransit ?? { amount: 0, itemCount: 0 },
    pendingConfirm: summary.pendingConfirm ?? { amount: 0, itemCount: 0 },
    recognized: summary.recognized ?? { amount: 0, itemCount: 0 },
  };
}

export function computeSellerBucketShares(summary) {
  return computeCodPipelineMetrics(summaryToPipelineShape(summary)).shares;
}

export function computeSellerDetailHeroMetrics(summary) {
  const pipeline = summaryToPipelineShape(summary);
  const totalGross = Number(summary?.totalGross) || 0;
  const recognizedGmv = Number(pipeline.recognized?.amount) || 0;
  const totalPlatformFee = Number(summary?.balance?.totalPlatformFee) || 0;

  return {
    availableBalance: Number(summary?.balance?.availableBalance) || 0,
    pendingPayout: Number(summary?.balance?.pendingPayoutAmount) || 0,
    periodGmv: totalGross,
    recognizedGmv,
    recognizedItems: Number(pipeline.recognized?.itemCount) || 0,
    totalPlatformFee,
    feeRatePeriod: feeRatePercent(totalGross, totalPlatformFee),
  };
}

export function parseLedgerPage(raw, fallback = 1) {
  const page = Number(raw);
  return Number.isFinite(page) && page >= 1 ? Math.floor(page) : fallback;
}

export const SELLER_LEDGER_PAGE_SIZE = 20;
