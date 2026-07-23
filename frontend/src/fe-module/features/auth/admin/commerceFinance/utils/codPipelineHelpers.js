import { COD_BUCKET_LABELS } from "../constants/financeOverviewConstants.js";

export const COD_STAGE_META = [
  {
    key: "inTransit",
    label: COD_BUCKET_LABELS.inTransit,
    hint: "PROCESSING / SHIPPED",
    icon: "local_shipping",
    accentClass: "text-admin-accent",
    surfaceClass: "bg-admin-accent-soft/50",
    barClass: "bg-admin-accent",
  },
  {
    key: "pendingConfirm",
    label: COD_BUCKET_LABELS.pendingConfirm,
    hint: "DELIVERED — chờ xác nhận",
    icon: "inventory_2",
    accentClass: "text-admin-warning",
    surfaceClass: "bg-admin-warning-soft/60",
    barClass: "bg-admin-warning",
  },
  {
    key: "recognized",
    label: COD_BUCKET_LABELS.recognized,
    hint: "COMPLETED + PAID",
    icon: "payments",
    accentClass: "text-admin-success",
    surfaceClass: "bg-admin-success-soft/60",
    barClass: "bg-admin-success",
  },
];

export function getCodBucket(pipeline, key) {
  return pipeline?.[key] ?? { amount: 0, itemCount: 0 };
}

export function computeCodPipelineMetrics(pipeline) {
  const inTransit = getCodBucket(pipeline, "inTransit");
  const pendingConfirm = getCodBucket(pipeline, "pendingConfirm");
  const recognized = getCodBucket(pipeline, "recognized");

  const pipelineAmount = inTransit.amount + pendingConfirm.amount;
  const pipelineItemCount = inTransit.itemCount + pendingConfirm.itemCount;
  const totalAmount = pipelineAmount + recognized.amount;
  const totalItemCount = pipelineItemCount + recognized.itemCount;

  const share = (amount) => (totalAmount > 0 ? (amount / totalAmount) * 100 : 0);

  const atRiskDenom = pipelineAmount;
  const pendingShareOfPipeline =
    atRiskDenom > 0 ? (pendingConfirm.amount / atRiskDenom) * 100 : 0;
  const recognizedShareOfTotal = totalAmount > 0 ? (recognized.amount / totalAmount) * 100 : 0;
  const inFlightShareOfTotal = totalAmount > 0 ? (pipelineAmount / totalAmount) * 100 : 0;

  return {
    inTransit,
    pendingConfirm,
    recognized,
    pipelineAmount,
    pipelineItemCount,
    totalAmount,
    totalItemCount,
    shares: {
      inTransit: share(inTransit.amount),
      pendingConfirm: share(pendingConfirm.amount),
      recognized: share(recognized.amount),
    },
    conversion: {
      pendingShareOfPipeline,
      recognizedShareOfTotal,
      inFlightShareOfTotal,
    },
    isEmpty: totalAmount <= 0 && totalItemCount <= 0,
  };
}

export function formatSharePercent(value) {
  if (!Number.isFinite(value)) return "—";
  return `${(Math.round(value * 10) / 10).toFixed(1)}%`;
}

/** Approximate deep-links into Order Support (order/shipment list filters). */
export function buildCodStageDeepLinkParams(stageKey) {
  if (stageKey === "inTransit") {
    return {
      section: "orderSupport",
      tab: "order-detail",
      orderListFilters: { status: "PROCESSING", page: "1" },
    };
  }
  if (stageKey === "pendingConfirm") {
    return {
      section: "orderSupport",
      tab: "shipment-detail",
      shipmentListFilters: { status: "DELIVERED", page: "1" },
    };
  }
  if (stageKey === "recognized") {
    return {
      section: "orderSupport",
      tab: "order-detail",
      orderListFilters: {
        status: "COMPLETED",
        payment_status: "PAID",
        page: "1",
      },
    };
  }
  return { section: "orderSupport", tab: "order-detail" };
}
