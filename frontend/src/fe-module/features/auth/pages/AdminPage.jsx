import { useCallback, useState } from "react";
import { AdminNestedNav } from "../admin/components/AdminNestedNav.jsx";
import { AdminPageLayout } from "../admin/components/AdminPageLayout.jsx";
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

export function AdminPage() {
  const [adminTopTab, setAdminTopTab] = useState("rolePermission");
  const [rolePermissionTab, setRolePermissionTab] = useState("role-list");
  const [loginSessionTab, setLoginSessionTab] = useState("login-history");
  const [selectedInvestigationUserId, setSelectedInvestigationUserId] = useState("");
  const [selectedRoleId, setSelectedRoleId] = useState("");
  const [alert, setAlert] = useState(null);

  const activeChildTab = adminTopTab === "rolePermission" ? rolePermissionTab : loginSessionTab;

  const onNotify = useCallback((nextAlert) => {
    setAlert(nextAlert);
  }, []);

  const handleSectionChange = useCallback((sectionId) => {
    setAdminTopTab(sectionId);
    setAlert(null);
  }, []);

  const handleChildTabChange = useCallback(
    (childId) => {
      if (adminTopTab === "rolePermission") {
        setRolePermissionTab(childId);
      } else {
        setLoginSessionTab(childId);
      }
      setAlert(null);
    },
    [adminTopTab]
  );

  const onViewRolePermissions = useCallback((roleId) => {
    setAdminTopTab("rolePermission");
    setSelectedRoleId(roleId);
    setRolePermissionTab("role-permissions");
    setAlert(null);
  }, []);

  const RoleTabComponent = ROLE_PERMISSION_TAB_COMPONENTS[rolePermissionTab] || RoleListTab;
  const LoginSessionTabComponent =
    LOGIN_SESSION_TAB_COMPONENTS[loginSessionTab] || AdminLoginHistoryTab;

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
          title={alert.variant === "success" ? "Thanh cong" : alert.variant === "error" ? "Loi" : undefined}
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

      {adminTopTab === "rolePermission" ? <RoleTabComponent {...roleTabProps} /> : null}
      {adminTopTab === "loginSession" ? <LoginSessionTabComponent {...loginSessionTabProps} /> : null}
    </AdminPageLayout>
  );
}
