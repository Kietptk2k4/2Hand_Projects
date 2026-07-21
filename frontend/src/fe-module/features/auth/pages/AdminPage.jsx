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
  parseCommerceFinanceSellerId,
  parseCommentModerationListFilters,
  parseInvestigationUserId,
  parseInvestigationUserListFilters,
  parsePostModerationListFilters,
  parseOrderSupportOrderId,
  parseOrderSupportOrderListFilters,
  parseOrderSupportPaymentId,
  parseOrderSupportPaymentFilters,
  parseOrderSupportShipmentId,
  parseOrderSupportShipmentListFilters,
  parseOrderSupportWebhookFilters,
  parseRbacSelectedUserId,
  parseRbacSelectedRoleId,
  parseRbacUserListFilters,
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
import { resolveRbacUrlParams } from "../admin/rolePermission/rbacPageContract.js";
import { AdminUserTargetBar } from "../admin/userInvestigation/components/AdminUserTargetBar.jsx";
import { InvestigationUserListPanel } from "../admin/userInvestigation/components/InvestigationUserListPanel.jsx";
import { InvestigationCurrentEnforcementTab } from "../admin/userInvestigation/components/tabs/InvestigationCurrentEnforcementTab.jsx";
import { InvestigationEnforcementHistoryTab } from "../admin/userInvestigation/components/tabs/InvestigationEnforcementHistoryTab.jsx";
import { InvestigationLoginHistoryTab } from "../admin/userInvestigation/components/tabs/InvestigationLoginHistoryTab.jsx";
import { InvestigationProfileTab } from "../admin/userInvestigation/components/tabs/InvestigationProfileTab.jsx";
import { InvestigationUserSessionsTab } from "../admin/userInvestigation/components/tabs/InvestigationUserSessionsTab.jsx";
import { AdminActionLogsTab } from "../admin/adminAudit/components/tabs/AdminActionLogsTab.jsx";
import { PostModerationTab } from "../admin/contentModeration/components/tabs/PostModerationTab.jsx";
import { CommentModerationTab } from "../admin/contentModeration/components/tabs/CommentModerationTab.jsx";
import { ContentModerationQaReference } from "../admin/contentModeration/components/ContentModerationQaReference.jsx";
import { CommentModerationListPanel } from "../admin/contentModeration/components/CommentModerationListPanel.jsx";
import { ContentModerationTargetBar } from "../admin/contentModeration/components/ContentModerationTargetBar.jsx";
import { PostModerationListPanel } from "../admin/contentModeration/components/PostModerationListPanel.jsx";
import { OrderSupportDetailTab } from "../admin/orderSupport/components/tabs/OrderSupportDetailTab.jsx";
import { PaymentSupportDetailTab } from "../admin/orderSupport/components/tabs/PaymentSupportDetailTab.jsx";
import { ShipmentSupportDetailTab } from "../admin/orderSupport/components/tabs/ShipmentSupportDetailTab.jsx";
import { AdminSupportTargetBar } from "../admin/orderSupport/components/AdminSupportTargetBar.jsx";
import { WebhookLogsSupportTab } from "../admin/orderSupport/components/tabs/WebhookLogsSupportTab.jsx";
import { AdminRefundApprovalsTab } from "../admin/orderSupport/components/tabs/AdminRefundApprovalsTab.jsx";
import { AdminFinanceOverviewTab } from "../admin/commerceFinance/components/tabs/AdminFinanceOverviewTab.jsx";
import { AdminFinanceCodPipelineTab } from "../admin/commerceFinance/components/tabs/AdminFinanceCodPipelineTab.jsx";
import { AdminFinanceTopSellersTab } from "../admin/commerceFinance/components/tabs/AdminFinanceTopSellersTab.jsx";
import { AdminFinanceSellerDetailTab } from "../admin/commerceFinance/components/tabs/AdminFinanceSellerDetailTab.jsx";
import { AdminFinancePayoutQueueTab } from "../admin/commerceFinance/components/tabs/AdminFinancePayoutQueueTab.jsx";
import { CategoryManagementTab } from "../admin/catalogManagement/components/tabs/CategoryManagementTab.jsx";
import { BrandManagementTab } from "../admin/catalogManagement/components/tabs/BrandManagementTab.jsx";
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
  "refund-approvals": AdminRefundApprovalsTab,
  "webhook-logs": WebhookLogsSupportTab,
};

const COMMERCE_FINANCE_TAB_COMPONENTS = {
  "finance-overview": AdminFinanceOverviewTab,
  "cod-pipeline": AdminFinanceCodPipelineTab,
  "top-sellers": AdminFinanceTopSellersTab,
  "seller-detail": AdminFinanceSellerDetailTab,
  "payout-queue": AdminFinancePayoutQueueTab,
};

const CATALOG_MANAGEMENT_TAB_COMPONENTS = {
  categories: CategoryManagementTab,
  brands: BrandManagementTab,
};

export function AdminPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const adminTopTab = parseAdminSection(searchParams);
  const activeChildTab = parseAdminTab(searchParams, adminTopTab);
  const investigationUserId = parseInvestigationUserId(searchParams);
  const investigationUserListFilters = useMemo(
    () => parseInvestigationUserListFilters(searchParams),
    [searchParams],
  );
  const postModerationListFilters = useMemo(
    () => parsePostModerationListFilters(searchParams),
    [searchParams],
  );
  const commentModerationListFilters = useMemo(
    () => parseCommentModerationListFilters(searchParams),
    [searchParams],
  );
  const orderSupportOrderId = parseOrderSupportOrderId(searchParams);
  const orderSupportOrderListFilters = parseOrderSupportOrderListFilters(searchParams);
  const orderSupportPaymentId = parseOrderSupportPaymentId(searchParams);
  const orderSupportShipmentId = parseOrderSupportShipmentId(searchParams);
  const orderSupportWebhookFilters = parseOrderSupportWebhookFilters(searchParams);
  const orderSupportPaymentFilters = parseOrderSupportPaymentFilters(searchParams);
  const orderSupportShipmentListFilters = parseOrderSupportShipmentListFilters(searchParams);
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
  const commerceFinanceSellerId = parseCommerceFinanceSellerId(searchParams);
  const rbacUserListFilters = parseRbacUserListFilters(searchParams);
  const rbacSelectedUserId = parseRbacSelectedUserId(searchParams);
  const rbacSelectedRoleId = parseRbacSelectedRoleId(searchParams);

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
          investigationUserListFilters,
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
    investigationUserListFilters,
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
          paymentFilters:
            sectionId === "orderSupport" && defaultTab === "payment-detail"
              ? orderSupportPaymentFilters
              : undefined,
          orderListFilters:
            sectionId === "orderSupport" && defaultTab === "order-detail"
              ? orderSupportOrderListFilters
              : undefined,
          shipmentListFilters:
            sectionId === "orderSupport" && defaultTab === "shipment-detail"
              ? orderSupportShipmentListFilters
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
          ...(sectionId === "rolePermission"
            ? resolveRbacUrlParams(defaultTab, {
                rbacUserListFilters,
                rbacSelectedUserId,
                rbacSelectedRoleId,
              })
            : {}),
          investigationUserListFilters:
            sectionId === "userInvestigation" ? investigationUserListFilters : undefined,
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
      investigationUserListFilters,
      orderSupportOrderId,
      orderSupportOrderListFilters,
      orderSupportPaymentId,
      orderSupportShipmentId,
      orderSupportWebhookFilters,
      orderSupportPaymentFilters,
      orderSupportShipmentListFilters,
      rbacSelectedUserId,
      rbacSelectedRoleId,
      rbacUserListFilters,
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
          paymentFilters:
            adminTopTab === "orderSupport" && childId === "payment-detail"
              ? orderSupportPaymentFilters
              : undefined,
          orderListFilters:
            adminTopTab === "orderSupport" && childId === "order-detail"
              ? orderSupportOrderListFilters
              : undefined,
          shipmentListFilters:
            adminTopTab === "orderSupport" && childId === "shipment-detail"
              ? orderSupportShipmentListFilters
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
          ...(adminTopTab === "rolePermission"
            ? resolveRbacUrlParams(childId, {
                rbacUserListFilters,
                rbacSelectedUserId,
                rbacSelectedRoleId,
              })
            : {}),
          investigationUserListFilters:
            adminTopTab === "userInvestigation" ? investigationUserListFilters : undefined,
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
      investigationUserListFilters,
      orderSupportOrderId,
      orderSupportOrderListFilters,
      orderSupportPaymentId,
      orderSupportShipmentId,
      orderSupportWebhookFilters,
      orderSupportPaymentFilters,
      orderSupportShipmentListFilters,
      rbacSelectedUserId,
      rbacSelectedRoleId,
      rbacUserListFilters,
      searchParams,
      setSearchParams,
    ],
  );

  const handleRbacRoleIdChange = useCallback(
    (nextRoleId) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "rolePermission",
          tab: activeChildTab,
          ...resolveRbacUrlParams(activeChildTab, {
            rbacUserListFilters,
            rbacSelectedUserId,
            rbacSelectedRoleId: nextRoleId,
          }),
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [activeChildTab, rbacSelectedUserId, rbacUserListFilters, searchParams, setSearchParams],
  );

  const handleInvestigationTargetChange = useCallback(
    ({ userId, user }) => {
      setInvestigationTargetUser(user ?? null);
      setSearchParams(
        buildAdminSearchParams({
          section: "userInvestigation",
          tab: activeChildTab,
          // null = explicit clear (do not fall back to preserved URL userId)
          userId: userId ? userId : null,
          investigationUserListFilters,
          preserve: searchParams,
        }),
        { replace: true },
      );
      setAlert(null);
    },
    [activeChildTab, investigationUserListFilters, searchParams, setSearchParams],
  );

  const handleInvestigationUserListFiltersChange = useCallback(
    (filters) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "userInvestigation",
          tab: activeChildTab,
          userId: investigationUserId || undefined,
          investigationUserListFilters: filters,
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [activeChildTab, investigationUserId, searchParams, setSearchParams],
  );

  const handleInvestigationListUserSelect = useCallback(
    (userId, user) => {
      handleInvestigationTargetChange({ userId, user });
    },
    [handleInvestigationTargetChange],
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
          paymentFilters:
            activeChildTab === "payment-detail" ? orderSupportPaymentFilters : undefined,
          orderListFilters:
            activeChildTab === "order-detail" ? orderSupportOrderListFilters : undefined,
          shipmentListFilters:
            activeChildTab === "shipment-detail" ? orderSupportShipmentListFilters : undefined,
          preserve: searchParams,
        }),
        { replace: true },
      );
      setAlert(null);
    },
    [
      activeChildTab,
      orderSupportOrderId,
      orderSupportOrderListFilters,
      orderSupportPaymentId,
      orderSupportShipmentId,
      orderSupportWebhookFilters,
      orderSupportPaymentFilters,
      orderSupportShipmentListFilters,
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

  const handleOrderListFiltersChange = useCallback(
    (filters) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "orderSupport",
          tab: "order-detail",
          orderId: orderSupportOrderId,
          paymentId: orderSupportPaymentId,
          shipmentId: orderSupportShipmentId,
          orderListFilters: filters,
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

  const handleOrderSelect = useCallback(
    (nextOrderId) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "orderSupport",
          tab: "order-detail",
          orderId: nextOrderId,
          paymentId: orderSupportPaymentId,
          shipmentId: orderSupportShipmentId,
          orderListFilters: orderSupportOrderListFilters,
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [
      orderSupportPaymentId,
      orderSupportShipmentId,
      orderSupportOrderListFilters,
      searchParams,
      setSearchParams,
    ],
  );

  const handlePaymentFiltersChange = useCallback(
    (filters) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "orderSupport",
          tab: "payment-detail",
          orderId: orderSupportOrderId,
          paymentId: orderSupportPaymentId,
          shipmentId: orderSupportShipmentId,
          paymentFilters: filters,
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

  const handleShipmentListFiltersChange = useCallback(
    (filters) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "orderSupport",
          tab: "shipment-detail",
          orderId: orderSupportOrderId,
          paymentId: orderSupportPaymentId,
          shipmentId: orderSupportShipmentId,
          shipmentListFilters: filters,
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

  const handleShipmentSelect = useCallback(
    (nextShipmentId) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "orderSupport",
          tab: "shipment-detail",
          orderId: orderSupportOrderId,
          paymentId: orderSupportPaymentId,
          shipmentId: nextShipmentId,
          shipmentListFilters: orderSupportShipmentListFilters,
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [
      orderSupportOrderId,
      orderSupportPaymentId,
      orderSupportShipmentListFilters,
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
          postModerationListFilters,
          commentModerationListFilters,
          preserve: searchParams,
        }),
        { replace: true },
      );
      setAlert(null);
    },
    [
      activeChildTab,
      commentModerationListFilters,
      contentModerationCommentId,
      contentModerationPostId,
      contentModerationProductId,
      contentModerationProductView,
      postModerationListFilters,
      searchParams,
      setSearchParams,
    ],
  );

  const handlePostModerationListFiltersChange = useCallback(
    (filters) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "contentModeration",
          tab: activeChildTab,
          postId: contentModerationPostId || undefined,
          commentId: contentModerationCommentId || undefined,
          productId: contentModerationProductId || undefined,
          productView: contentModerationProductView,
          postModerationListFilters: filters,
          commentModerationListFilters,
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [
      activeChildTab,
      commentModerationListFilters,
      contentModerationCommentId,
      contentModerationPostId,
      contentModerationProductId,
      contentModerationProductView,
      searchParams,
      setSearchParams,
    ],
  );

  const handleCommentModerationListFiltersChange = useCallback(
    (filters) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "contentModeration",
          tab: activeChildTab,
          postId: contentModerationPostId || undefined,
          commentId: contentModerationCommentId || undefined,
          productId: contentModerationProductId || undefined,
          productView: contentModerationProductView,
          postModerationListFilters,
          commentModerationListFilters: filters,
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [
      activeChildTab,
      commentModerationListFilters,
      contentModerationCommentId,
      contentModerationPostId,
      contentModerationProductId,
      contentModerationProductView,
      postModerationListFilters,
      searchParams,
      setSearchParams,
    ],
  );

  const handlePostModerationListSelect = useCallback(
    (postId) => {
      if (postId === contentModerationPostId) {
        handleContentModerationTargetChange({ postId: "" });
        return;
      }
      handleContentModerationTargetChange({ postId });
    },
    [contentModerationPostId, handleContentModerationTargetChange],
  );

  const handlePostModerationClear = useCallback(() => {
    handleContentModerationTargetChange({ postId: "" });
  }, [handleContentModerationTargetChange]);

  const handleCommentModerationListSelect = useCallback(
    (commentId) => {
      handleContentModerationTargetChange({ commentId });
    },
    [handleContentModerationTargetChange],
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
      setSearchParams(
        buildAdminSearchParams({
          section: "rolePermission",
          tab: "role-permissions",
          rbacRoleId: roleId,
          preserve: searchParams,
        }),
        { replace: true },
      );
      setAlert(null);
    },
    [searchParams, setSearchParams],
  );

  const handleRbacUserListFiltersChange = useCallback(
    (filters) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "rolePermission",
          tab: activeChildTab,
          ...resolveRbacUrlParams(activeChildTab, {
            rbacUserListFilters: filters,
            rbacSelectedUserId,
            rbacSelectedRoleId,
          }),
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [activeChildTab, rbacSelectedRoleId, rbacSelectedUserId, searchParams, setSearchParams],
  );

  const handleRbacUserSelect = useCallback(
    (nextUserId) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "rolePermission",
          tab: activeChildTab,
          ...resolveRbacUrlParams(activeChildTab, {
            rbacUserListFilters,
            rbacSelectedUserId: nextUserId,
            rbacSelectedRoleId,
          }),
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [activeChildTab, rbacSelectedRoleId, rbacUserListFilters, searchParams, setSearchParams],
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
  const CommerceFinanceTabComponent =
    COMMERCE_FINANCE_TAB_COMPONENTS[activeChildTab] || AdminFinanceOverviewTab;
  const CatalogManagementTabComponent =
    CATALOG_MANAGEMENT_TAB_COMPONENTS[activeChildTab] || CategoryManagementTab;

  const roleTabProps = {
    onNotify,
    onTabChange: handleChildTabChange,
    selectedRoleId: rbacSelectedRoleId,
    onSelectedRoleIdChange: handleRbacRoleIdChange,
    onViewRolePermissions,
    rbacUserListFilters,
    rbacSelectedUserId,
    onRbacUserListFiltersChange: handleRbacUserListFiltersChange,
    onRbacUserSelect: handleRbacUserSelect,
  };

  const investigationTabProps = {
    userId: investigationUserId,
    targetUser: investigationTargetUser,
    onNotify,
  };

  const orderSupportTabProps = {
    orderId: orderSupportOrderId,
    orderListFilters: orderSupportOrderListFilters,
    paymentId: orderSupportPaymentId,
    shipmentId: orderSupportShipmentId,
    webhookFilters: orderSupportWebhookFilters,
    paymentFilters: orderSupportPaymentFilters,
    shipmentListFilters: orderSupportShipmentListFilters,
    onNavigate: handleSupportNavigate,
    onFiltersChange: handleWebhookFiltersChange,
    onOrderListFiltersChange: handleOrderListFiltersChange,
    onOrderSelect: handleOrderSelect,
    onPaymentFiltersChange: handlePaymentFiltersChange,
    onShipmentListFiltersChange: handleShipmentListFiltersChange,
    onShipmentSelect: handleShipmentSelect,
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
      if (TabComponent === PostModerationTab) {
        return null;
      }
      if (TabComponent === CommentModerationTab) {
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
      if (OrderSupportTabComponent === AdminRefundApprovalsTab) {
        return <OrderSupportTabComponent onNotify={onNotify} />;
      }
      return <OrderSupportTabComponent {...orderSupportTabProps} />;
    }
    if (adminTopTab === "commerceFinance") {
      if (CommerceFinanceTabComponent === AdminFinanceSellerDetailTab) {
        return <CommerceFinanceTabComponent sellerId={commerceFinanceSellerId} />;
      }
      if (CommerceFinanceTabComponent === AdminFinancePayoutQueueTab) {
        return <CommerceFinanceTabComponent onNotify={onNotify} />;
      }
      return <CommerceFinanceTabComponent />;
    }
    if (adminTopTab === "catalogManagement") {
      return <CatalogManagementTabComponent onNotify={onNotify} />;
    }
    return null;
  }, [
    AdminAuditTabComponent,
    ContentModerationTabComponent,
    InvestigationTabComponent,
    OrderSupportTabComponent,
    CommerceFinanceTabComponent,
    CatalogManagementTabComponent,
    SystemOperationsTabComponent,
    RoleTabComponent,
    adminTopTab,
    commerceFinanceSellerId,
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
          <>
            <AdminUserTargetBar
              userId={investigationUserId}
              selectedUser={investigationTargetUser}
              onTargetChange={handleInvestigationTargetChange}
            />
            <InvestigationUserListPanel
              userListFilters={investigationUserListFilters}
              onFiltersChange={handleInvestigationUserListFiltersChange}
              selectedUserId={investigationUserId}
              targetUser={investigationTargetUser}
              onUserSelect={handleInvestigationListUserSelect}
              onSelectedUserSync={setInvestigationTargetUser}
            />
          </>
        ) : null}

        {adminTopTab === "orderSupport" && activeChildTab !== "refund-approvals" ? (
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

        {adminTopTab === "contentModeration" && activeChildTab === "comment-moderation" ? (
          <ContentModerationTargetBar
            activeTab={activeChildTab}
            targetIds={{
              postId: contentModerationPostId,
              commentId: contentModerationCommentId,
            }}
            onTargetChange={handleContentModerationTargetChange}
          />
        ) : null}

        {adminTopTab === "contentModeration" && activeChildTab === "post-moderation" ? (
          <PostModerationListPanel
            listFilters={postModerationListFilters}
            onFiltersChange={handlePostModerationListFiltersChange}
            selectedPostId={contentModerationPostId}
            onPostSelect={handlePostModerationListSelect}
            onPostClear={handlePostModerationClear}
          />
        ) : null}

        {adminTopTab === "contentModeration" && activeChildTab === "comment-moderation" ? (
          <CommentModerationListPanel
            listFilters={commentModerationListFilters}
            onFiltersChange={handleCommentModerationListFiltersChange}
            selectedCommentId={contentModerationCommentId}
            onCommentSelect={handleCommentModerationListSelect}
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
