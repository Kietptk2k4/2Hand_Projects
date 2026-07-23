import { DEFAULT_ADMIN_AUDIT_TAB } from "./adminAudit/adminAuditTabs.js";
import { DEFAULT_CONTENT_MODERATION_TAB } from "./contentModeration/contentModerationTabs.js";
import { DEFAULT_ORDER_SUPPORT_TAB } from "./orderSupport/orderSupportTabs.js";
import { DEFAULT_SYSTEM_OPERATIONS_TAB } from "./systemOperations/systemOperationsTabs.js";
import { DEFAULT_COMMERCE_FINANCE_TAB } from "./commerceFinance/commerceFinanceTabs.js";
import { DEFAULT_CATALOG_MANAGEMENT_TAB } from "./catalogManagement/catalogManagementTabs.js";
import {
  isRbacRoleSelectionTab,
  isRbacUserListTab,
} from "./rolePermission/rbacPageContract.js";

const VALID_SECTIONS = [
  "rolePermission",
  "userInvestigation",
  "adminAudit",
  "contentModeration",
  "orderSupport",
  "systemOperations",
  "commerceFinance",
  "catalogManagement",
];

const LEGACY_SECTION_MAP = {
  loginSession: "userInvestigation",
  commerceModeration: "contentModeration",
};

const DEFAULT_TAB_BY_SECTION = {
  rolePermission: "role-list",
  userInvestigation: "profile",
  adminAudit: DEFAULT_ADMIN_AUDIT_TAB,
  contentModeration: DEFAULT_CONTENT_MODERATION_TAB,
  orderSupport: DEFAULT_ORDER_SUPPORT_TAB,
  systemOperations: DEFAULT_SYSTEM_OPERATIONS_TAB,
  commerceFinance: DEFAULT_COMMERCE_FINANCE_TAB,
  catalogManagement: DEFAULT_CATALOG_MANAGEMENT_TAB,
};

const SUPPORT_PARAM_KEYS = [
  "orderId",
  "orderView",
  "paymentId",
  "paymentView",
  "shipmentId",
  "shipmentView",
  "refundRequestId",
  "refundView",
  "provider",
  "reference_id",
  "status",
  "from",
  "to",
  "page",
  "size",
  "pay_status",
  "pay_q",
  "pay_reconciliation_status",
  "pay_method",
  "pay_order_id",
  "pay_from",
  "pay_to",
  "pay_page",
  "pay_size",
  "ref_q",
  "ref_status",
  "ref_requested_by",
  "ref_method",
  "ref_from",
  "ref_to",
  "ref_page",
  "ref_limit",
  "sh_q",
  "sh_status",
  "sh_carrier",
  "sh_order_id",
  "sh_from",
  "sh_to",
  "sh_sort",
  "sh_page",
  "sh_size",
  "ord_q",
  "ord_status",
  "ord_payment_status",
  "ord_method",
  "ord_from",
  "ord_to",
  "ord_sort",
  "ord_page",
  "ord_size",
];

const ORDER_LIST_FILTER_KEYS = [
  "ord_q",
  "ord_status",
  "ord_payment_status",
  "ord_method",
  "ord_from",
  "ord_to",
  "ord_sort",
  "ord_page",
  "ord_size",
];

const SHIPMENT_LIST_FILTER_KEYS = [
  "sh_q",
  "sh_status",
  "sh_carrier",
  "sh_order_id",
  "sh_from",
  "sh_to",
  "sh_sort",
  "sh_page",
  "sh_size",
];

const PAYMENT_FILTER_KEYS = [
  "pay_q",
  "pay_status",
  "pay_reconciliation_status",
  "pay_method",
  "pay_order_id",
  "pay_from",
  "pay_to",
  "pay_page",
  "pay_size",
];

const REFUND_LIST_FILTER_KEYS = [
  "ref_q",
  "ref_status",
  "ref_requested_by",
  "ref_method",
  "ref_from",
  "ref_to",
  "ref_page",
  "ref_limit",
];

const WEBHOOK_FILTER_KEYS = [
  "wh_provider",
  "wh_reference_id",
  "wh_q",
  "wh_event_type",
  "wh_status",
  "wh_from",
  "wh_to",
  "wh_page",
  "wh_size",
  "wh_log_id",
  "wh_log_provider",
];

const FINANCE_OVERVIEW_FILTER_KEYS = [
  "fin_from",
  "fin_to",
  "fin_granularity",
  "fin_range",
  "fin_limit",
  "fin_ledger_page",
];

const COMMERCE_FINANCE_PARAM_KEYS = ["seller_shop"];

const PAYOUT_QUEUE_FILTER_KEYS = ["payout_status", "payout_page"];

/** URL sentinel: explicit all payout statuses (distinct from missing → default REQUESTED). */
export const PAYOUT_QUEUE_STATUS_ALL = "ALL";

const CATALOG_CATEGORY_FILTER_KEYS = ["cat_q", "cat_status"];

/** URL sentinel: explicit all category statuses. */
export const CATALOG_CATEGORY_STATUS_ALL = "ALL";

const CATALOG_BRAND_FILTER_KEYS = ["brand_q", "brand_status", "brand_page"];

/** URL sentinel: explicit all brand statuses. */
export const CATALOG_BRAND_STATUS_ALL = "ALL";

/** URL sentinel: explicit "all statuses" (distinct from missing ref_status → default REQUESTED). */
const REFUND_LIST_STATUS_ALL = "ALL";

const AUDIT_PARAM_KEYS = [
  "admin_id",
  "action",
  "target_type",
  "target_id",
  "status",
  "from",
  "to",
  "page",
  "size",
  "critical_only",
  "logId",
];


const SYSTEM_OPERATIONS_CONFIG_FILTER_KEYS = [
  "sc_q",
  "sc_value_type",
  "sc_is_active",
  "sc_page",
  "sc_size",
];

const SYSTEM_OPERATIONS_ANNOUNCEMENT_FILTER_KEYS = [
  "sa_q",
  "sa_status",
  "sa_severity",
  "sa_page",
  "sa_size",
];

const SYSTEM_OPERATIONS_MODEL_REGISTRY_FILTER_KEYS = ["mr_status"];

const SYSTEM_OPERATIONS_SELECTION_KEYS = [
  "configId",
  "configView",
  "announcementId",
  "announcementView",
  "mr_version",
  "mr_view",
];

const CONTENT_MODERATION_PARAM_KEYS = ["postId", "commentId", "shopId", "productId", "reviewId", "productView"];

const RBAC_USER_LIST_FILTER_KEYS = [
  "rbac_status",
  "rbac_q",
  "rbac_sort",
  "rbac_page",
  "rbac_size",
  "rbac_user_id",
];

const RBAC_ROLE_PARAM_KEYS = ["rbac_role_id"];

const INVESTIGATION_USER_LIST_FILTER_KEYS = [
  "inv_status",
  "inv_q",
  "inv_sort",
  "inv_page",
  "inv_size",
];

const POST_MODERATION_LIST_FILTER_KEYS = [
  "post_mod_status",
  "post_mod_moderation_status",
  "post_mod_q",
  "post_mod_sort",
  "post_mod_page",
  "post_mod_size",
];

const COMMENT_MODERATION_LIST_FILTER_KEYS = [
  "cmt_mod_status",
  "cmt_mod_moderation_status",
  "cmt_mod_post_id",
  "cmt_mod_q",
  "cmt_mod_sort",
  "cmt_mod_page",
  "cmt_mod_size",
];

const SHOP_MODERATION_LIST_FILTER_KEYS = [
  "shop_mod_status",
  "shop_mod_q",
  "shop_mod_sort",
  "shop_mod_page",
  "shop_mod_size",
];

const PRODUCT_MODERATION_LIST_FILTER_KEYS = [
  "product_mod_status",
  "product_mod_q",
  "product_mod_sort",
  "product_mod_page",
  "product_mod_size",
];

const REVIEW_MODERATION_LIST_FILTER_KEYS = [
  "review_mod_status",
  "review_mod_rating",
  "review_mod_q",
  "review_mod_sort",
  "review_mod_page",
  "review_mod_size",
];

const MANAGED_PARAM_KEYS = new Set([
  "section",
  "tab",
  "userId",
  "sellerId",
  ...SUPPORT_PARAM_KEYS,
  ...AUDIT_PARAM_KEYS,
  ...CONTENT_MODERATION_PARAM_KEYS,
  ...SYSTEM_OPERATIONS_CONFIG_FILTER_KEYS,
  ...SYSTEM_OPERATIONS_ANNOUNCEMENT_FILTER_KEYS,
  ...SYSTEM_OPERATIONS_MODEL_REGISTRY_FILTER_KEYS,
  ...SYSTEM_OPERATIONS_SELECTION_KEYS,
  ...RBAC_USER_LIST_FILTER_KEYS,
  ...RBAC_ROLE_PARAM_KEYS,
  ...INVESTIGATION_USER_LIST_FILTER_KEYS,
  ...POST_MODERATION_LIST_FILTER_KEYS,
  ...COMMENT_MODERATION_LIST_FILTER_KEYS,
  ...SHOP_MODERATION_LIST_FILTER_KEYS,
  ...PRODUCT_MODERATION_LIST_FILTER_KEYS,
  ...REVIEW_MODERATION_LIST_FILTER_KEYS,
  ...ORDER_LIST_FILTER_KEYS,
  ...SHIPMENT_LIST_FILTER_KEYS,
  ...PAYMENT_FILTER_KEYS,
  ...REFUND_LIST_FILTER_KEYS,
  ...WEBHOOK_FILTER_KEYS,
  ...FINANCE_OVERVIEW_FILTER_KEYS,
  ...COMMERCE_FINANCE_PARAM_KEYS,
  ...PAYOUT_QUEUE_FILTER_KEYS,
  ...CATALOG_CATEGORY_FILTER_KEYS,
  ...CATALOG_BRAND_FILTER_KEYS,
]);

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

export function parseOrderSupportOrderView(searchParams) {
  const value = searchParams.get("orderView") || "summary";
  if (value === "items") return "items";
  if (value === "timeline") return "timeline";
  return "summary";
}

export function parseOrderSupportPaymentId(searchParams) {
  return searchParams.get("paymentId") || "";
}

export function parseOrderSupportPaymentView(searchParams) {
  const value = searchParams.get("paymentView") || "summary";
  if (value === "timeline") return "timeline";
  if (value === "webhooks") return "webhooks";
  return "summary";
}

export function parseOrderSupportShipmentView(searchParams) {
  const value = searchParams.get("shipmentView") || "summary";
  if (value === "timeline") return "timeline";
  if (value === "webhooks") return "webhooks";
  if (value === "override") return "override";
  return "summary";
}

export function parseOrderSupportShipmentId(searchParams) {
  return searchParams.get("shipmentId") || "";
}

export function parseOrderSupportWebhookFilters(searchParams) {
  const legacyProvider = searchParams.get("wh_provider") || searchParams.get("provider") || "";
  const legacyReference = searchParams.get("wh_reference_id") || searchParams.get("reference_id") || "";
  const legacyStatus = searchParams.get("wh_status") || searchParams.get("status") || "";
  const legacyFrom = searchParams.get("wh_from") || searchParams.get("from") || "";
  const legacyTo = searchParams.get("wh_to") || searchParams.get("to") || "";
  const legacyPage = searchParams.get("wh_page") || searchParams.get("page") || "1";
  const legacySize = searchParams.get("wh_size") || searchParams.get("size") || "20";

  return {
    provider: legacyProvider,
    reference_id: legacyReference,
    q: searchParams.get("wh_q") || "",
    event_type: searchParams.get("wh_event_type") || "",
    status: legacyStatus,
    from: legacyFrom,
    to: legacyTo,
    page: legacyPage,
    size: legacySize,
  };
}

export function parseOrderSupportWebhookLogId(searchParams) {
  return searchParams.get("wh_log_id") || "";
}

export function parseOrderSupportWebhookProvider(searchParams) {
  return searchParams.get("wh_log_provider") || "";
}

export function parseOrderSupportOrderListFilters(searchParams) {
  return {
    q: searchParams.get("ord_q") || "",
    status: searchParams.get("ord_status") || "",
    payment_status: searchParams.get("ord_payment_status") || "",
    payment_method: searchParams.get("ord_method") || "",
    from: searchParams.get("ord_from") || "",
    to: searchParams.get("ord_to") || "",
    sort: searchParams.get("ord_sort") || "created_at",
    page: searchParams.get("ord_page") || "1",
    size: searchParams.get("ord_size") || "20",
  };
}

export function parseOrderSupportShipmentListFilters(searchParams) {
  return {
    q: searchParams.get("sh_q") || "",
    status: searchParams.get("sh_status") || "",
    carrier: searchParams.get("sh_carrier") || "",
    order_id: searchParams.get("sh_order_id") || "",
    from: searchParams.get("sh_from") || "",
    to: searchParams.get("sh_to") || "",
    sort: searchParams.get("sh_sort") || "updated_at",
    page: searchParams.get("sh_page") || "1",
    size: searchParams.get("sh_size") || "20",
  };
}

export function parseOrderSupportPaymentFilters(searchParams) {
  return {
    q: searchParams.get("pay_q") || "",
    status: searchParams.get("pay_status") || "",
    reconciliation_status: searchParams.get("pay_reconciliation_status") || "",
    payment_method: searchParams.get("pay_method") || "",
    order_id: searchParams.get("pay_order_id") || "",
    from: searchParams.get("pay_from") || "",
    to: searchParams.get("pay_to") || "",
    page: searchParams.get("pay_page") || "1",
    size: searchParams.get("pay_size") || "20",
  };
}

export function parseOrderSupportRefundRequestId(searchParams) {
  return searchParams.get("refundRequestId") || "";
}

export function parseOrderSupportRefundView(searchParams) {
  const value = searchParams.get("refundView") || "summary";
  if (value === "detail") return "detail";
  if (value === "note") return "note";
  return "summary";
}

function parseRefundListStatusFilter(searchParams) {
  if (!searchParams.has("ref_status")) {
    return "REQUESTED";
  }
  const raw = searchParams.get("ref_status") || "";
  if (!raw || raw === REFUND_LIST_STATUS_ALL) {
    return "";
  }
  return raw;
}

export function parseOrderSupportRefundListFilters(searchParams) {
  return {
    q: searchParams.get("ref_q") || "",
    status: parseRefundListStatusFilter(searchParams),
    requested_by: searchParams.get("ref_requested_by") || "",
    payment_method: searchParams.get("ref_method") || "",
    from: searchParams.get("ref_from") || "",
    to: searchParams.get("ref_to") || "",
    page: searchParams.get("ref_page") || "1",
    limit: searchParams.get("ref_limit") || "20",
  };
}

export function parseAdminAuditFilters(searchParams) {
  return {
    admin_id: searchParams.get("admin_id") || "",
    action: searchParams.get("action") || "",
    target_type: searchParams.get("target_type") || "",
    target_id: searchParams.get("target_id") || "",
    status: searchParams.get("status") || "",
    from: searchParams.get("from") || "",
    to: searchParams.get("to") || "",
    page: searchParams.get("page") || "1",
    size: searchParams.get("size") || "20",
    critical_only: searchParams.get("critical_only") || "",
  };
}

export function parseAdminAuditLogId(searchParams) {
  return searchParams.get("logId") || "";
}

export function parseContentModerationPostId(searchParams) {
  return searchParams.get("postId") || "";
}

export function parseContentModerationCommentId(searchParams) {
  return searchParams.get("commentId") || "";
}

export function parseContentModerationShopId(searchParams) {
  return searchParams.get("shopId") || "";
}

export function parseContentModerationProductId(searchParams) {
  return searchParams.get("productId") || "";
}

export function parseContentModerationReviewId(searchParams) {
  return searchParams.get("reviewId") || "";
}



export function parseSystemOperationsConfigFilters(searchParams) {
  return {
    q: searchParams.get("sc_q") || "",
    value_type: searchParams.get("sc_value_type") || "",
    is_active: searchParams.get("sc_is_active") || "",
    page: searchParams.get("sc_page") || "1",
    size: searchParams.get("sc_size") || "20",
  };
}

export function parseSystemOperationsAnnouncementFilters(searchParams) {
  return {
    q: searchParams.get("sa_q") || "",
    status: searchParams.get("sa_status") || "",
    severity: searchParams.get("sa_severity") || "",
    page: searchParams.get("sa_page") || "1",
    size: searchParams.get("sa_size") || "20",
  };
}

export function parseSystemOperationsConfigId(searchParams) {
  return searchParams.get("configId") || "";
}

export function parseSystemOperationsConfigView(searchParams) {
  const value = searchParams.get("configView") || "edit";
  return value === "history" ? "history" : "edit";
}

export function parseSystemOperationsAnnouncementId(searchParams) {
  return searchParams.get("announcementId") || "";
}

export function parseSystemOperationsAnnouncementView(searchParams) {
  const value = searchParams.get("announcementView") || "detail";
  return value === "actions" ? "actions" : "detail";
}

export function parseSystemOperationsModelRegistryFilters(searchParams) {
  return {
    status: searchParams.get("mr_status") || "",
  };
}

export function parseSystemOperationsModelRegistryVersion(searchParams) {
  return searchParams.get("mr_version") || "";
}

export function parseSystemOperationsModelRegistryView(searchParams) {
  const value = searchParams.get("mr_view") || "detail";
  return value === "metrics" ? "metrics" : "detail";
}

export function parseContentModerationProductView(searchParams) {
  const value = searchParams.get("productView") || "list";
  return value === "history" ? "history" : "list";
}

export function parseCommerceFinanceSellerId(searchParams) {
  return searchParams.get("sellerId") || "";
}

export function parseCommerceFinanceSellerShop(searchParams) {
  return searchParams.get("seller_shop") || "";
}

export function parseCommerceFinanceOverviewFilters(searchParams) {
  return {
    from: searchParams.get("fin_from") || "",
    to: searchParams.get("fin_to") || "",
    granularity: searchParams.get("fin_granularity") || "DAY",
    range: searchParams.get("fin_range") || "30d",
    limit: searchParams.get("fin_limit") || "",
    ledgerPage: searchParams.get("fin_ledger_page") || "1",
  };
}

export function parseCatalogCategoryFilters(searchParams) {
  const rawStatus = searchParams.get("cat_status");
  let status = "";
  if (rawStatus === "active") {
    status = "active";
  } else if (rawStatus === "inactive") {
    status = "inactive";
  }
  return {
    q: searchParams.get("cat_q") || "",
    status,
  };
}

export function parseCatalogBrandFilters(searchParams) {
  const rawStatus = searchParams.get("brand_status");
  let status = "";
  if (rawStatus === "active") {
    status = "active";
  } else if (rawStatus === "inactive") {
    status = "inactive";
  }
  return {
    q: searchParams.get("brand_q") || "",
    status,
    page: searchParams.get("brand_page") || "1",
  };
}

export function parseCommerceFinancePayoutQueueFilters(searchParams) {
  const rawStatus = searchParams.get("payout_status");
  let status = "REQUESTED";
  if (rawStatus === PAYOUT_QUEUE_STATUS_ALL) {
    status = "";
  } else if (rawStatus) {
    status = rawStatus;
  }
  return {
    status,
    page: searchParams.get("payout_page") || "1",
  };
}

function applyFinanceOverviewFilterParams(next, filters) {
  if (!filters) return;
  const { from, to, granularity, range, limit, ledgerPage } = filters;
  if (from) next.set("fin_from", from);
  if (to) next.set("fin_to", to);
  if (granularity && granularity !== "DAY") next.set("fin_granularity", granularity);
  if (range && range !== "30d") next.set("fin_range", range);
  if (limit && String(limit) !== "25") next.set("fin_limit", String(limit));
  if (ledgerPage && String(ledgerPage) !== "1") next.set("fin_ledger_page", String(ledgerPage));
}

function applyCatalogCategoryFilterParams(next, filters) {
  if (!filters) return;
  const { q, status } = filters;
  if (q) next.set("cat_q", q);
  if (status === "") {
    next.set("cat_status", CATALOG_CATEGORY_STATUS_ALL);
  } else if (status) {
    next.set("cat_status", status);
  }
}

function applyCatalogBrandFilterParams(next, filters) {
  if (!filters) return;
  const { q, status, page } = filters;
  if (q) next.set("brand_q", q);
  if (status === "") {
    next.set("brand_status", CATALOG_BRAND_STATUS_ALL);
  } else if (status) {
    next.set("brand_status", status);
  }
  if (page && String(page) !== "1") next.set("brand_page", String(page));
}

function applyPayoutQueueFilterParams(next, filters) {
  if (!filters) return;
  const { status, page } = filters;
  if (status === "") {
    next.set("payout_status", PAYOUT_QUEUE_STATUS_ALL);
  } else if (status && status !== "REQUESTED") {
    next.set("payout_status", status);
  } else if (status === "REQUESTED") {
    next.set("payout_status", "REQUESTED");
  }
  if (page && String(page) !== "1") next.set("payout_page", String(page));
}

export function parseRbacUserListFilters(searchParams) {
  return {
    status: searchParams.get("rbac_status") || "",
    q: searchParams.get("rbac_q") || "",
    sort: searchParams.get("rbac_sort") || "email",
    page: searchParams.get("rbac_page") || "1",
    size: searchParams.get("rbac_size") || "20",
  };
}

export function parseRbacSelectedUserId(searchParams) {
  return searchParams.get("rbac_user_id") || "";
}

export function parseRbacSelectedRoleId(searchParams) {
  return searchParams.get("rbac_role_id") || "";
}

export function parseInvestigationUserListFilters(searchParams) {
  return {
    status: searchParams.get("inv_status") || "",
    q: searchParams.get("inv_q") || "",
    sort: searchParams.get("inv_sort") || "created_at",
    page: searchParams.get("inv_page") || "1",
    size: searchParams.get("inv_size") || "20",
  };
}

export function parsePostModerationListFilters(searchParams) {
  return {
    status: searchParams.get("post_mod_status") || "",
    moderation_status: searchParams.get("post_mod_moderation_status") || "",
    q: searchParams.get("post_mod_q") || "",
    sort: searchParams.get("post_mod_sort") || "created_at",
    page: searchParams.get("post_mod_page") || "1",
    size: searchParams.get("post_mod_size") || "20",
  };
}

export function parseCommentModerationListFilters(searchParams) {
  return {
    status: searchParams.get("cmt_mod_status") || "",
    moderation_status: searchParams.get("cmt_mod_moderation_status") || "",
    post_id: searchParams.get("cmt_mod_post_id") || "",
    q: searchParams.get("cmt_mod_q") || "",
    sort: searchParams.get("cmt_mod_sort") || "created_at",
    page: searchParams.get("cmt_mod_page") || "1",
    size: searchParams.get("cmt_mod_size") || "20",
  };
}

export function parseShopModerationListFilters(searchParams) {
  return {
    status: searchParams.get("shop_mod_status") || "",
    q: searchParams.get("shop_mod_q") || "",
    sort: searchParams.get("shop_mod_sort") || "NEWEST",
    page: searchParams.get("shop_mod_page") || "1",
    size: searchParams.get("shop_mod_size") || "20",
  };
}

export function parseProductModerationListFilters(searchParams) {
  return {
    status:
      searchParams.get("product_mod_status") ||
      searchParams.get("productStatus") ||
      "",
    q: searchParams.get("product_mod_q") || searchParams.get("q") || "",
    sort: searchParams.get("product_mod_sort") || "NEWEST",
    page: searchParams.get("product_mod_page") || "1",
    size: searchParams.get("product_mod_size") || "20",
  };
}

function normalizeLegacyReviewStatus(raw) {
  if (!raw) return "";
  const legacy = { all: "", visible: "VISIBLE", hidden: "HIDDEN" };
  return legacy[String(raw).toLowerCase()] || raw;
}

export function parseReviewModerationListFilters(searchParams) {
  const legacyStatus = searchParams.get("review_mod_status") || searchParams.get("reviewStatus") || "";
  return {
    status: normalizeLegacyReviewStatus(legacyStatus),
    rating: searchParams.get("review_mod_rating") || searchParams.get("rating") || "",
    q: searchParams.get("review_mod_q") || searchParams.get("q") || "",
    sort: searchParams.get("review_mod_sort") || "NEWEST",
    page: searchParams.get("review_mod_page") || "1",
    size: searchParams.get("review_mod_size") || "20",
  };
}

function copyUnmanagedParams(next, preserve) {
  if (!preserve) return;
  for (const [key, value] of preserve.entries()) {
    if (!MANAGED_PARAM_KEYS.has(key)) {
      next.set(key, value);
    }
  }
}

function applyAuditParams(next, auditFilters, logId) {
  if (!auditFilters) return;

  const {
    admin_id: adminId,
    action,
    target_type: targetType,
    target_id: targetId,
    status,
    from,
    to,
    page,
    size,
    critical_only: criticalOnly,
  } = auditFilters;

  if (adminId) next.set("admin_id", adminId);
  if (action) next.set("action", action);
  if (targetType) next.set("target_type", targetType);
  if (targetId) next.set("target_id", targetId);
  if (status) next.set("status", status);
  if (from) next.set("from", from);
  if (to) next.set("to", to);
  if (page) next.set("page", String(page));
  if (size) next.set("size", String(size));
  if (criticalOnly === "true") next.set("critical_only", "true");

  if (logId) next.set("logId", logId);
}


function applySystemOperationsParams(
  next,
  {
    tab,
    configFilters,
    announcementFilters,
    modelRegistryFilters,
    configId,
    configView,
    announcementId,
    announcementView,
    mrVersion,
    mrView,
    clearConfigSelection,
    clearAnnouncementSelection,
    clearModelRegistrySelection,
    preserve,
  },
) {
  if (tab === "system-configs" && configFilters) {
    const { q, value_type: valueType, is_active: isActive, page, size } = configFilters;
    if (q) next.set("sc_q", q);
    if (valueType) next.set("sc_value_type", valueType);
    if (isActive) next.set("sc_is_active", isActive);
    if (page) next.set("sc_page", String(page));
    if (size) next.set("sc_size", String(size));
  } else if (tab === "system-configs" && preserve) {
    for (const key of SYSTEM_OPERATIONS_CONFIG_FILTER_KEYS) {
      const value = preserve.get(key);
      if (value) next.set(key, value);
    }
  }

  if (tab === "system-announcements" && announcementFilters) {
    const { q, status, severity, page, size } = announcementFilters;
    if (q) next.set("sa_q", q);
    if (status) next.set("sa_status", status);
    if (severity) next.set("sa_severity", severity);
    if (page) next.set("sa_page", String(page));
    if (size) next.set("sa_size", String(size));
  } else if (tab === "system-announcements" && preserve) {
    for (const key of SYSTEM_OPERATIONS_ANNOUNCEMENT_FILTER_KEYS) {
      const value = preserve.get(key);
      if (value) next.set(key, value);
    }
  }

  if (tab === "model-registry" && modelRegistryFilters) {
    const { status } = modelRegistryFilters;
    if (status) next.set("mr_status", status);
  } else if (tab === "model-registry" && preserve) {
    for (const key of SYSTEM_OPERATIONS_MODEL_REGISTRY_FILTER_KEYS) {
      const value = preserve.get(key);
      if (value) next.set(key, value);
    }
  }

  if (clearConfigSelection && clearAnnouncementSelection && clearModelRegistrySelection) {
    return;
  }

  if (!clearConfigSelection) {
    if (configId) next.set("configId", configId);
    if (configView && configView !== "edit") next.set("configView", configView);
  }

  if (!clearAnnouncementSelection) {
    if (announcementId) next.set("announcementId", announcementId);
    if (announcementView && announcementView !== "detail") next.set("announcementView", announcementView);
  }

  if (!clearModelRegistrySelection) {
    if (mrVersion) next.set("mr_version", String(mrVersion));
    if (mrView && mrView !== "detail") next.set("mr_view", mrView);
  }
}

function applyContentModerationParams(next, { postId, commentId, shopId, productId, reviewId }) {
  if (postId) next.set("postId", postId);
  if (commentId) next.set("commentId", commentId);
  if (shopId) next.set("shopId", shopId);
  if (productId) next.set("productId", productId);
  if (reviewId) next.set("reviewId", reviewId);
}

function applyPaymentFilterParams(next, paymentFilters) {
  if (!paymentFilters) return;

  const {
    q,
    status,
    reconciliation_status: reconciliationStatus,
    payment_method: paymentMethod,
    order_id: orderId,
    from,
    to,
    page,
    size,
  } = paymentFilters;

  if (q) next.set("pay_q", q);
  if (status) next.set("pay_status", status);
  if (reconciliationStatus) next.set("pay_reconciliation_status", reconciliationStatus);
  if (paymentMethod) next.set("pay_method", paymentMethod);
  if (orderId) next.set("pay_order_id", orderId);
  if (from) next.set("pay_from", from);
  if (to) next.set("pay_to", to);
  if (page) next.set("pay_page", String(page));
  if (size && String(size) !== "20") next.set("pay_size", String(size));
}

function applyOrderListFilterParams(next, orderListFilters) {
  if (!orderListFilters) return;

  const {
    q,
    status,
    payment_status: paymentStatus,
    payment_method: paymentMethod,
    from,
    to,
    sort,
    page,
    size,
  } = orderListFilters;

  if (q) next.set("ord_q", q);
  if (status) next.set("ord_status", status);
  if (paymentStatus) next.set("ord_payment_status", paymentStatus);
  if (paymentMethod) next.set("ord_method", paymentMethod);
  if (from) next.set("ord_from", from);
  if (to) next.set("ord_to", to);
  if (sort && sort !== "created_at") next.set("ord_sort", sort);
  if (page) next.set("ord_page", String(page));
  if (size && String(size) !== "20") next.set("ord_size", String(size));
}

function applyShipmentListFilterParams(next, shipmentListFilters) {
  if (!shipmentListFilters) return;

  const { q, status, carrier, order_id, from, to, sort, page, size } = shipmentListFilters;
  if (q) next.set("sh_q", q);
  if (status) next.set("sh_status", status);
  if (carrier) next.set("sh_carrier", carrier);
  if (order_id) next.set("sh_order_id", order_id);
  if (from) next.set("sh_from", from);
  if (to) next.set("sh_to", to);
  if (sort && sort !== "updated_at") next.set("sh_sort", sort);
  if (page) next.set("sh_page", String(page));
  if (size && String(size) !== "20") next.set("sh_size", String(size));
}

function applyWebhookFilterParams(next, webhookFilters) {
  if (!webhookFilters) return;

  const {
    provider,
    reference_id: referenceId,
    q,
    event_type: eventType,
    status,
    from,
    to,
    page,
    size,
  } = webhookFilters;

  if (provider) next.set("wh_provider", provider);
  if (referenceId) next.set("wh_reference_id", referenceId);
  if (q) next.set("wh_q", q);
  if (eventType) next.set("wh_event_type", eventType);
  if (status) next.set("wh_status", status);
  if (from) next.set("wh_from", from);
  if (to) next.set("wh_to", to);
  if (page) next.set("wh_page", String(page));
  if (size && String(size) !== "20") next.set("wh_size", String(size));
}

function applyRefundListFilterParams(next, refundListFilters) {
  if (!refundListFilters) return;

  const {
    q,
    status,
    requested_by: requestedBy,
    payment_method: paymentMethod,
    from,
    to,
    page,
    limit,
  } = refundListFilters;

  if (q) next.set("ref_q", q);
  // Persist explicit status; empty string means all statuses (ALL sentinel survives URL serialization).
  next.set("ref_status", status || REFUND_LIST_STATUS_ALL);
  if (requestedBy) next.set("ref_requested_by", requestedBy);
  if (paymentMethod) next.set("ref_method", paymentMethod);
  if (from) next.set("ref_from", from);
  if (to) next.set("ref_to", to);
  if (page) next.set("ref_page", String(page));
  if (limit && String(limit) !== "20") next.set("ref_limit", String(limit));
}

function applyRbacUserListFilterParams(next, rbacUserListFilters, rbacUserId) {
  if (!rbacUserListFilters) return;

  const { status, q, sort, page, size } = rbacUserListFilters;
  if (status) next.set("rbac_status", status);
  if (q) next.set("rbac_q", q);
  if (sort && sort !== "email") next.set("rbac_sort", sort);
  if (page) next.set("rbac_page", String(page));
  if (size && String(size) !== "20") next.set("rbac_size", String(size));
  if (rbacUserId) next.set("rbac_user_id", rbacUserId);
}

function applyRbacSectionParams(next, { tab, rbacUserListFilters, rbacUserId, rbacRoleId }) {
  if (isRbacRoleSelectionTab(tab) && rbacRoleId) {
    next.set("rbac_role_id", rbacRoleId);
  }

  if (isRbacUserListTab(tab) && rbacUserListFilters) {
    applyRbacUserListFilterParams(next, rbacUserListFilters, rbacUserId);
  }
}

function applyInvestigationUserListFilterParams(next, investigationUserListFilters) {
  if (!investigationUserListFilters) return;

  const { status, q, sort, page, size } = investigationUserListFilters;
  if (status) next.set("inv_status", status);
  if (q) next.set("inv_q", q);
  if (sort && sort !== "created_at") next.set("inv_sort", sort);
  if (page) next.set("inv_page", String(page));
  if (size && String(size) !== "20") next.set("inv_size", String(size));
}

function applyPostModerationListFilterParams(next, postModerationListFilters) {
  if (!postModerationListFilters) return;

  const { status, moderation_status: moderationStatus, q, sort, page, size } =
    postModerationListFilters;
  if (status) next.set("post_mod_status", status);
  if (moderationStatus) next.set("post_mod_moderation_status", moderationStatus);
  if (q) next.set("post_mod_q", q);
  if (sort && sort !== "created_at") next.set("post_mod_sort", sort);
  if (page) next.set("post_mod_page", String(page));
  if (size && String(size) !== "20") next.set("post_mod_size", String(size));
}

function applyCommentModerationListFilterParams(next, commentModerationListFilters) {
  if (!commentModerationListFilters) return;

  const { status, moderation_status: moderationStatus, post_id: postId, q, sort, page, size } =
    commentModerationListFilters;
  if (status) next.set("cmt_mod_status", status);
  if (moderationStatus) next.set("cmt_mod_moderation_status", moderationStatus);
  if (postId) next.set("cmt_mod_post_id", postId);
  if (q) next.set("cmt_mod_q", q);
  if (sort && sort !== "created_at") next.set("cmt_mod_sort", sort);
  if (page) next.set("cmt_mod_page", String(page));
  if (size && String(size) !== "20") next.set("cmt_mod_size", String(size));
}

function applyShopModerationListFilterParams(next, shopModerationListFilters) {
  if (!shopModerationListFilters) return;

  const { status, q, sort, page, size } = shopModerationListFilters;
  if (status) next.set("shop_mod_status", status);
  if (q) next.set("shop_mod_q", q);
  if (sort && sort !== "NEWEST") next.set("shop_mod_sort", sort);
  if (page) next.set("shop_mod_page", String(page));
  if (size && String(size) !== "20") next.set("shop_mod_size", String(size));
}

function applyProductModerationListFilterParams(next, productModerationListFilters) {
  if (!productModerationListFilters) return;

  const { status, q, sort, page, size } = productModerationListFilters;
  if (status) next.set("product_mod_status", status);
  if (q) next.set("product_mod_q", q);
  if (sort && sort !== "NEWEST") next.set("product_mod_sort", sort);
  if (page) next.set("product_mod_page", String(page));
  if (size && String(size) !== "20") next.set("product_mod_size", String(size));
}

function applyReviewModerationListFilterParams(next, reviewModerationListFilters) {
  if (!reviewModerationListFilters) return;

  const { status, rating, q, sort, page, size } = reviewModerationListFilters;
  if (status) next.set("review_mod_status", status);
  if (rating) next.set("review_mod_rating", rating);
  if (q) next.set("review_mod_q", q);
  if (sort && sort !== "NEWEST") next.set("review_mod_sort", sort);
  if (page) next.set("review_mod_page", String(page));
  if (size && String(size) !== "20") next.set("review_mod_size", String(size));
}

export function buildAdminSearchParams({
  section,
  tab,
  userId,
  sellerId,
  sellerShop,
  orderId,
  orderView,
  clearOrderSelection = false,
  paymentId,
  paymentView,
  clearPaymentSelection = false,
  shipmentId,
  shipmentView,
  clearShipmentSelection = false,
  refundRequestId,
  refundView,
  clearRefundSelection = false,
  webhookFilters,
  webhookLogId,
  webhookLogProvider,
  clearWebhookSelection = false,
  paymentFilters,
  orderListFilters,
  shipmentListFilters,
  refundListFilters,
  auditFilters,
  logId,
  clearLogId = false,
  postId,
  commentId,
  shopId,
  productId,
  reviewId,
  configFilters,
  announcementFilters,
  configId,
  configView,
  clearConfigSelection = false,
  announcementId,
  announcementView,
  clearAnnouncementSelection = false,
  modelRegistryFilters,
  mrVersion,
  mrView,
  clearModelRegistrySelection = false,
  rbacUserListFilters,
  rbacUserId,
  rbacRoleId,
  investigationUserListFilters,
  postModerationListFilters,
  commentModerationListFilters,
  shopModerationListFilters,
  productModerationListFilters,
  reviewModerationListFilters,
  financeOverviewFilters,
  payoutQueueFilters,
  catalogCategoryFilters,
  catalogBrandFilters,
  preserve,
}) {
  const next = new URLSearchParams();
  copyUnmanagedParams(next, preserve);

  next.set("section", section);
  next.set("tab", tab);

  const resolvedUserId =
    userId !== undefined
      ? userId || null
      : preserve
        ? preserve.get("userId")
        : null;
  if (resolvedUserId) {
    next.set("userId", resolvedUserId);
  }

  const resolvedSellerId = sellerId ?? (preserve ? preserve.get("sellerId") : null);
  if (resolvedSellerId) {
    next.set("sellerId", resolvedSellerId);
  }

  const resolvedSellerShop =
    sellerShop !== undefined
      ? sellerShop || null
      : preserve
        ? preserve.get("seller_shop")
        : null;
  if (resolvedSellerShop) {
    next.set("seller_shop", resolvedSellerShop);
  }

  const resolvedOrderId = clearOrderSelection
    ? undefined
    : orderId ?? (preserve ? preserve.get("orderId") : null);
  if (resolvedOrderId) {
    next.set("orderId", resolvedOrderId);
  }

  const resolvedOrderView = clearOrderSelection
    ? undefined
    : orderView ??
      (preserve?.get("orderView") === "items"
        ? "items"
        : preserve?.get("orderView") === "timeline"
          ? "timeline"
          : undefined);
  if (resolvedOrderView && resolvedOrderView !== "summary") {
    next.set("orderView", resolvedOrderView);
  }

  const resolvedPaymentId = clearPaymentSelection
    ? undefined
    : paymentId ?? (preserve ? preserve.get("paymentId") : null);
  if (resolvedPaymentId) {
    next.set("paymentId", resolvedPaymentId);
  }

  const resolvedPaymentView = clearPaymentSelection
    ? undefined
    : paymentView ??
      (preserve?.get("paymentView") === "timeline"
        ? "timeline"
        : preserve?.get("paymentView") === "webhooks"
          ? "webhooks"
          : undefined);
  if (resolvedPaymentView && resolvedPaymentView !== "summary") {
    next.set("paymentView", resolvedPaymentView);
  }

  const resolvedShipmentId = clearShipmentSelection
    ? undefined
    : shipmentId ?? (preserve ? preserve.get("shipmentId") : null);
  if (resolvedShipmentId) {
    next.set("shipmentId", resolvedShipmentId);
  }

  const resolvedShipmentView = clearShipmentSelection
    ? undefined
    : shipmentView ??
      (preserve?.get("shipmentView") === "timeline"
        ? "timeline"
        : preserve?.get("shipmentView") === "webhooks"
          ? "webhooks"
          : preserve?.get("shipmentView") === "override"
            ? "override"
            : undefined);
  if (resolvedShipmentView && resolvedShipmentView !== "summary") {
    next.set("shipmentView", resolvedShipmentView);
  }

  const resolvedRefundRequestId = clearRefundSelection
    ? null
    : refundRequestId ?? (preserve ? preserve.get("refundRequestId") : null);
  if (resolvedRefundRequestId) {
    next.set("refundRequestId", resolvedRefundRequestId);
  }

  const resolvedRefundView = clearRefundSelection
    ? null
    : refundView ??
      (preserve?.get("refundView") === "detail"
        ? "detail"
        : preserve?.get("refundView") === "note"
          ? "note"
          : null);
  if (resolvedRefundView && resolvedRefundView !== "summary") {
    next.set("refundView", resolvedRefundView);
  }

  if (section === "orderSupport") {
    if (webhookFilters) {
      applyWebhookFilterParams(next, webhookFilters);
    } else if (tab === "webhook-logs" && preserve) {
      for (const key of WEBHOOK_FILTER_KEYS) {
        const value = preserve.get(key);
        if (value) next.set(key, value);
      }
    }

    if (webhookLogId) {
      next.set("wh_log_id", webhookLogId);
    } else if (clearWebhookSelection) {
      next.delete("wh_log_id");
      next.delete("wh_log_provider");
    }

    if (webhookLogProvider) {
      next.set("wh_log_provider", webhookLogProvider);
    } else if (clearWebhookSelection) {
      next.delete("wh_log_provider");
    }

    if (paymentFilters) {
      applyPaymentFilterParams(next, paymentFilters);
    } else if (tab === "payment-detail" && preserve) {
      for (const key of PAYMENT_FILTER_KEYS) {
        const value = preserve.get(key);
        if (value) next.set(key, value);
      }
    }

    if (orderListFilters) {
      applyOrderListFilterParams(next, orderListFilters);
    } else if (tab === "order-detail" && preserve) {
      for (const key of ORDER_LIST_FILTER_KEYS) {
        const value = preserve.get(key);
        if (value) next.set(key, value);
      }
    }

    if (shipmentListFilters) {
      applyShipmentListFilterParams(next, shipmentListFilters);
    } else if (tab === "shipment-detail" && preserve) {
      for (const key of SHIPMENT_LIST_FILTER_KEYS) {
        const value = preserve.get(key);
        if (value) next.set(key, value);
      }
    }

    if (refundListFilters) {
      applyRefundListFilterParams(next, refundListFilters);
    } else if (tab === "refund-approvals" && preserve) {
      for (const key of REFUND_LIST_FILTER_KEYS) {
        const value = preserve.get(key);
        if (value) next.set(key, value);
      }
    }
  }

  if (section === "adminAudit") {
    const resolvedAuditFilters =
      auditFilters ??
      (preserve
        ? {
            admin_id: preserve.get("admin_id") || "",
            action: preserve.get("action") || "",
            target_type: preserve.get("target_type") || "",
            target_id: preserve.get("target_id") || "",
            status: preserve.get("status") || "",
            from: preserve.get("from") || "",
            to: preserve.get("to") || "",
            page: preserve.get("page") || "1",
            size: preserve.get("size") || "20",
            critical_only: preserve.get("critical_only") || "",
          }
        : undefined);

    const resolvedLogId = clearLogId
      ? null
      : logId ?? (preserve ? preserve.get("logId") : null);
    applyAuditParams(next, resolvedAuditFilters, resolvedLogId);
  }


  if (section === "systemOperations") {
    const resolvedTab = tab || preserve?.get("tab") || DEFAULT_SYSTEM_OPERATIONS_TAB;
    applySystemOperationsParams(next, {
      tab: resolvedTab,
      configFilters:
        configFilters ??
        (resolvedTab === "system-configs" && preserve
          ? parseSystemOperationsConfigFilters(preserve)
          : undefined),
      announcementFilters:
        announcementFilters ??
        (resolvedTab === "system-announcements" && preserve
          ? parseSystemOperationsAnnouncementFilters(preserve)
          : undefined),
      modelRegistryFilters:
        modelRegistryFilters ??
        (resolvedTab === "model-registry" && preserve
          ? parseSystemOperationsModelRegistryFilters(preserve)
          : undefined),
      configId: clearConfigSelection
        ? undefined
        : configId ?? (preserve ? preserve.get("configId") : null),
      configView: clearConfigSelection
        ? undefined
        : configView ??
          (preserve?.get("configView") === "history" ? "history" : undefined),
      announcementId: clearAnnouncementSelection
        ? undefined
        : announcementId ?? (preserve ? preserve.get("announcementId") : null),
      announcementView: clearAnnouncementSelection
        ? undefined
        : announcementView ??
          (preserve?.get("announcementView") === "actions" ? "actions" : undefined),
      mrVersion: clearModelRegistrySelection
        ? undefined
        : mrVersion ?? (preserve ? preserve.get("mr_version") : null),
      mrView: clearModelRegistrySelection
        ? undefined
        : mrView ?? (preserve?.get("mr_view") === "metrics" ? "metrics" : undefined),
      clearConfigSelection,
      clearAnnouncementSelection,
      clearModelRegistrySelection,
      preserve,
    });
  }

  if (section === "contentModeration") {
    applyContentModerationParams(next, {
      postId: postId ?? (preserve ? preserve.get("postId") : null),
      commentId: commentId ?? (preserve ? preserve.get("commentId") : null),
      shopId: shopId ?? (preserve ? preserve.get("shopId") : null),
      productId: productId ?? (preserve ? preserve.get("productId") : null),
      reviewId: reviewId ?? (preserve ? preserve.get("reviewId") : null),
    });
    applyPostModerationListFilterParams(
      next,
      postModerationListFilters ??
        (preserve ? parsePostModerationListFilters(preserve) : undefined),
    );
    applyCommentModerationListFilterParams(
      next,
      commentModerationListFilters ??
        (preserve ? parseCommentModerationListFilters(preserve) : undefined),
    );
    applyShopModerationListFilterParams(
      next,
      shopModerationListFilters ??
        (preserve ? parseShopModerationListFilters(preserve) : undefined),
    );
    applyProductModerationListFilterParams(
      next,
      productModerationListFilters ??
        (preserve ? parseProductModerationListFilters(preserve) : undefined),
    );
    applyReviewModerationListFilterParams(
      next,
      reviewModerationListFilters ??
        (preserve ? parseReviewModerationListFilters(preserve) : undefined),
    );
  }

  if (section === "rolePermission") {
    const resolvedRbacFilters =
      rbacUserListFilters ??
      (preserve ? parseRbacUserListFilters(preserve) : undefined);
    const resolvedRbacUserId =
      rbacUserId !== undefined
        ? rbacUserId || undefined
        : preserve
          ? parseRbacSelectedUserId(preserve) || undefined
          : undefined;
    const resolvedRbacRoleId =
      rbacRoleId ?? (preserve ? parseRbacSelectedRoleId(preserve) || undefined : undefined);
    applyRbacSectionParams(next, {
      tab,
      rbacUserListFilters: resolvedRbacFilters,
      rbacUserId: resolvedRbacUserId,
      rbacRoleId: resolvedRbacRoleId,
    });
  }

  if (section === "userInvestigation") {
    const resolvedInvestigationFilters =
      investigationUserListFilters ??
      (preserve ? parseInvestigationUserListFilters(preserve) : undefined);
    applyInvestigationUserListFilterParams(next, resolvedInvestigationFilters);
  }

  if (section === "commerceFinance") {
    if (financeOverviewFilters) {
      applyFinanceOverviewFilterParams(next, financeOverviewFilters);
    } else if (preserve) {
      for (const key of FINANCE_OVERVIEW_FILTER_KEYS) {
        const value = preserve.get(key);
        if (value) next.set(key, value);
      }
      for (const key of COMMERCE_FINANCE_PARAM_KEYS) {
        const value = preserve.get(key);
        if (value) next.set(key, value);
      }
    }
    if (payoutQueueFilters) {
      applyPayoutQueueFilterParams(next, payoutQueueFilters);
    } else if (preserve) {
      for (const key of PAYOUT_QUEUE_FILTER_KEYS) {
        const value = preserve.get(key);
        if (value) next.set(key, value);
      }
    }
  }

  if (section === "catalogManagement") {
    if (catalogCategoryFilters) {
      applyCatalogCategoryFilterParams(next, catalogCategoryFilters);
    } else if (preserve) {
      for (const key of CATALOG_CATEGORY_FILTER_KEYS) {
        const value = preserve.get(key);
        if (value) next.set(key, value);
      }
    }
    if (catalogBrandFilters) {
      applyCatalogBrandFilterParams(next, catalogBrandFilters);
    } else if (preserve) {
      for (const key of CATALOG_BRAND_FILTER_KEYS) {
        const value = preserve.get(key);
        if (value) next.set(key, value);
      }
    }
  }

  return next;
}