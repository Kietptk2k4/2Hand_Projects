import { DEFAULT_COMMERCE_MODERATION_TAB } from "./commerceModeration/commerceModerationTabs.js";

const VALID_SECTIONS = ["rolePermission", "userInvestigation", "commerceModeration"];

const LEGACY_SECTION_MAP = {
  loginSession: "userInvestigation",
};

const DEFAULT_TAB_BY_SECTION = {
  rolePermission: "role-list",
  userInvestigation: "profile",
  commerceModeration: DEFAULT_COMMERCE_MODERATION_TAB,
};

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

export function buildAdminSearchParams({ section, tab, userId, preserve }) {
  const next = new URLSearchParams();

  if (preserve) {
    for (const [key, value] of preserve.entries()) {
      if (key !== "section" && key !== "tab" && key !== "userId") {
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

  return next;
}
