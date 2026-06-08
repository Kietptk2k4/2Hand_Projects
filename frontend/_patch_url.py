from pathlib import Path

p = Path(r"d:/Projects/2Hand_Projects/frontend/src/fe-module/features/auth/admin/adminUrlParams.js")
text = p.read_text(encoding="utf-8")

if "DEFAULT_SYSTEM_OPERATIONS_TAB" not in text:
    text = text.replace(
        'import { DEFAULT_ORDER_SUPPORT_TAB } from "./orderSupport/orderSupportTabs.js";',
        'import { DEFAULT_ORDER_SUPPORT_TAB } from "./orderSupport/orderSupportTabs.js";\nimport { DEFAULT_SYSTEM_OPERATIONS_TAB } from "./systemOperations/systemOperationsTabs.js";',
    )

if '"systemOperations"' not in text:
    text = text.replace(
        '  "orderSupport",\n];',
        '  "orderSupport",\n  "systemOperations",\n];',
    )

if "systemOperations:" not in text:
    text = text.replace(
        '  orderSupport: DEFAULT_ORDER_SUPPORT_TAB,\n};',
        '  orderSupport: DEFAULT_ORDER_SUPPORT_TAB,\n  systemOperations: DEFAULT_SYSTEM_OPERATIONS_TAB,\n};',
    )

if "SYSTEM_OPERATIONS_PARAM_KEYS" not in text:
    insert_keys = """
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
"""
    text = text.replace(
        "const CONTENT_MODERATION_PARAM_KEYS",
        insert_keys + "\nconst CONTENT_MODERATION_PARAM_KEYS",
    )

    text = text.replace(
        "  ...CONTENT_MODERATION_PARAM_KEYS,\n]);",
        "  ...CONTENT_MODERATION_PARAM_KEYS,\n  ...SYSTEM_OPERATIONS_CONFIG_FILTER_KEYS,\n  ...SYSTEM_OPERATIONS_ANNOUNCEMENT_FILTER_KEYS,\n  ...SYSTEM_OPERATIONS_SELECTION_KEYS,\n]);",
    )

parse_fns = """

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
"""

if "parseSystemOperationsConfigFilters" not in text:
    text = text.replace(
        "export function parseContentModerationProductView(searchParams) {",
        parse_fns + "\nexport function parseContentModerationProductView(searchParams) {",
    )

apply_fn = """
function applySystemOperationsParams(
  next,
  { tab, configFilters, announcementFilters, configId, configView, clearConfigSelection },
) {
  if (tab === "system-configs" && configFilters) {
    const { q, value_type: valueType, is_active: isActive, page, size } = configFilters;
    if (q) next.set("sc_q", q);
    if (valueType) next.set("sc_value_type", valueType);
    if (isActive) next.set("sc_is_active", isActive);
    if (page) next.set("sc_page", String(page));
    if (size) next.set("sc_size", String(size));
  } else if (tab === "system-configs") {
    for (const key of SYSTEM_OPERATIONS_CONFIG_FILTER_KEYS) {
      const value = next.get(key);
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
"""

if "applySystemOperationsParams" not in text:
    text = text.replace(
        "function applyContentModerationParams(next, { postId, commentId, productId, productView }) {",
        apply_fn + "\nfunction applyContentModerationParams(next, { postId, commentId, productId, productView }) {",
    )

if "section === \"systemOperations\"" not in text:
    build_insert = """
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
    });
  }
"""
    text = text.replace(
        "  if (section === \"contentModeration\") {",
        build_insert + "\n  if (section === \"contentModeration\") {",
    )

if "configFilters," not in text.split("export function buildAdminSearchParams")[1][:400]:
    text = text.replace(
        "  productView,\n  preserve,\n}) {",
        "  productView,\n  configFilters,\n  announcementFilters,\n  configId,\n  configView,\n  clearConfigSelection = false,\n  preserve,\n}) {",
    )

p.write_text(text, encoding="utf-8")
print("adminUrlParams patched")