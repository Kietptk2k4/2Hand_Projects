/** Shared eyebrow for every RBAC admin tab. */
export const RBAC_SECTION_EYEBROW = "Vai trò & quyền";

/** Tabs that show the user list panel and persist rbac_* user filter URL params. */
export const RBAC_USER_LIST_TABS = ["assign", "revoke", "user-permissions"];

/** Tab that persists rbac_role_id in the URL. */
export const RBAC_ROLE_SELECTION_TAB = "role-permissions";

export const RBAC_LIST_TAB = "role-list";

export function isRbacUserListTab(tab) {
  return RBAC_USER_LIST_TABS.includes(tab);
}

export function isRbacRoleSelectionTab(tab) {
  return tab === RBAC_ROLE_SELECTION_TAB;
}

export function resolveRbacUrlParams(
  tab,
  { rbacUserListFilters, rbacSelectedUserId, rbacSelectedRoleId },
) {
  return {
    rbacUserListFilters: isRbacUserListTab(tab) ? rbacUserListFilters : undefined,
    // null = explicit clear (do not fall back to preserved URL param)
    rbacUserId: isRbacUserListTab(tab)
      ? rbacSelectedUserId === null
        ? null
        : rbacSelectedUserId || undefined
      : undefined,
    rbacRoleId: isRbacRoleSelectionTab(tab) ? rbacSelectedRoleId || undefined : undefined,
  };
}

/**
 * Checklist — every RBAC tab view should satisfy:
 *
 * Layout
 * - [ ] Uses AdminListPageShell, AdminWorkspacePageShell, or AdminDetailPageShell
 * - [ ] eyebrow = RBAC_SECTION_EYEBROW
 * - [ ] Outer spacing: space-y-6 (via shell)
 *
 * Header
 * - [ ] title + subtitle in sentence case (Vietnamese)
 * - [ ] headerActions when refresh or primary CTA applies
 *
 * States
 * - [ ] loading → AdminListSkeleton inside shell
 * - [ ] forbidden → AdminForbiddenPanel (not error panel)
 * - [ ] error → AdminErrorPanel with onRetry when recoverable
 * - [ ] empty → AdminEmptyPanel with contextual copy
 *
 * Data surface
 * - [ ] Table uses AdminDataTable + mobile cards
 * - [ ] Codes/IDs use font-mono; timestamps use tabular-nums
 *
 * URL (rolePermission section)
 * - [ ] role-list → only section + tab
 * - [ ] role-permissions → rbac_role_id when role selected
 * - [ ] assign | revoke | user-permissions → rbac_user_id + list filters
 */
export const RBAC_PAGE_CHECKLIST = [
  "Shell: AdminListPageShell | AdminWorkspacePageShell | AdminDetailPageShell",
  "eyebrow: RBAC_SECTION_EYEBROW",
  "States: loading / forbidden / error / empty / ready",
  "Copy: Vietnamese, sentence case, no mixed EN labels",
  "URL: tab-scoped rbac_role_id and rbac_user_* params",
];
