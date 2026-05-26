import { useCallback, useState } from "react";
import { RolePermissionLayout } from "../admin/rolePermission/components/RolePermissionLayout.jsx";
import { AssignRoleTab } from "../admin/rolePermission/components/AssignRoleTab.jsx";
import { PermissionsOfRoleTab } from "../admin/rolePermission/components/PermissionsOfRoleTab.jsx";
import { PermissionsOfUserTab } from "../admin/rolePermission/components/PermissionsOfUserTab.jsx";
import { RoleListTab } from "../admin/rolePermission/components/RoleListTab.jsx";
import { ADMIN_TOP_TABS } from "../admin/adminTabs.js";
import { AuthAlert } from "../../../shared/ui/auth/authUi.jsx";

const TAB_COMPONENTS = {
  "role-list": RoleListTab,
  assign: AssignRoleTab,
  "role-permissions": PermissionsOfRoleTab,
  "user-permissions": PermissionsOfUserTab,
};

export function AdminPage() {
  const [adminTopTab] = useState("rolePermission");
  const [rolePermissionTab, setRolePermissionTab] = useState("role-list");
  const [selectedRoleId, setSelectedRoleId] = useState("");
  const [alert, setAlert] = useState(null);

  const onNotify = useCallback((nextAlert) => {
    setAlert(nextAlert);
  }, []);

  const onRolePermissionTabChange = useCallback((tabId) => {
    setRolePermissionTab(tabId);
    setAlert(null);
  }, []);

  const onViewRolePermissions = useCallback((roleId) => {
    setSelectedRoleId(roleId);
    setRolePermissionTab("role-permissions");
    setAlert(null);
  }, []);

  const TabComponent = TAB_COMPONENTS[rolePermissionTab] || RoleListTab;

  const tabProps = {
    onNotify,
    onTabChange: onRolePermissionTabChange,
    selectedRoleId,
    onSelectedRoleIdChange: setSelectedRoleId,
    onViewRolePermissions,
  };

  return (
    <div className="space-y-6">
      <header>
        <h1 className="text-2xl font-semibold text-on-surface md:text-3xl">Quan tri</h1>
        <p className="mt-2 text-base text-on-surface-variant">Administration</p>
      </header>

      <div className="flex gap-2 border-b border-outline-variant">
        {ADMIN_TOP_TABS.map((tab) => (
          <button
            key={tab.id}
            type="button"
            className={[
              "border-b-2 px-4 py-2 text-sm font-medium transition-colors",
              adminTopTab === tab.id
                ? "border-primary text-primary"
                : "border-transparent text-on-surface-variant hover:text-on-surface",
            ].join(" ")}
          >
            {tab.labelVn}
          </button>
        ))}
      </div>

      {alert ? (
        <AuthAlert
          variant={alert.variant}
          title={alert.variant === "success" ? "Thanh cong" : alert.variant === "error" ? "Loi" : undefined}
          message={alert.message}
          onDismiss={() => setAlert(null)}
        />
      ) : null}

      {adminTopTab === "rolePermission" ? (
        <RolePermissionLayout activeTab={rolePermissionTab} onTabChange={onRolePermissionTabChange}>
          <TabComponent {...tabProps} />
        </RolePermissionLayout>
      ) : null}
    </div>
  );
}
