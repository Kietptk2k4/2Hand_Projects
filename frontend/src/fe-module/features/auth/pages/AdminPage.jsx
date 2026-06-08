import { useCallback, useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { AdminProductRemovalTab } from "../../commerce/components/AdminProductRemovalTab";
import { AdminReviewModerationTab } from "../../commerce/components/AdminReviewModerationTab";
import { AdminShopModerationTab } from "../../commerce/components/AdminShopModerationTab";
import {
  buildAdminSearchParams,
  parseAdminAuditFilters,
  parseAdminAuditLogId,
  parseAdminSection,
  parseAdminTab,
  parseContentModerationCommentId,
  parseContentModerationPostId,
  parseContentModerationProductId,
  parseContentModerationProductView,
  parseInvestigationUserId,
  parseOrderSupportOrderId,
  parseOrderSupportPaymentId,
  parseOrderSupportShipmentId,
  parseOrderSupportWebhookFilters,
  parseSystemOperationsAnnouncementFilters,
  parseSystemOperationsConfigFilters,
  parseSystemOperationsConfigId,
  parseSystemOperationsConfigView,
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
import { AdminActionLogsTab } from "../admin/adminAudit/components/tabs/AdminActionLogsTab.jsx";
import { PostModerationTab } from "../admin/contentModeration/components/tabs/PostModerationTab.jsx";
import { CommentModerationTab } from "../admin/contentModeration/components/tabs/CommentModerationTab.jsx";
import { ContentModerationQaReference } from "../admin/contentModeration/components/ContentModerationQaReference.jsx";
import { ContentModerationTargetBar } from "../admin/contentModeration/components/ContentModerationTargetBar.jsx";
import { OrderSupportDetailTab } from "../admin/orderSupport/components/tabs/OrderSupportDetailTab.jsx";
import { PaymentSupportDetailTab } from "../admin/orderSupport/components/tabs/PaymentSupportDetailTab.jsx";
import { ShipmentSupportDetailTab } from "../admin/orderSupport/components/tabs/ShipmentSupportDetailTab.jsx";
import { AdminSupportTargetBar } from "../admin/orderSupport/components/AdminSupportTargetBar.jsx";
import { WebhookLogsSupportTab } from "../admin/orderSupport/components/tabs/WebhookLogsSupportTab.jsx";
import { SystemConfigsTab } from "../admin/systemOperations/components/tabs/SystemConfigsTab.jsx";
import { SystemAnnouncementsTab } from "../admin/systemOperations/components/tabs/SystemAnnouncementsTab.jsx";
import { AuthAlert } from "../../../shared/ui/auth/authUi.jsx";

const CONTENT_MODERATION_TAB_COMPONENTS = {
  "post-moderation": PostModerationTab,
  "comment-moderation": CommentModerationTab,
  "shop-moderation": AdminShopModerationTab,
  "review-moderation": AdminReviewModerationTab,
  "product-moderation": AdminProductRemovalTab,
};

const ADMIN_AUDIT_TAB_COMPONENTS = {
  "action-logs": AdminActionLogsTab,
};

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

const SYSTEM_OPERATIONS_TAB_COMPONENTS = {
  "system-configs": SystemConfigsTab,
  "system-announcements": SystemAnnouncementsTab,
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
  const adminAuditFilters = parseAdminAuditFilters(searchParams);
  const adminAuditLogId = parseAdminAuditLogId(searchParams);
  const contentModerationPostId = parseContentModerationPostId(searchParams);
  const contentModerationCommentId = parseContentModerationCommentId(searchParams);
  const contentModerationProductId = parseContentModerationProductId(searchParams);
  const contentModerationProductView = parseContentModerationProductView(searchParams);
  const systemOperationsConfigFilters = parseSystemOperationsConfigFilters(searchParams);
  const systemOperationsAnnouncementFilters = parseSystemOperationsAnnouncementFilters(searchParams);
  const systemOperationsConfigId = parseSystemOperationsConfigId(searchParams);
  const systemOperationsConfigView = parseSystemOperationsConfigView(searchParams);

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

    if (rawSection === "commerceModeration") {
      setSearchParams(
        buildAdminSearchParams({
          section: "contentModeration",
          tab: searchParams.get("tab") || "shop-moderation",
          postId: contentModerationPostId || undefined,
          commentId: contentModerationCommentId || undefined,
          productId: contentModerationProductId || undefined,
          productView: contentModerationProductView,
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
  }, [
    contentModerationCommentId,
    contentModerationPostId,
    contentModerationProductId,
    contentModerationProductView,
    investigationUserId,
    searchParams,
    setSearchParams,
  ]);

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
          auditFilters: sectionId === "adminAudit" ? adminAuditFilters : undefined,
          logId: sectionId === "adminAudit" ? adminAuditLogId || undefined : undefined,
          postId: sectionId === "contentModeration" ? contentModerationPostId || undefined : undefined,
          commentId:
            sectionId === "contentModeration" ? contentModerationCommentId || undefined : undefined,
          productId:
            sectionId === "contentModeration" ? contentModerationProductId || undefined : undefined,
          productView:
            sectionId === "contentModeration" ? contentModerationProductView : undefined,
        }),
        { replace: true },
      );
      setAlert(null);
    },
    [
      adminAuditFilters,
      adminAuditLogId,
      contentModerationCommentId,
      contentModerationPostId,
      contentModerationProductId,
      contentModerationProductView,
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
          auditFilters: adminTopTab === "adminAudit" ? adminAuditFilters : undefined,
          logId: adminTopTab === "adminAudit" ? adminAuditLogId || undefined : undefined,
          postId:
            adminTopTab === "contentModeration" ? contentModerationPostId || undefined : undefined,
          commentId:
            adminTopTab === "contentModeration"
              ? contentModerationCommentId || undefined
              : undefined,
          productId:
            adminTopTab === "contentModeration"
              ? contentModerationProductId || undefined
              : undefined,
          productView:
            adminTopTab === "contentModeration" ? contentModerationProductView : undefined,
          preserve: searchParams,
        }),
        { replace: true },
      );
      setAlert(null);
    },
    [
      adminTopTab,
      adminAuditFilters,
      adminAuditLogId,
      contentModerationCommentId,
      contentModerationPostId,
      contentModerationProductId,
      contentModerationProductView,
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

  const handleAuditFiltersChange = useCallback(
    (filters) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "adminAudit",
          tab: activeChildTab,
          auditFilters: filters,
          clearLogId: true,
          preserve: searchParams,
        }),
        { replace: true },
      );
      setAlert(null);
    },
    [activeChildTab, searchParams, setSearchParams],
  );

  const handleAuditLogIdChange = useCallback(
    (nextLogId) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "adminAudit",
          tab: activeChildTab,
          auditFilters: adminAuditFilters,
          logId: nextLogId || undefined,
          clearLogId: !nextLogId,
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [activeChildTab, adminAuditFilters, searchParams, setSearchParams],
  );

  const handleContentModerationProductViewChange = useCallback(
    ({ productId: nextProductId, productView: nextProductView }) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "contentModeration",
          tab: activeChildTab,
          postId: contentModerationPostId || undefined,
          commentId: contentModerationCommentId || undefined,
          productId: nextProductId || undefined,
          productView: nextProductView || "list",
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [
      activeChildTab,
      contentModerationCommentId,
      contentModerationPostId,
      searchParams,
      setSearchParams,
    ],
  );

  const handleContentModerationTargetChange = useCallback(
    (patch) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "contentModeration",
          tab: activeChildTab,
          postId: "postId" in patch ? patch.postId : contentModerationPostId,
          commentId: "commentId" in patch ? patch.commentId : contentModerationCommentId,
          productId: contentModerationProductId || undefined,
          productView: contentModerationProductView,
          preserve: searchParams,
        }),
        { replace: true },
      );
      setAlert(null);
    },
    [
      activeChildTab,
      contentModerationCommentId,
      contentModerationPostId,
      contentModerationProductId,
      contentModerationProductView,
      searchParams,
      setSearchParams,
    ],
  );


  const handleSystemOperationsConfigFiltersChange = useCallback(
    (filters) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "systemOperations",
          tab: activeChildTab,
          configFilters: filters,
          clearConfigSelection: true,
          preserve: searchParams,
        }),
        { replace: true },
      );
      setAlert(null);
    },
    [activeChildTab, searchParams, setSearchParams],
  );

  const handleSystemOperationsAnnouncementFiltersChange = useCallback(
    (filters) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "systemOperations",
          tab: activeChildTab,
          announcementFilters: filters,
          preserve: searchParams,
        }),
        { replace: true },
      );
      setAlert(null);
    },
    [activeChildTab, searchParams, setSearchParams],
  );

  const handleSystemOperationsConfigSelectionChange = useCallback(
    ({ configId: nextConfigId, configView: nextConfigView }) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "systemOperations",
          tab: activeChildTab,
          configFilters: systemOperationsConfigFilters,
          announcementFilters: systemOperationsAnnouncementFilters,
          configId: nextConfigId || undefined,
          configView: nextConfigView || undefined,
          clearConfigSelection: !nextConfigId,
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [
      activeChildTab,
      searchParams,
      setSearchParams,
      systemOperationsAnnouncementFilters,
      systemOperationsConfigFilters,
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
  const AdminAuditTabComponent =
    ADMIN_AUDIT_TAB_COMPONENTS[activeChildTab] || AdminActionLogsTab;
  const ContentModerationTabComponent =
    CONTENT_MODERATION_TAB_COMPONENTS[activeChildTab] || PostModerationTab;
  const SystemOperationsTabComponent =
    SYSTEM_OPERATIONS_TAB_COMPONENTS[activeChildTab] || SystemConfigsTab;
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

  const adminAuditTabProps = {
    logId: adminAuditLogId,
    auditFilters: adminAuditFilters,
    onFiltersChange: handleAuditFiltersChange,
    onLogIdChange: handleAuditLogIdChange,
    onNotify,
  };


  const systemOperationsTabProps = {
    configId: systemOperationsConfigId,
    configView: systemOperationsConfigView,
    configFilters: systemOperationsConfigFilters,
    announcementFilters: systemOperationsAnnouncementFilters,
    onFiltersChange: handleSystemOperationsConfigFiltersChange,
    onAnnouncementFiltersChange: handleSystemOperationsAnnouncementFiltersChange,
    onConfigSelectionChange: handleSystemOperationsConfigSelectionChange,
    onNotify,
  };

  const contentModerationTabProps = {
    postId: contentModerationPostId,
    commentId: contentModerationCommentId,
    productId: contentModerationProductId,
    productView: contentModerationProductView,
    onProductViewChange: handleContentModerationProductViewChange,
  };

  const mainContent = useMemo(() => {
    if (adminTopTab === "rolePermission") {
      return <RoleTabComponent {...roleTabProps} />;
    }
    if (adminTopTab === "userInvestigation") {
      return <InvestigationTabComponent {...investigationTabProps} />;
    }
    if (adminTopTab === "adminAudit") {
      return <AdminAuditTabComponent {...adminAuditTabProps} />;
    }
    if (adminTopTab === "contentModeration") {
      const TabComponent = ContentModerationTabComponent;
      if (TabComponent === PostModerationTab || TabComponent === CommentModerationTab) {
        return <TabComponent {...contentModerationTabProps} />;
      }
      if (TabComponent === AdminProductRemovalTab) {
        return <TabComponent {...contentModerationTabProps} />;
      }
      return <TabComponent />;
    }
    if (adminTopTab === "systemOperations") {
      return <SystemOperationsTabComponent {...systemOperationsTabProps} />;
    }
    if (adminTopTab === "orderSupport") {
      return <OrderSupportTabComponent {...orderSupportTabProps} />;
    }
    return null;
  }, [
    AdminAuditTabComponent,
    ContentModerationTabComponent,
    InvestigationTabComponent,
    OrderSupportTabComponent,
    SystemOperationsTabComponent,
    RoleTabComponent,
    adminTopTab,
    adminAuditTabProps,
    contentModerationTabProps,
    investigationTabProps,
    orderSupportTabProps,
    systemOperationsTabProps,
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

        {adminTopTab === "contentModeration" &&
        (activeChildTab === "post-moderation" || activeChildTab === "comment-moderation") ? (
          <ContentModerationTargetBar
            activeTab={activeChildTab}
            targetIds={{
              postId: contentModerationPostId,
              commentId: contentModerationCommentId,
            }}
            onTargetChange={handleContentModerationTargetChange}
          />
        ) : null}

        {adminTopTab === "adminAudit" || adminTopTab === "contentModeration" ? (
          <ContentModerationQaReference activeTab={activeChildTab} />
        ) : null}

        {mainContent}
      </AdminPageLayout>
    </AdminShell>
  );
}
