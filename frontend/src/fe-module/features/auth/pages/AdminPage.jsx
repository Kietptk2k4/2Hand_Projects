import { useCallback, useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { AdminProductRemovalTab } from "../../commerce/components/AdminProductRemovalTab";
import { AdminReviewModerationTab } from "../../commerce/components/AdminReviewModerationTab";
import { AdminShopModerationTab } from "../../commerce/components/AdminShopModerationTab";
import {
  buildAdminSearchParams,
  parseAdminSection,
  parseAdminTab,
  parseInvestigationUserId,
} from "../admin/adminUrlParams.js";
import { AdminNestedNav } from "../admin/components/AdminNestedNav.jsx";
import { AdminPageLayout } from "../admin/components/AdminPageLayout.jsx";
import { AdminShell } from "../admin/components/AdminShell.jsx";
import { AssignRoleTab } from "../admin/rolePermission/components/AssignRoleTab.jsx";
import { RevokeRoleTab } from "../admin/rolePermission/components/RevokeRoleTab.jsx";
import { PermissionsOfRoleTab } from "../admin/rolePermission/components/PermissionsOfRoleTab.jsx";
import { PermissionsOfUserTab } from "../admin/rolePermission/components/PermissionsOfUserTab.jsx";
import { RoleListTab } from "../admin/rolePermission/components/RoleListTab.jsx";
import { AdminUserTargetBar } from "../admin/userInvestigation/components/AdminUserTargetBar.jsx";
import { InvestigationCurrentEnforcementTab } from "../admin/userInvestigation/components/tabs/InvestigationCurrentEnforcementTab.jsx";
import { InvestigationEnforcementHistoryTab } from "../admin/userInvestigation/components/tabs/InvestigationEnforcementHistoryTab.jsx";
import { InvestigationLoginHistoryTab } from "../admin/userInvestigation/components/tabs/InvestigationLoginHistoryTab.jsx";
import { InvestigationProfileTab } from "../admin/userInvestigation/components/tabs/InvestigationProfileTab.jsx";
import { InvestigationUserSessionsTab } from "../admin/userInvestigation/components/tabs/InvestigationUserSessionsTab.jsx";
import { AuthAlert } from "../../../shared/ui/auth/authUi.jsx";

const ROLE_PERMISSION_TAB_COMPONENTS = {
  "role-list": RoleListTab,
  assign: AssignRoleTab,
  revoke: RevokeRoleTab,
  "role-permissions": PermissionsOfRoleTab,
  "user-permissions": PermissionsOfUserTab,
};

const USER_INVESTIGATION_TAB_COMPONENTS = {
  profile: InvestigationProfileTab,
  "login-history": InvestigationLoginHistoryTab,
  "user-sessions": InvestigationUserSessionsTab,
  "current-enforcement": InvestigationCurrentEnforcementTab,
  "enforcement-history": InvestigationEnforcementHistoryTab,
};

const COMMERCE_MODERATION_TAB_COMPONENTS = {
  "shop-moderation": AdminShopModerationTab,
  "review-moderation": AdminReviewModerationTab,
  "product-moderation": AdminProductRemovalTab,
};

export function AdminPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const adminTopTab = parseAdminSection(searchParams);
  const activeChildTab = parseAdminTab(searchParams, adminTopTab);
  const investigationUserId = parseInvestigationUserId(searchParams);

  const [selectedRoleId, setSelectedRoleId] = useState("");
  const [investigationTargetUser, setInvestigationTargetUser] = useState(null);
  const [alert, setAlert] = useState(null);

  useEffect(() => {
    const rawSection = searchParams.get("section");
    if (rawSection === "loginSession") {
      setSearchParams(
        buildAdminSearchParams({
          section: "userInvestigation",
          tab: searchParams.get("tab") || "login-history",
          userId: investigationUserId,
          preserve: searchParams,
        }),
        { replace: true },
      );
      return;
    }

    if (!rawSection) {
      setSearchParams(
        buildAdminSearchParams({ section: "rolePermission", tab: "role-list" }),
        { replace: true },
      );
    }
  }, [investigationUserId, searchParams, setSearchParams]);

  const onNotify = useCallback((nextAlert) => {
    setAlert(nextAlert);
  }, []);

  const handleSectionChange = useCallback(
    (sectionId) => {
      const defaultTab = parseAdminTab(new URLSearchParams(), sectionId);
      setSearchParams(
        buildAdminSearchParams({
          section: sectionId,
          tab: defaultTab,
          userId: sectionId === "userInvestigation" ? investigationUserId : undefined,
        }),
        { replace: true },
      );
      setAlert(null);
    },
    [investigationUserId, setSearchParams],
  );

  const handleChildTabChange = useCallback(
    (childId) => {
      setSearchParams(
        buildAdminSearchParams({
          section: adminTopTab,
          tab: childId,
          userId: adminTopTab === "userInvestigation" ? investigationUserId : undefined,
          preserve: searchParams,
        }),
        { replace: true },
      );
      setAlert(null);
    },
    [adminTopTab, investigationUserId, searchParams, setSearchParams],
  );

  const handleInvestigationTargetChange = useCallback(
    ({ userId, user }) => {
      setInvestigationTargetUser(user ?? null);
      setSearchParams(
        buildAdminSearchParams({
          section: "userInvestigation",
          tab: activeChildTab,
          userId: userId || undefined,
          preserve: searchParams,
        }),
        { replace: true },
      );
      setAlert(null);
    },
    [activeChildTab, searchParams, setSearchParams],
  );

  const onViewRolePermissions = useCallback(
    (roleId) => {
      setSelectedRoleId(roleId);
      setSearchParams(
        buildAdminSearchParams({
          section: "rolePermission",
          tab: "role-permissions",
        }),
        { replace: true },
      );
      setAlert(null);
    },
    [setSearchParams],
  );

  const RoleTabComponent = ROLE_PERMISSION_TAB_COMPONENTS[activeChildTab] || RoleListTab;
  const InvestigationTabComponent =
    USER_INVESTIGATION_TAB_COMPONENTS[activeChildTab] || InvestigationProfileTab;
  const CommerceTabComponent =
    COMMERCE_MODERATION_TAB_COMPONENTS[activeChildTab] || AdminShopModerationTab;

  const roleTabProps = {
    onNotify,
    onTabChange: handleChildTabChange,
    selectedRoleId,
    onSelectedRoleIdChange: setSelectedRoleId,
    onViewRolePermissions,
  };

  const investigationTabProps = {
    userId: investigationUserId,
    targetUser: investigationTargetUser,
    onNotify,
  };

  const mainContent = useMemo(() => {
    if (adminTopTab === "rolePermission") {
      return <RoleTabComponent {...roleTabProps} />;
    }
    if (adminTopTab === "userInvestigation") {
      return <InvestigationTabComponent {...investigationTabProps} />;
    }
    if (adminTopTab === "commerceModeration") {
      return <CommerceTabComponent />;
    }
    return null;
  }, [
    CommerceTabComponent,
    InvestigationTabComponent,
    RoleTabComponent,
    adminTopTab,
    investigationTabProps,
    roleTabProps,
  ]);

  return (
    <AdminShell>
      <AdminPageLayout
        nav={
          <AdminNestedNav
            activeSection={adminTopTab}
            activeChildTab={activeChildTab}
            onSectionChange={handleSectionChange}
            onChildTabChange={handleChildTabChange}
          />
        }
      >
        {alert ? (
          <AuthAlert
            variant={alert.variant}
            title={
              alert.variant === "success"
                ? "Thành công"
                : alert.variant === "error"
                  ? "Lỗi"
                  : undefined
            }
            message={alert.message}
            onDismiss={() => setAlert(null)}
          />
        ) : null}

        {adminTopTab === "userInvestigation" ? (
          <AdminUserTargetBar
            userId={investigationUserId}
            selectedUser={investigationTargetUser}
            onTargetChange={handleInvestigationTargetChange}
          />
        ) : null}

        {mainContent}
      </AdminPageLayout>
    </AdminShell>
  );
}
