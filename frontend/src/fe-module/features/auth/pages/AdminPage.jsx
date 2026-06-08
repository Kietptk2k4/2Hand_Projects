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
  parseOrderSupportOrderId,
  parseOrderSupportPaymentId,
  parseOrderSupportShipmentId,
  parseOrderSupportWebhookFilters,
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
import { AdminSupportTargetBar } from "../admin/orderSupport/components/AdminSupportTargetBar.jsx";
import { OrderSupportDetailTab } from "../admin/orderSupport/components/tabs/OrderSupportDetailTab.jsx";
import { PaymentSupportDetailTab } from "../admin/orderSupport/components/tabs/PaymentSupportDetailTab.jsx";
import { ShipmentSupportDetailTab } from "../admin/orderSupport/components/tabs/ShipmentSupportDetailTab.jsx";
import { WebhookLogsSupportTab } from "../admin/orderSupport/components/tabs/WebhookLogsSupportTab.jsx";
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

const ORDER_SUPPORT_TAB_COMPONENTS = {
  "order-detail": OrderSupportDetailTab,
  "payment-detail": PaymentSupportDetailTab,
  "shipment-detail": ShipmentSupportDetailTab,
  "webhook-logs": WebhookLogsSupportTab,
};

export function AdminPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const adminTopTab = parseAdminSection(searchParams);
  const activeChildTab = parseAdminTab(searchParams, adminTopTab);
  const investigationUserId = parseInvestigationUserId(searchParams);
  const orderSupportOrderId = parseOrderSupportOrderId(searchParams);
  const orderSupportPaymentId = parseOrderSupportPaymentId(searchParams);
  const orderSupportShipmentId = parseOrderSupportShipmentId(searchParams);
  const orderSupportWebhookFilters = parseOrderSupportWebhookFilters(searchParams);

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
          orderId: sectionId === "orderSupport" ? orderSupportOrderId : undefined,
          paymentId: sectionId === "orderSupport" ? orderSupportPaymentId : undefined,
          shipmentId: sectionId === "orderSupport" ? orderSupportShipmentId : undefined,
          webhookFilters:
            sectionId === "orderSupport" && defaultTab === "webhook-logs"
              ? orderSupportWebhookFilters
              : undefined,
        }),
        { replace: true },
      );
      setAlert(null);
    },
    [
      investigationUserId,
      orderSupportOrderId,
      orderSupportPaymentId,
      orderSupportShipmentId,
      orderSupportWebhookFilters,
      setSearchParams,
    ],
  );

  const handleChildTabChange = useCallback(
    (childId) => {
      setSearchParams(
        buildAdminSearchParams({
          section: adminTopTab,
          tab: childId,
          userId: adminTopTab === "userInvestigation" ? investigationUserId : undefined,
          orderId: adminTopTab === "orderSupport" ? orderSupportOrderId : undefined,
          paymentId: adminTopTab === "orderSupport" ? orderSupportPaymentId : undefined,
          shipmentId: adminTopTab === "orderSupport" ? orderSupportShipmentId : undefined,
          webhookFilters:
            adminTopTab === "orderSupport" && childId === "webhook-logs"
              ? orderSupportWebhookFilters
              : undefined,
          preserve: searchParams,
        }),
        { replace: true },
      );
      setAlert(null);
    },
    [
      adminTopTab,
      investigationUserId,
      orderSupportOrderId,
      orderSupportPaymentId,
      orderSupportShipmentId,
      orderSupportWebhookFilters,
      searchParams,
      setSearchParams,
    ],
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

  const handleSupportNavigate = useCallback(
    (nextParams) => {
      setSearchParams(nextParams, { replace: true });
      setAlert(null);
    },
    [setSearchParams],
  );

  const handleSupportTargetChange = useCallback(
    (patch) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "orderSupport",
          tab: activeChildTab,
          orderId: "orderId" in patch ? patch.orderId : orderSupportOrderId,
          paymentId: "paymentId" in patch ? patch.paymentId : orderSupportPaymentId,
          shipmentId: "shipmentId" in patch ? patch.shipmentId : orderSupportShipmentId,
          webhookFilters:
            activeChildTab === "webhook-logs" ? orderSupportWebhookFilters : undefined,
          preserve: searchParams,
        }),
        { replace: true },
      );
      setAlert(null);
    },
    [
      activeChildTab,
      orderSupportOrderId,
      orderSupportPaymentId,
      orderSupportShipmentId,
      orderSupportWebhookFilters,
      searchParams,
      setSearchParams,
    ],
  );

  const handleWebhookFiltersChange = useCallback(
    (filters) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "orderSupport",
          tab: "webhook-logs",
          orderId: orderSupportOrderId,
          paymentId: orderSupportPaymentId,
          shipmentId: orderSupportShipmentId,
          webhookFilters: filters,
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [
      orderSupportOrderId,
      orderSupportPaymentId,
      orderSupportShipmentId,
      searchParams,
      setSearchParams,
    ],
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
  const OrderSupportTabComponent =
    ORDER_SUPPORT_TAB_COMPONENTS[activeChildTab] || OrderSupportDetailTab;

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

  const orderSupportTabProps = {
    orderId: orderSupportOrderId,
    paymentId: orderSupportPaymentId,
    shipmentId: orderSupportShipmentId,
    webhookFilters: orderSupportWebhookFilters,
    onNavigate: handleSupportNavigate,
    onFiltersChange: handleWebhookFiltersChange,
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
    if (adminTopTab === "orderSupport") {
      return <OrderSupportTabComponent {...orderSupportTabProps} />;
    }
    return null;
  }, [
    CommerceTabComponent,
    InvestigationTabComponent,
    OrderSupportTabComponent,
    RoleTabComponent,
    adminTopTab,
    investigationTabProps,
    orderSupportTabProps,
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

        {adminTopTab === "orderSupport" ? (
          <AdminSupportTargetBar
            activeTab={activeChildTab}
            targetIds={{
              orderId: orderSupportOrderId,
              paymentId: orderSupportPaymentId,
              shipmentId: orderSupportShipmentId,
            }}
            onTargetChange={handleSupportTargetChange}
          />
        ) : null}

        {mainContent}
      </AdminPageLayout>
    </AdminShell>
  );
}
