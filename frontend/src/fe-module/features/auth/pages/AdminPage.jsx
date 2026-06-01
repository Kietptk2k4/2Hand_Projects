import { useCallback, useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { AdminReviewModerationTab } from "../../commerce/components/AdminReviewModerationTab";
import { AdminShopModerationTab } from "../../commerce/components/AdminShopModerationTab";
import {
  buildAdminSearchParams,
  parseAdminSection,
  parseAdminTab,
} from "../admin/adminUrlParams.js";
import { AdminNestedNav } from "../admin/components/AdminNestedNav.jsx";
import { AdminPageLayout } from "../admin/components/AdminPageLayout.jsx";
import { AdminCommerceModerationPlaceholderTab } from "../admin/commerceModeration/components/AdminCommerceModerationPlaceholderTab.jsx";
import { AdminUserTargetBar } from "../admin/loginSession/components/AdminUserTargetBar.jsx";
import { AssignRoleTab } from "../admin/rolePermission/components/AssignRoleTab.jsx";
import { RevokeRoleTab } from "../admin/rolePermission/components/RevokeRoleTab.jsx";
import { PermissionsOfRoleTab } from "../admin/rolePermission/components/PermissionsOfRoleTab.jsx";
import { PermissionsOfUserTab } from "../admin/rolePermission/components/PermissionsOfUserTab.jsx";
import { RoleListTab } from "../admin/rolePermission/components/RoleListTab.jsx";
import { AdminLoginHistoryTab } from "../admin/loginSession/components/AdminLoginHistoryTab.jsx";
import { AdminUserSessionsTab } from "../admin/loginSession/components/AdminUserSessionsTab.jsx";
import { AuthAlert } from "../../../shared/ui/auth/authUi.jsx";

const ROLE_PERMISSION_TAB_COMPONENTS = {
  "role-list": RoleListTab,
  assign: AssignRoleTab,
  revoke: RevokeRoleTab,
  "role-permissions": PermissionsOfRoleTab,
  "user-permissions": PermissionsOfUserTab,
};

const LOGIN_SESSION_TAB_COMPONENTS = {
  "login-history": AdminLoginHistoryTab,
  "user-sessions": AdminUserSessionsTab,
};

const COMMERCE_MODERATION_TAB_COMPONENTS = {
  "shop-moderation": AdminShopModerationTab,
  "review-moderation": AdminReviewModerationTab,
  "product-moderation": function AdminProductModerationPlaceholder() {
    return (
      <AdminCommerceModerationPlaceholderTab
        title="Kiểm duyệt sản phẩm"
        description="RemoveProductByAdmin — đang được phát triển."
      />
    );
  },
};

export function AdminPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const adminTopTab = parseAdminSection(searchParams);
  const activeChildTab = parseAdminTab(searchParams, adminTopTab);

  const [selectedInvestigationUserId, setSelectedInvestigationUserId] = useState("");
  const [selectedRoleId, setSelectedRoleId] = useState("");
  const [alert, setAlert] = useState(null);

  useEffect(() => {
    if (!searchParams.get("section")) {
      setSearchParams(
        buildAdminSearchParams({ section: "rolePermission", tab: "role-list" }),
        { replace: true },
      );
    }
  }, [searchParams, setSearchParams]);

  const onNotify = useCallback((nextAlert) => {
    setAlert(nextAlert);
  }, []);

  const handleSectionChange = useCallback(
    (sectionId) => {
      const defaultTab = parseAdminTab(new URLSearchParams(), sectionId);
      setSearchParams(buildAdminSearchParams({ section: sectionId, tab: defaultTab }), {
        replace: true,
      });
      setAlert(null);
    },
    [setSearchParams],
  );

  const handleChildTabChange = useCallback(
    (childId) => {
      setSearchParams(
        buildAdminSearchParams({
          section: adminTopTab,
          tab: childId,
          preserve: searchParams,
        }),
        { replace: true },
      );
      setAlert(null);
    },
    [adminTopTab, searchParams, setSearchParams],
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
  const LoginSessionTabComponent =
    LOGIN_SESSION_TAB_COMPONENTS[activeChildTab] || AdminLoginHistoryTab;
  const CommerceTabComponent =
    COMMERCE_MODERATION_TAB_COMPONENTS[activeChildTab] || AdminShopModerationTab;

  const roleTabProps = {
    onNotify,
    onTabChange: handleChildTabChange,
    selectedRoleId,
    onSelectedRoleIdChange: setSelectedRoleId,
    onViewRolePermissions,
  };

  const loginSessionTabProps = {
    userId: selectedInvestigationUserId,
    onNotify,
  };

  const mainContent = useMemo(() => {
    if (adminTopTab === "rolePermission") {
      return <RoleTabComponent {...roleTabProps} />;
    }
    if (adminTopTab === "loginSession") {
      return <LoginSessionTabComponent {...loginSessionTabProps} />;
    }
    if (adminTopTab === "commerceModeration") {
      return <CommerceTabComponent />;
    }
    return null;
  }, [
    CommerceTabComponent,
    LoginSessionTabComponent,
    RoleTabComponent,
    adminTopTab,
    loginSessionTabProps,
    roleTabProps,
  ]);

  return (
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

      {adminTopTab === "loginSession" ? (
        <AdminUserTargetBar
          userId={selectedInvestigationUserId}
          onUserIdChange={setSelectedInvestigationUserId}
        />
      ) : null}

      {mainContent}
    </AdminPageLayout>
  );
}
