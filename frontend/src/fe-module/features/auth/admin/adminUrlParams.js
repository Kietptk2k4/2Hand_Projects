import { DEFAULT_COMMERCE_MODERATION_TAB } from "./commerceModeration/commerceModerationTabs.js";
import { DEFAULT_ORDER_SUPPORT_TAB } from "./orderSupport/orderSupportTabs.js";

const VALID_SECTIONS = [
  "rolePermission",
  "userInvestigation",
  "commerceModeration",
  "orderSupport",
];

const LEGACY_SECTION_MAP = {
  loginSession: "userInvestigation",
};

const DEFAULT_TAB_BY_SECTION = {
  rolePermission: "role-list",
  userInvestigation: "profile",
  commerceModeration: DEFAULT_COMMERCE_MODERATION_TAB,
  orderSupport: DEFAULT_ORDER_SUPPORT_TAB,
};

const SUPPORT_PARAM_KEYS = [
  "orderId",
  "paymentId",
  "shipmentId",
  "provider",
  "reference_id",
  "status",
  "from",
  "to",
  "page",
  "size",
];

export function parseAdminSection(searchParams) {
  const raw = searchParams.get("section");
  const mapped = LEGACY_SECTION_MAP[raw] || raw;
  if (!mapped || !VALID_SECTIONS.includes(mapped)) {
    return "rolePermission";
  }
  return mapped;
}

export function parseAdminTab(searchParams, section) {
  const raw = searchParams.get("tab");
  if (raw) return raw;
  return DEFAULT_TAB_BY_SECTION[section] || DEFAULT_TAB_BY_SECTION.rolePermission;
}

export function parseInvestigationUserId(searchParams) {
  return searchParams.get("userId") || "";
}

export function parseOrderSupportOrderId(searchParams) {
  return searchParams.get("orderId") || "";
}

export function parseOrderSupportPaymentId(searchParams) {
  return searchParams.get("paymentId") || "";
}

export function parseOrderSupportShipmentId(searchParams) {
  return searchParams.get("shipmentId") || "";
}

export function parseOrderSupportWebhookFilters(searchParams) {
  return {
    provider: searchParams.get("provider") || "",
    reference_id: searchParams.get("reference_id") || "",
    status: searchParams.get("status") || "",
    from: searchParams.get("from") || "",
    to: searchParams.get("to") || "",
    page: searchParams.get("page") || "1",
    size: searchParams.get("size") || "20",
  };
}

export function buildAdminSearchParams({
  section,
  tab,
  userId,
  orderId,
  paymentId,
  shipmentId,
  webhookFilters,
  preserve,
}) {
  const next = new URLSearchParams();

  if (preserve) {
    for (const [key, value] of preserve.entries()) {
      if (
        key !== "section" &&
        key !== "tab" &&
        key !== "userId" &&
        !SUPPORT_PARAM_KEYS.includes(key)
      ) {
        next.set(key, value);
      }
    }
  }

  next.set("section", section);
  next.set("tab", tab);

  const resolvedUserId = userId ?? (preserve ? preserve.get("userId") : null);
  if (resolvedUserId) {
    next.set("userId", resolvedUserId);
  }

  const resolvedOrderId = orderId ?? (preserve ? preserve.get("orderId") : null);
  if (resolvedOrderId) {
    next.set("orderId", resolvedOrderId);
  }

  const resolvedPaymentId = paymentId ?? (preserve ? preserve.get("paymentId") : null);
  if (resolvedPaymentId) {
    next.set("paymentId", resolvedPaymentId);
  }

  const resolvedShipmentId = shipmentId ?? (preserve ? preserve.get("shipmentId") : null);
  if (resolvedShipmentId) {
    next.set("shipmentId", resolvedShipmentId);
  }

  if (webhookFilters) {
    const {
      provider,
      reference_id: referenceId,
      status,
      from,
      to,
      page,
      size,
    } = webhookFilters;
    if (provider) next.set("provider", provider);
    if (referenceId) next.set("reference_id", referenceId);
    if (status) next.set("status", status);
    if (from) next.set("from", from);
    if (to) next.set("to", to);
    if (page) next.set("page", String(page));
    if (size) next.set("size", String(size));
  } else if (section === "orderSupport" && tab === "webhook-logs" && preserve) {
    for (const key of ["provider", "reference_id", "status", "from", "to", "page", "size"]) {
      const value = preserve.get(key);
      if (value) next.set(key, value);
    }
  }

  return next;
}
