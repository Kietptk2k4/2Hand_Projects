import { useCallback, useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { AdminProductRemovalTab } from "../../commerce/components/AdminProductRemovalTab";
import { ProductModerationListPanel } from "../../commerce/components/ProductModerationListPanel.jsx";
import { AdminReviewModerationTab } from "../../commerce/components/AdminReviewModerationTab";
import { ReviewModerationListPanel } from "../../commerce/components/ReviewModerationListPanel.jsx";
import { SystemConfigListPanel } from "../admin/systemOperations/components/SystemConfigListPanel.jsx";
import { SystemAnnouncementListPanel } from "../admin/systemOperations/components/SystemAnnouncementListPanel.jsx";
import { ModelRegistryListPanel } from "../admin/systemOperations/components/ModelRegistryListPanel.jsx";
import { AdminShopModerationTab } from "../../commerce/components/AdminShopModerationTab";
import { ShopModerationListPanel } from "../../commerce/components/ShopModerationListPanel.jsx";
import {
  buildAdminSearchParams,
  parseAdminAuditFilters,
  parseAdminAuditLogId,
  parseAdminSection,
  parseAdminTab,
  parseContentModerationCommentId,
  parseContentModerationPostId,
  parseContentModerationProductId,
  parseCommerceFinanceSellerId,
  parseCommentModerationListFilters,
  parseShopModerationListFilters,
  parseProductModerationListFilters,
  parseReviewModerationListFilters,
  parseContentModerationShopId,
  parseContentModerationReviewId,
  parseInvestigationUserId,
  parseInvestigationUserListFilters,
  parsePostModerationListFilters,
  parseOrderSupportOrderId,
  parseOrderSupportOrderListFilters,
  parseOrderSupportOrderView,
  parseOrderSupportPaymentId,
  parseOrderSupportPaymentFilters,
  parseOrderSupportPaymentView,
  parseOrderSupportRefundListFilters,
  parseOrderSupportRefundRequestId,
  parseOrderSupportRefundView,
  parseOrderSupportShipmentId,
  parseOrderSupportShipmentListFilters,
  parseOrderSupportShipmentView,
  parseOrderSupportWebhookFilters,
  parseOrderSupportWebhookLogId,
  parseOrderSupportWebhookProvider,
  parseRbacSelectedUserId,
  parseRbacSelectedRoleId,
  parseRbacUserListFilters,
  parseSystemOperationsAnnouncementFilters,
  parseSystemOperationsConfigFilters,
  parseSystemOperationsConfigId,
  parseSystemOperationsConfigView,
  parseSystemOperationsAnnouncementId,
  parseSystemOperationsAnnouncementView,
  parseSystemOperationsModelRegistryFilters,
  parseSystemOperationsModelRegistryVersion,
  parseSystemOperationsModelRegistryView,
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
import { OrderSupportListPanel } from "../admin/orderSupport/components/OrderSupportListPanel.jsx";
import { PaymentSupportDetailTab } from "../admin/orderSupport/components/tabs/PaymentSupportDetailTab.jsx";
import { PaymentSupportListPanel } from "../admin/orderSupport/components/PaymentSupportListPanel.jsx";
import { ShipmentSupportListPanel } from "../admin/orderSupport/components/ShipmentSupportListPanel.jsx";
import { ShipmentSupportDetailTab } from "../admin/orderSupport/components/tabs/ShipmentSupportDetailTab.jsx";
import { AdminSupportTargetBar } from "../admin/orderSupport/components/AdminSupportTargetBar.jsx";
import { WebhookLogsSupportTab } from "../admin/orderSupport/components/tabs/WebhookLogsSupportTab.jsx";
import { WebhookSupportListPanel } from "../admin/orderSupport/components/WebhookSupportListPanel.jsx";
import { RefundSupportListPanel } from "../admin/orderSupport/components/RefundSupportListPanel.jsx";
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
import { ModelRegistryTab } from "../admin/systemOperations/components/tabs/ModelRegistryTab.jsx";
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
  "model-registry": ModelRegistryTab,
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
  const shopModerationListFilters = useMemo(
    () => parseShopModerationListFilters(searchParams),
    [searchParams],
  );
  const productModerationListFilters = useMemo(
    () => parseProductModerationListFilters(searchParams),
    [searchParams],
  );
  const reviewModerationListFilters = useMemo(
    () => parseReviewModerationListFilters(searchParams),
    [searchParams],
  );
  const orderSupportOrderId = parseOrderSupportOrderId(searchParams);
  const orderSupportOrderView = parseOrderSupportOrderView(searchParams);
  const orderSupportOrderListFilters = parseOrderSupportOrderListFilters(searchParams);
  const orderSupportPaymentId = parseOrderSupportPaymentId(searchParams);
  const orderSupportPaymentView = parseOrderSupportPaymentView(searchParams);
  const orderSupportShipmentId = parseOrderSupportShipmentId(searchParams);
  const orderSupportShipmentView = parseOrderSupportShipmentView(searchParams);
  const orderSupportWebhookFilters = parseOrderSupportWebhookFilters(searchParams);
  const orderSupportWebhookLogId = parseOrderSupportWebhookLogId(searchParams);
  const orderSupportWebhookLogProvider = parseOrderSupportWebhookProvider(searchParams);
  const orderSupportPaymentFilters = parseOrderSupportPaymentFilters(searchParams);
  const orderSupportShipmentListFilters = parseOrderSupportShipmentListFilters(searchParams);
  const orderSupportRefundRequestId = parseOrderSupportRefundRequestId(searchParams);
  const orderSupportRefundView = parseOrderSupportRefundView(searchParams);
  const orderSupportRefundListFilters = parseOrderSupportRefundListFilters(searchParams);
  const adminAuditFilters = parseAdminAuditFilters(searchParams);
  const adminAuditLogId = parseAdminAuditLogId(searchParams);
  const contentModerationPostId = parseContentModerationPostId(searchParams);
  const contentModerationCommentId = parseContentModerationCommentId(searchParams);
  const contentModerationShopId = parseContentModerationShopId(searchParams);
  const contentModerationProductId = parseContentModerationProductId(searchParams);
  const contentModerationReviewId = parseContentModerationReviewId(searchParams);
  const systemOperationsConfigFilters = parseSystemOperationsConfigFilters(searchParams);
  const systemOperationsAnnouncementFilters = parseSystemOperationsAnnouncementFilters(searchParams);
  const systemOperationsConfigId = parseSystemOperationsConfigId(searchParams);
  const systemOperationsConfigView = parseSystemOperationsConfigView(searchParams);
  const systemOperationsAnnouncementId = parseSystemOperationsAnnouncementId(searchParams);
  const systemOperationsAnnouncementView = parseSystemOperationsAnnouncementView(searchParams);
  const systemOperationsModelRegistryFilters = parseSystemOperationsModelRegistryFilters(searchParams);
  const systemOperationsModelRegistryVersion = parseSystemOperationsModelRegistryVersion(searchParams);
  const systemOperationsModelRegistryView = parseSystemOperationsModelRegistryView(searchParams);
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
          preserve: searchParams,
        }),
        { replace: true },
      );
      return;
    }

    if (
      searchParams.get("productView") === "history" &&
      searchParams.get("productId")
    ) {
      setSearchParams(
        buildAdminSearchParams({
          section: "contentModeration",
          tab: searchParams.get("tab") || "product-moderation",
          productId: searchParams.get("productId"),
          productModerationListFilters,
          reviewModerationListFilters,
          reviewId: contentModerationReviewId || undefined,
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
    productModerationListFilters,
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
          reviewId:
            sectionId === "contentModeration" ? contentModerationReviewId || undefined : undefined,
          productModerationListFilters:
            sectionId === "contentModeration" ? productModerationListFilters : undefined,
          reviewModerationListFilters:
            sectionId === "contentModeration" ? reviewModerationListFilters : undefined,
          shopModerationListFilters:
            sectionId === "contentModeration" ? shopModerationListFilters : undefined,
          postModerationListFilters:
            sectionId === "contentModeration" ? postModerationListFilters : undefined,
          commentModerationListFilters:
            sectionId === "contentModeration" ? commentModerationListFilters : undefined,
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
      contentModerationReviewId,
      productModerationListFilters,
      reviewModerationListFilters,
      shopModerationListFilters,
      postModerationListFilters,
      commentModerationListFilters,
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
          reviewId:
            adminTopTab === "contentModeration"
              ? contentModerationReviewId || undefined
              : undefined,
          postModerationListFilters:
            adminTopTab === "contentModeration" ? postModerationListFilters : undefined,
          commentModerationListFilters:
            adminTopTab === "contentModeration" ? commentModerationListFilters : undefined,
          shopModerationListFilters:
            adminTopTab === "contentModeration" ? shopModerationListFilters : undefined,
          productModerationListFilters:
            adminTopTab === "contentModeration" ? productModerationListFilters : undefined,
          reviewModerationListFilters:
            adminTopTab === "contentModeration" ? reviewModerationListFilters : undefined,
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
      contentModerationReviewId,
      productModerationListFilters,
      reviewModerationListFilters,
      shopModerationListFilters,
      postModerationListFilters,
      commentModerationListFilters,
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
          webhookLogId: orderSupportWebhookLogId,
          webhookLogProvider: orderSupportWebhookLogProvider,
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
      orderSupportWebhookLogId,
      orderSupportWebhookLogProvider,
      searchParams,
      setSearchParams,
    ],
  );

  const handleWebhookSupportSelectionChange = useCallback(
    ({ webhookLogId: nextWebhookLogId, webhookLogProvider: nextWebhookLogProvider }) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "orderSupport",
          tab: "webhook-logs",
          orderId: orderSupportOrderId,
          paymentId: orderSupportPaymentId,
          shipmentId: orderSupportShipmentId,
          webhookLogId: nextWebhookLogId || undefined,
          webhookLogProvider: nextWebhookLogProvider || undefined,
          clearWebhookSelection: !nextWebhookLogId,
          webhookFilters: orderSupportWebhookFilters,
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [
      orderSupportOrderId,
      orderSupportPaymentId,
      orderSupportShipmentId,
      orderSupportWebhookFilters,
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
          orderId: nextOrderId || undefined,
          orderView: nextOrderId ? orderSupportOrderView : undefined,
          clearOrderSelection: !nextOrderId,
          paymentId: orderSupportPaymentId,
          shipmentId: orderSupportShipmentId,
          orderListFilters: orderSupportOrderListFilters,
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [
      orderSupportOrderView,
      orderSupportPaymentId,
      orderSupportShipmentId,
      orderSupportOrderListFilters,
      searchParams,
      setSearchParams,
    ],
  );

  const handleOrderSupportSelectionChange = useCallback(
    ({ orderId: nextOrderId, orderView: nextOrderView }) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "orderSupport",
          tab: "order-detail",
          orderId: nextOrderId || undefined,
          orderView: nextOrderView || undefined,
          clearOrderSelection: !nextOrderId,
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

  const handlePaymentSupportSelectionChange = useCallback(
    ({ paymentId: nextPaymentId, paymentView: nextPaymentView }) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "orderSupport",
          tab: "payment-detail",
          orderId: orderSupportOrderId,
          paymentId: nextPaymentId || undefined,
          paymentView: nextPaymentView || undefined,
          clearPaymentSelection: !nextPaymentId,
          shipmentId: orderSupportShipmentId,
          paymentFilters: orderSupportPaymentFilters,
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [
      orderSupportOrderId,
      orderSupportPaymentFilters,
      orderSupportShipmentId,
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

  const handleShipmentSupportSelectionChange = useCallback(
    ({ shipmentId: nextShipmentId, shipmentView: nextShipmentView }) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "orderSupport",
          tab: "shipment-detail",
          orderId: orderSupportOrderId,
          paymentId: orderSupportPaymentId,
          shipmentId: nextShipmentId || undefined,
          shipmentView: nextShipmentView || undefined,
          clearShipmentSelection: !nextShipmentId,
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

  const handleRefundSupportSelectionChange = useCallback(
    ({ refundRequestId: nextRefundRequestId, refundView: nextRefundView }) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "orderSupport",
          tab: "refund-approvals",
          orderId: orderSupportOrderId,
          paymentId: orderSupportPaymentId,
          shipmentId: orderSupportShipmentId,
          refundRequestId: nextRefundRequestId || undefined,
          refundView: nextRefundView || undefined,
          clearRefundSelection: !nextRefundRequestId,
          refundListFilters: orderSupportRefundListFilters,
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [
      orderSupportOrderId,
      orderSupportPaymentId,
      orderSupportShipmentId,
      orderSupportRefundListFilters,
      searchParams,
      setSearchParams,
    ],
  );

  const handleRefundListFiltersChange = useCallback(
    (filters) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "orderSupport",
          tab: "refund-approvals",
          orderId: orderSupportOrderId,
          paymentId: orderSupportPaymentId,
          shipmentId: orderSupportShipmentId,
          refundRequestId: orderSupportRefundRequestId,
          refundListFilters: filters,
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [
      orderSupportOrderId,
      orderSupportPaymentId,
      orderSupportRefundRequestId,
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

  const handleContentModerationTargetChange = useCallback(
    (patch) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "contentModeration",
          tab: activeChildTab,
          postId: "postId" in patch ? patch.postId : contentModerationPostId,
          commentId: "commentId" in patch ? patch.commentId : contentModerationCommentId,
          shopId: "shopId" in patch ? patch.shopId : contentModerationShopId,
          productId: "productId" in patch ? patch.productId : contentModerationProductId,
          reviewId: "reviewId" in patch ? patch.reviewId : contentModerationReviewId,
          postModerationListFilters,
          commentModerationListFilters,
          shopModerationListFilters,
          productModerationListFilters,
          reviewModerationListFilters,
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
      contentModerationShopId,
      contentModerationPostId,
      contentModerationProductId,
      contentModerationReviewId,
      postModerationListFilters,
      shopModerationListFilters,
      productModerationListFilters,
      reviewModerationListFilters,
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
          shopId: contentModerationShopId || undefined,
          postModerationListFilters: filters,
          commentModerationListFilters,
          shopModerationListFilters,
          productModerationListFilters,
          reviewModerationListFilters,
          reviewId: contentModerationReviewId || undefined,
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
      contentModerationShopId,
      shopModerationListFilters,
      productModerationListFilters,
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
          shopId: contentModerationShopId || undefined,
          postModerationListFilters,
          commentModerationListFilters: filters,
          shopModerationListFilters,
          productModerationListFilters,
          reviewModerationListFilters,
          reviewId: contentModerationReviewId || undefined,
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
      contentModerationShopId,
      shopModerationListFilters,
      productModerationListFilters,
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

  const handleCommentModerationClear = useCallback(() => {
    handleContentModerationTargetChange({ commentId: "" });
  }, [handleContentModerationTargetChange]);

  const handleCommentModerationListSelect = useCallback(
    (commentId) => {
      if (commentId === contentModerationCommentId) {
        handleCommentModerationClear();
        return;
      }
      handleContentModerationTargetChange({ commentId });
    },
    [contentModerationCommentId, handleCommentModerationClear, handleContentModerationTargetChange],
  );

  const handleShopModerationListFiltersChange = useCallback(
    (filters) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "contentModeration",
          tab: activeChildTab,
          postId: contentModerationPostId || undefined,
          commentId: contentModerationCommentId || undefined,
          shopId: contentModerationShopId || undefined,
          productId: contentModerationProductId || undefined,
          postModerationListFilters,
          commentModerationListFilters,
          shopModerationListFilters: filters,
          productModerationListFilters,
          reviewModerationListFilters,
          reviewId: contentModerationReviewId || undefined,
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
      contentModerationShopId,
      productModerationListFilters,
      postModerationListFilters,
      searchParams,
      setSearchParams,
    ],
  );

  const handleShopModerationClear = useCallback(() => {
    handleContentModerationTargetChange({ shopId: "" });
  }, [handleContentModerationTargetChange]);

  const handleShopModerationListSelect = useCallback(
    (shopId) => {
      if (shopId === contentModerationShopId) {
        handleShopModerationClear();
        return;
      }
      handleContentModerationTargetChange({ shopId });
    },
    [contentModerationShopId, handleContentModerationTargetChange, handleShopModerationClear],
  );


  const handleProductModerationListFiltersChange = useCallback(
    (filters) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "contentModeration",
          tab: activeChildTab,
          postId: contentModerationPostId || undefined,
          commentId: contentModerationCommentId || undefined,
          shopId: contentModerationShopId || undefined,
          productId: contentModerationProductId || undefined,
          postModerationListFilters,
          commentModerationListFilters,
          shopModerationListFilters,
          productModerationListFilters: filters,
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
      contentModerationShopId,
      postModerationListFilters,
      shopModerationListFilters,
      searchParams,
      setSearchParams,
    ],
  );

  const handleProductModerationClear = useCallback(() => {
    handleContentModerationTargetChange({ productId: "" });
  }, [handleContentModerationTargetChange]);

  const handleProductModerationListSelect = useCallback(
    (productId) => {
      if (productId === contentModerationProductId) {
        handleProductModerationClear();
        return;
      }
      handleContentModerationTargetChange({ productId });
    },
    [contentModerationProductId, handleContentModerationTargetChange, handleProductModerationClear],
  );

  const handleReviewModerationListFiltersChange = useCallback(
    (filters) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "contentModeration",
          tab: activeChildTab,
          postId: contentModerationPostId || undefined,
          commentId: contentModerationCommentId || undefined,
          shopId: contentModerationShopId || undefined,
          productId: contentModerationProductId || undefined,
          reviewId: contentModerationReviewId || undefined,
          postModerationListFilters,
          commentModerationListFilters,
          shopModerationListFilters,
          productModerationListFilters,
          reviewModerationListFilters: filters,
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
      contentModerationReviewId,
      contentModerationShopId,
      postModerationListFilters,
      shopModerationListFilters,
      productModerationListFilters,
      searchParams,
      setSearchParams,
    ],
  );

  const handleReviewModerationClear = useCallback(() => {
    handleContentModerationTargetChange({ reviewId: "" });
  }, [handleContentModerationTargetChange]);

  const handleReviewModerationListSelect = useCallback(
    (reviewId) => {
      if (reviewId === contentModerationReviewId) {
        handleReviewModerationClear();
        return;
      }
      handleContentModerationTargetChange({ reviewId });
    },
    [contentModerationReviewId, handleContentModerationTargetChange, handleReviewModerationClear],
  );

  const handleSystemOperationsConfigFiltersChange = useCallback(
    (filters) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "systemOperations",
          tab: activeChildTab,
          configFilters: filters,
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

  const handleSystemOperationsModelRegistryFiltersChange = useCallback(
    (filters) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "systemOperations",
          tab: activeChildTab,
          modelRegistryFilters: filters,
          mrVersion: systemOperationsModelRegistryVersion || undefined,
          mrView: systemOperationsModelRegistryView || undefined,
          preserve: searchParams,
        }),
        { replace: true },
      );
      setAlert(null);
    },
    [
      activeChildTab,
      searchParams,
      setSearchParams,
      systemOperationsModelRegistryVersion,
      systemOperationsModelRegistryView,
    ],
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
          announcementId: systemOperationsAnnouncementId || undefined,
          announcementView: systemOperationsAnnouncementView || undefined,
          mrVersion: systemOperationsModelRegistryVersion || undefined,
          mrView: systemOperationsModelRegistryView || undefined,
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
      systemOperationsAnnouncementId,
      systemOperationsAnnouncementView,
      systemOperationsConfigFilters,
      systemOperationsModelRegistryVersion,
      systemOperationsModelRegistryView,
    ],
  );

  const handleSystemOperationsAnnouncementSelectionChange = useCallback(
    ({ announcementId: nextAnnouncementId, announcementView: nextAnnouncementView }) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "systemOperations",
          tab: activeChildTab,
          configFilters: systemOperationsConfigFilters,
          announcementFilters: systemOperationsAnnouncementFilters,
          configId: systemOperationsConfigId || undefined,
          configView: systemOperationsConfigView || undefined,
          announcementId: nextAnnouncementId || undefined,
          announcementView: nextAnnouncementView || undefined,
          mrVersion: systemOperationsModelRegistryVersion || undefined,
          mrView: systemOperationsModelRegistryView || undefined,
          clearAnnouncementSelection: !nextAnnouncementId,
          preserve: searchParams,
        }),
        { replace: true },
      );
    },
    [
      activeChildTab,
      systemOperationsAnnouncementFilters,
      systemOperationsConfigFilters,
      systemOperationsConfigId,
      systemOperationsConfigView,
      systemOperationsModelRegistryVersion,
      systemOperationsModelRegistryView,
      searchParams,
      setSearchParams,
    ],
  );

  const handleSystemOperationsModelRegistrySelectionChange = useCallback(
    ({ mrVersion: nextMrVersion, mrView: nextMrView }) => {
      setSearchParams(
        buildAdminSearchParams({
          section: "systemOperations",
          tab: activeChildTab,
          configFilters: systemOperationsConfigFilters,
          announcementFilters: systemOperationsAnnouncementFilters,
          modelRegistryFilters: systemOperationsModelRegistryFilters,
          configId: systemOperationsConfigId || undefined,
          configView: systemOperationsConfigView || undefined,
          announcementId: systemOperationsAnnouncementId || undefined,
          announcementView: systemOperationsAnnouncementView || undefined,
          mrVersion: nextMrVersion || undefined,
          mrView: nextMrView || undefined,
          clearModelRegistrySelection: !nextMrVersion,
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
      systemOperationsAnnouncementId,
      systemOperationsAnnouncementView,
      systemOperationsConfigFilters,
      systemOperationsConfigId,
      systemOperationsConfigView,
      systemOperationsModelRegistryFilters,
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
        return null;
      }
      if (TabComponent === AdminShopModerationTab) {
        return null;
      }
      if (TabComponent === AdminProductRemovalTab) {
        return null;
      }
      return <TabComponent />;
    }
    if (adminTopTab === "systemOperations") {
      return <SystemOperationsTabComponent {...systemOperationsTabProps} />;
    }
    if (adminTopTab === "orderSupport") {
      if (OrderSupportTabComponent === AdminRefundApprovalsTab) {
        return null;
      }
      if (OrderSupportTabComponent === OrderSupportDetailTab) {
        return null;
      }
      if (OrderSupportTabComponent === PaymentSupportDetailTab) {
        return null;
      }
      if (OrderSupportTabComponent === ShipmentSupportDetailTab) {
        return null;
      }
      if (OrderSupportTabComponent === WebhookLogsSupportTab) {
        return null;
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

        {adminTopTab === "orderSupport" &&
        activeChildTab !== "refund-approvals" &&
        activeChildTab !== "order-detail" &&
        activeChildTab !== "payment-detail" &&
        activeChildTab !== "shipment-detail" &&
        activeChildTab !== "webhook-logs" ? (
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

        {adminTopTab === "orderSupport" && activeChildTab === "order-detail" ? (
          <OrderSupportListPanel
            orderListFilters={orderSupportOrderListFilters}
            onFiltersChange={handleOrderListFiltersChange}
            orderId={orderSupportOrderId}
            orderView={orderSupportOrderView}
            onOrderSelectionChange={handleOrderSupportSelectionChange}
            onNavigate={handleSupportNavigate}
          />
        ) : null}

        {adminTopTab === "orderSupport" && activeChildTab === "payment-detail" ? (
          <PaymentSupportListPanel
            paymentFilters={orderSupportPaymentFilters}
            onFiltersChange={handlePaymentFiltersChange}
            paymentId={orderSupportPaymentId}
            paymentView={orderSupportPaymentView}
            orderId={orderSupportOrderId}
            onPaymentSelectionChange={handlePaymentSupportSelectionChange}
            onNavigate={handleSupportNavigate}
          />
        ) : null}

        {adminTopTab === "orderSupport" && activeChildTab === "shipment-detail" ? (
          <ShipmentSupportListPanel
            shipmentListFilters={orderSupportShipmentListFilters}
            onFiltersChange={handleShipmentListFiltersChange}
            shipmentId={orderSupportShipmentId}
            shipmentView={orderSupportShipmentView}
            onShipmentSelectionChange={handleShipmentSupportSelectionChange}
            onNavigate={handleSupportNavigate}
            onNotify={onNotify}
          />
        ) : null}

        {adminTopTab === "orderSupport" && activeChildTab === "refund-approvals" ? (
          <RefundSupportListPanel
            refundListFilters={orderSupportRefundListFilters}
            onFiltersChange={handleRefundListFiltersChange}
            refundRequestId={orderSupportRefundRequestId}
            refundView={orderSupportRefundView}
            onRefundSelectionChange={handleRefundSupportSelectionChange}
            onNavigate={handleSupportNavigate}
            onNotify={onNotify}
          />
        ) : null}

        {adminTopTab === "orderSupport" && activeChildTab === "webhook-logs" ? (
          <WebhookSupportListPanel
            webhookFilters={orderSupportWebhookFilters}
            onFiltersChange={handleWebhookFiltersChange}
            webhookLogId={orderSupportWebhookLogId}
            webhookLogProvider={orderSupportWebhookLogProvider}
            onWebhookSelectionChange={handleWebhookSupportSelectionChange}
            onNavigate={handleSupportNavigate}
            onNotify={onNotify}
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
            onCommentClear={handleCommentModerationClear}
          />
        ) : null}

        {adminTopTab === "contentModeration" && activeChildTab === "shop-moderation" ? (
          <ShopModerationListPanel
            listFilters={shopModerationListFilters}
            onFiltersChange={handleShopModerationListFiltersChange}
            selectedShopId={contentModerationShopId}
            onShopSelect={handleShopModerationListSelect}
            onShopClear={handleShopModerationClear}
          />
        ) : null}

        {adminTopTab === "contentModeration" && activeChildTab === "product-moderation" ? (
          <ProductModerationListPanel
            listFilters={productModerationListFilters}
            onFiltersChange={handleProductModerationListFiltersChange}
            selectedProductId={contentModerationProductId}
            onProductSelect={handleProductModerationListSelect}
            onProductClear={handleProductModerationClear}
          />
        ) : null}

        {adminTopTab === "contentModeration" && activeChildTab === "review-moderation" ? (
          <ReviewModerationListPanel
            listFilters={reviewModerationListFilters}
            onFiltersChange={handleReviewModerationListFiltersChange}
            selectedReviewId={contentModerationReviewId}
            onReviewSelect={handleReviewModerationListSelect}
            onReviewClear={handleReviewModerationClear}
          />
        ) : null}

        {adminTopTab === "systemOperations" && activeChildTab === "system-configs" ? (
          <SystemConfigListPanel
            configFilters={systemOperationsConfigFilters}
            onFiltersChange={handleSystemOperationsConfigFiltersChange}
            configId={systemOperationsConfigId}
            configView={systemOperationsConfigView}
            onConfigSelectionChange={handleSystemOperationsConfigSelectionChange}
          />
        ) : null}

        {adminTopTab === "systemOperations" && activeChildTab === "system-announcements" ? (
          <SystemAnnouncementListPanel
            announcementFilters={systemOperationsAnnouncementFilters}
            onFiltersChange={handleSystemOperationsAnnouncementFiltersChange}
            announcementId={systemOperationsAnnouncementId}
            announcementView={systemOperationsAnnouncementView}
            onAnnouncementSelectionChange={handleSystemOperationsAnnouncementSelectionChange}
          />
        ) : null}

        {adminTopTab === "systemOperations" && activeChildTab === "model-registry" ? (
          <ModelRegistryListPanel
            modelRegistryFilters={systemOperationsModelRegistryFilters}
            onFiltersChange={handleSystemOperationsModelRegistryFiltersChange}
            mrVersion={systemOperationsModelRegistryVersion}
            mrView={systemOperationsModelRegistryView}
            onModelRegistrySelectionChange={handleSystemOperationsModelRegistrySelectionChange}
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
