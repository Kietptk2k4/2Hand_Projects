import { adminAuthHandlers } from "./adminAuthHandlers";
import { adminInvestigationHandlers } from "./adminInvestigationHandlers";
import { adminRbacHandlers } from "./adminRbacHandlers";
import { authHandlers } from "./authHandlers";
import { socialFeedHandlers } from "./socialFeedHandlers";
import { socialCreatePostHandlers } from "./socialCreatePostHandlers";
import { socialEditPostHandlers } from "./socialEditPostHandlers";
import { socialFollowHandlers } from "./socialFollowHandlers";
import { socialProfileHandlers } from "./socialProfileHandlers";
import { socialRelationsHandlers } from "./socialRelationsHandlers";
import { socialSavedPostsHandlers } from "./socialSavedPostsHandlers";
import { socialSearchPostsHandlers } from "./socialSearchPostsHandlers";
import { socialSearchHashtagHandlers } from "./socialSearchHashtagHandlers";
import { socialCommentWriteHandlers } from "./socialCommentWriteHandlers";
import { socialPostHandlers } from "./socialPostHandlers";
import { commerceProductListHandlers } from "./commerceProductListHandlers";
import { commerceCategoryProductsHandlers } from "./commerceCategoryProductsHandlers";
import { commerceProductSearchHandlers } from "./commerceProductSearchHandlers";
import { commerceShopProductsHandlers } from "./commerceShopProductsHandlers";
import { commerceProductDetailHandlers } from "./commerceProductDetailHandlers";
import { commerceCartHandlers } from "./commerceCartHandlers";
import { commerceProductReviewsHandlers } from "./commerceProductReviewsHandlers";
import { commerceProductReviewWriteHandlers } from "./commerceProductReviewWriteHandlers";
import { commerceAddressHandlers } from "./commerceAddressHandlers";
import { commerceCheckoutHandlers } from "./commerceCheckoutHandlers";
import { commerceOrderHandlers } from "./commerceOrderHandlers";
import { commerceOrderDetailHandlers } from "./commerceOrderDetailHandlers";
import { commerceShipmentHandlers } from "./commerceShipmentHandlers";
import { commercePaymentHandlers } from "./commercePaymentHandlers";
import { commerceSellerShopHandlers } from "./commerceSellerShopHandlers";
import { commerceSellerProductHandlers } from "./commerceSellerProductHandlers";
import { commerceSellerOrderHandlers } from "./commerceSellerOrderHandlers";
import { commerceSellerShipmentHandlers } from "./commerceSellerShipmentHandlers";
import { commerceSellerShopReviewsHandlers } from "./commerceSellerShopReviewsHandlers";
import { commerceAdminShopModerationHandlers } from "./commerceAdminShopModerationHandlers";
import { commerceAdminReviewModerationHandlers } from "./commerceAdminReviewModerationHandlers";
import { commerceAdminProductRemovalHandlers } from "./commerceAdminProductRemovalHandlers";

export const handlers = [
  ...authHandlers,
  ...adminAuthHandlers,
  ...adminRbacHandlers,
  ...adminInvestigationHandlers,
  ...socialFeedHandlers,
  ...socialSavedPostsHandlers,
  ...socialSearchPostsHandlers,
  ...socialSearchHashtagHandlers,
  ...socialPostHandlers,
  ...socialCommentWriteHandlers,
  ...socialCreatePostHandlers,
  ...socialEditPostHandlers,
  ...socialProfileHandlers,
  ...socialFollowHandlers,
  ...socialRelationsHandlers,
  ...commerceProductListHandlers,
  ...commerceCategoryProductsHandlers,
  ...commerceProductSearchHandlers,
  ...commerceShopProductsHandlers,
  ...commerceProductDetailHandlers,
  ...commerceCartHandlers,
  ...commerceProductReviewsHandlers,
  ...commerceProductReviewWriteHandlers,
  ...commerceAddressHandlers,
  ...commerceCheckoutHandlers,
  ...commercePaymentHandlers,
  ...commerceOrderHandlers,
  ...commerceOrderDetailHandlers,
  ...commerceShipmentHandlers,
  ...commerceSellerShopHandlers,
  ...commerceSellerProductHandlers,
  ...commerceSellerOrderHandlers,
  ...commerceSellerShipmentHandlers,
  ...commerceSellerShopReviewsHandlers,
  ...commerceAdminShopModerationHandlers,
  ...commerceAdminReviewModerationHandlers,
  ...commerceAdminProductRemovalHandlers,
];

