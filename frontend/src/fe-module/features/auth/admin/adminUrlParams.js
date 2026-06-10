import { DEFAULT_ADMIN_AUDIT_TAB } from "./adminAudit/adminAuditTabs.js";
import { DEFAULT_CONTENT_MODERATION_TAB } from "./contentModeration/contentModerationTabs.js";
import { DEFAULT_ORDER_SUPPORT_TAB } from "./orderSupport/orderSupportTabs.js";
import { DEFAULT_SYSTEM_OPERATIONS_TAB } from "./systemOperations/systemOperationsTabs.js";
import { DEFAULT_COMMERCE_FINANCE_TAB } from "./commerceFinance/commerceFinanceTabs.js";

const VALID_SECTIONS = [
  "rolePermission",
  "userInvestigation",
  "adminAudit",
  "contentModeration",
  "orderSupport",
  "systemOperations",
  "commerceFinance",
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
  "pay_status",
  "pay_method",
  "pay_order_id",
  "pay_from",
  "pay_to",
  "pay_page",
  "pay_size",
  "sh_status",
  "sh_carrier",
  "sh_sort",
  "sh_page",
  "sh_size",
  "ord_status",
  "ord_method",
  "ord_from",
  "ord_to",
  "ord_sort",
  "ord_page",
  "ord_size",
];

const ORDER_LIST_FILTER_KEYS = [
  "ord_status",
  "ord_method",
  "ord_from",
  "ord_to",
  "ord_sort",
  "ord_page",
  "ord_size",
];

const SHIPMENT_LIST_FILTER_KEYS = [
  "sh_status",
  "sh_carrier",
  "sh_sort",
  "sh_page",
  "sh_size",
];

const PAYMENT_FILTER_KEYS = [
  "pay_status",
  "pay_method",
  "pay_order_id",
  "pay_from",
  "pay_to",
  "pay_page",
  "pay_size",
];

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

const SYSTEM_OPERATIONS_SELECTION_KEYS = ["configId", "configView"];

const CONTENT_MODERATION_PARAM_KEYS = ["postId", "commentId", "productId", "productView"];

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
  ...SYSTEM_OPERATIONS_SELECTION_KEYS,
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

export function parseOrderSupportOrderListFilters(searchParams) {
  return {
    status: searchParams.get("ord_status") || "",
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
    status: searchParams.get("sh_status") || "",
    carrier: searchParams.get("sh_carrier") || "",
    sort: searchParams.get("sh_sort") || "updated_at",
    page: searchParams.get("sh_page") || "1",
    size: searchParams.get("sh_size") || "20",
  };
}

export function parseOrderSupportPaymentFilters(searchParams) {
  return {
    status: searchParams.get("pay_status") || "",
    payment_method: searchParams.get("pay_method") || "",
    order_id: searchParams.get("pay_order_id") || "",
    from: searchParams.get("pay_from") || "",
    to: searchParams.get("pay_to") || "",
    page: searchParams.get("pay_page") || "1",
    size: searchParams.get("pay_size") || "20",
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

export function parseContentModerationProductId(searchParams) {
  return searchParams.get("productId") || "";
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

export function parseContentModerationProductView(searchParams) {
  const value = searchParams.get("productView") || "list";
  return value === "history" ? "history" : "list";
}

export function parseCommerceFinanceSellerId(searchParams) {
  return searchParams.get("sellerId") || "";
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

  if (logId) next.set("logId", logId);
}


function applySystemOperationsParams(
  next,
  { tab, configFilters, announcementFilters, configId, configView, clearConfigSelection, preserve },
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
  }

  if (clearConfigSelection) {
    return;
  }

  if (configId) next.set("configId", configId);
  if (configView && configView !== "edit") next.set("configView", configView);
}

function applyContentModerationParams(next, { postId, commentId, productId, productView }) {
  if (postId) next.set("postId", postId);
  if (commentId) next.set("commentId", commentId);
  if (productId) next.set("productId", productId);
  if (productView && productView !== "list") next.set("productView", productView);
}

function applyPaymentFilterParams(next, paymentFilters) {
  if (!paymentFilters) return;

  const {
    status,
    payment_method: paymentMethod,
    order_id: orderId,
    from,
    to,
    page,
    size,
  } = paymentFilters;

  if (status) next.set("pay_status", status);
  if (paymentMethod) next.set("pay_method", paymentMethod);
  if (orderId) next.set("pay_order_id", orderId);
  if (from) next.set("pay_from", from);
  if (to) next.set("pay_to", to);
  if (page) next.set("pay_page", String(page));
  if (size) next.set("pay_size", String(size));
}

function applyOrderListFilterParams(next, orderListFilters) {
  if (!orderListFilters) return;

  const {
    status,
    payment_method: paymentMethod,
    from,
    to,
    sort,
    page,
    size,
  } = orderListFilters;

  if (status) next.set("ord_status", status);
  if (paymentMethod) next.set("ord_method", paymentMethod);
  if (from) next.set("ord_from", from);
  if (to) next.set("ord_to", to);
  if (sort && sort !== "created_at") next.set("ord_sort", sort);
  if (page) next.set("ord_page", String(page));
  if (size && String(size) !== "20") next.set("ord_size", String(size));
}

function applyShipmentListFilterParams(next, shipmentListFilters) {
  if (!shipmentListFilters) return;

  const { status, carrier, sort, page, size } = shipmentListFilters;
  if (status) next.set("sh_status", status);
  if (carrier) next.set("sh_carrier", carrier);
  if (sort && sort !== "updated_at") next.set("sh_sort", sort);
  if (page) next.set("sh_page", String(page));
  if (size && String(size) !== "20") next.set("sh_size", String(size));
}

export function buildAdminSearchParams({
  section,
  tab,
  userId,
  sellerId,
  orderId,
  paymentId,
  shipmentId,
  webhookFilters,
  paymentFilters,
  orderListFilters,
  shipmentListFilters,
  auditFilters,
  logId,
  clearLogId = false,
  postId,
  commentId,
  productId,
  productView,
  configFilters,
  announcementFilters,
  configId,
  configView,
  clearConfigSelection = false,
  preserve,
}) {
  const next = new URLSearchParams();
  copyUnmanagedParams(next, preserve);

  next.set("section", section);
  next.set("tab", tab);

  const resolvedUserId = userId ?? (preserve ? preserve.get("userId") : null);
  if (resolvedUserId) {
    next.set("userId", resolvedUserId);
  }

  const resolvedSellerId = sellerId ?? (preserve ? preserve.get("sellerId") : null);
  if (resolvedSellerId) {
    next.set("sellerId", resolvedSellerId);
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

  if (section === "orderSupport") {
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
    } else if (tab === "webhook-logs" && preserve) {
      for (const key of ["provider", "reference_id", "status", "from", "to", "page", "size"]) {
        const value = preserve.get(key);
        if (value) next.set(key, value);
      }
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
      configId: clearConfigSelection
        ? undefined
        : configId ?? (preserve ? preserve.get("configId") : null),
      configView: clearConfigSelection
        ? undefined
        : configView ??
          (preserve?.get("configView") === "history" ? "history" : undefined),
      clearConfigSelection,
      preserve,
    });
  }

  if (section === "contentModeration") {
    applyContentModerationParams(next, {
      postId: postId ?? (preserve ? preserve.get("postId") : null),
      commentId: commentId ?? (preserve ? preserve.get("commentId") : null),
      productId: productId ?? (preserve ? preserve.get("productId") : null),
      productView:
        productView ??
        (preserve?.get("productView") === "history" ? "history" : undefined),
    });
  }

  return next;
}