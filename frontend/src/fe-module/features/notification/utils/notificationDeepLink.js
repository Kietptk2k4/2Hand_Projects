import { APP_ROUTES } from "../../../shared/constants/routes";
import { parseNotificationMetadata } from "./notificationMapper";
import { buildCommerceSellerOrderDetailPath } from "../../commerce/utils/commerceRoutes";

function readMetadataField(notification, ...keys) {
  const metadata =
    notification?.metadata && typeof notification.metadata === "object"
      ? notification.metadata
      : parseNotificationMetadata(notification?.metadata);

  for (const key of keys) {
    if (metadata?.[key]) return metadata[key];
  }
  return null;
}

function buildSocialPostPath(postId, { focusComments = false } = {}) {
  if (!postId) return null;

  const params = new URLSearchParams();
  params.set("postId", postId);
  if (focusComments) {
    params.set("focusComments", "1");
  }

  return `${APP_ROUTES.socialFeed}?${params.toString()}`;
}

function isSellerAudience(notification) {
  return readMetadataField(notification, "recipient_audience") === "seller";
}

export function hasNotificationDeepLink(notification) {
  return Boolean(resolveNotificationDeepLink(notification));
}

export function resolveNotificationDeepLink(notification) {
  const referenceType = notification?.referenceType;
  const referenceId = notification?.referenceId;

  if (!referenceType || !referenceId) {
    return null;
  }

  switch (referenceType) {
    case "ORDER":
      if (isSellerAudience(notification)) {
        return buildCommerceSellerOrderDetailPath(referenceId);
      }
      return APP_ROUTES.commerceOrderDetail.replace(":orderId", referenceId);
    case "PAYMENT": {
      const orderId = readMetadataField(notification, "order_id", "orderId");
      if (orderId) {
        if (isSellerAudience(notification)) {
          return buildCommerceSellerOrderDetailPath(orderId);
        }
        return APP_ROUTES.commerceOrderDetail.replace(":orderId", orderId);
      }
      return isSellerAudience(notification)
        ? APP_ROUTES.commerceSellerOrders
        : APP_ROUTES.commerceOrders;
    }
    case "SHIPMENT": {
      if (isSellerAudience(notification)) {
        return APP_ROUTES.commerceSellerShipmentDetail.replace(":shipmentId", referenceId);
      }
      const orderId = readMetadataField(notification, "order_id", "orderId");
      if (orderId) {
        return APP_ROUTES.commerceShipmentTracking
          .replace(":orderId", orderId)
          .replace(":shipmentId", referenceId);
      }
      return APP_ROUTES.commerceOrders;
    }
    case "PAYOUT_REQUEST":
      return `${APP_ROUTES.commerceSellerAnalytics}#payout`;
    case "PRODUCT":
      return APP_ROUTES.commerceProductDetail.replace(":productId", referenceId);
    case "SHOP":
      return APP_ROUTES.commerceShopProducts.replace(":shopId", referenceId);
    case "USER":
      return APP_ROUTES.socialProfile.replace(":userId", referenceId);
    case "POST":
      return buildSocialPostPath(referenceId);
    case "COMMENT": {
      const postId = readMetadataField(notification, "post_id", "postId");
      return buildSocialPostPath(postId, { focusComments: true });
    }
    case "REVIEW": {
      const productId = readMetadataField(notification, "product_id", "productId");
      if (productId) {
        return APP_ROUTES.commerceProductReviews.replace(":productId", productId);
      }
      return APP_ROUTES.commerceReviewCreate;
    }
    case "USER_ENFORCEMENT":
      return APP_ROUTES.account;
    case "SYSTEM_ANNOUNCEMENT":
      return null;
    default:
      return null;
  }
}