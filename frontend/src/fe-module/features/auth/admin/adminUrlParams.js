import { DEFAULT_COMMERCE_MODERATION_TAB } from "./commerceModeration/commerceModerationTabs.js";

const VALID_SECTIONS = ["rolePermission", "loginSession", "commerceModeration"];

const DEFAULT_TAB_BY_SECTION = {
  rolePermission: "role-list",
  loginSession: "login-history",
  commerceModeration: DEFAULT_COMMERCE_MODERATION_TAB,
};

export function parseAdminSection(searchParams) {
  const raw = searchParams.get("section");
  if (!raw || !VALID_SECTIONS.includes(raw)) {
    return "rolePermission";
  }
  return raw;
}

export function parseAdminTab(searchParams, section) {
  const raw = searchParams.get("tab");
  if (raw) return raw;
  return DEFAULT_TAB_BY_SECTION[section] || DEFAULT_TAB_BY_SECTION.rolePermission;
}

export function buildAdminSearchParams({ section, tab, preserve }) {
  const next = new URLSearchParams();

  if (preserve) {
    for (const [key, value] of preserve.entries()) {
      if (key !== "section" && key !== "tab") {
        next.set(key, value);
      }
    }
  }

  next.set("section", section);
  next.set("tab", tab);
  return next;
}
