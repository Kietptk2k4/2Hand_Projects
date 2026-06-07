import { createBrowserRouter, Navigate } from "react-router-dom";
import { APP_ROUTES } from "../../shared/constants/routes";
import { AppLayout } from "./AppLayout";
import { AdminAuthGuard } from "./AdminAuthGuard";
import { AuthGuard } from "./AuthGuard";
import { LoginPage } from "../../features/auth/pages/LoginPage";
import { RegisterPage } from "../../features/auth/pages/RegisterPage";
import { ForgotPasswordPage } from "../../features/auth/pages/ForgotPasswordPage";
import { AccountPage } from "../../features/auth/pages/AccountPage";
import { AccountSecurityPage } from "../../features/auth/pages/AccountSecurityPage";
import { VerifyEmailPage } from "../../features/auth/pages/VerifyEmailPage";
import { AccountPasswordPage } from "../../features/auth/pages/AccountPasswordPage";
import { ChangePasswordPage } from "../../features/auth/pages/ChangePasswordPage";
import { SessionExpiredPage } from "../../features/auth/pages/SessionExpiredPage";
import { AdminLoginPage } from "../../features/auth/pages/AdminLoginPage";
import { AdminPage } from "../../features/auth/pages/AdminPage";
import { SocialFeedPage } from "../../features/social/pages/SocialFeedPage";
import { SocialProfilePage } from "../../features/social/pages/SocialProfilePage";
import { SocialSavedPostsPage } from "../../features/social/pages/SocialSavedPostsPage";
import { SocialSearchPostsPage } from "../../features/social/pages/SocialSearchPostsPage";
import { SocialHashtagPostsPage } from "../../features/social/pages/SocialHashtagPostsPage";
import { CommerceHomePage } from "../../features/commerce/pages/CommerceHomePage";
import { CommerceProductDetailPage } from "../../features/commerce/pages/CommerceProductDetailPage";
import { CommerceCategoryProductsPage } from "../../features/commerce/pages/CommerceCategoryProductsPage";
import { CommerceSearchPage } from "../../features/commerce/pages/CommerceSearchPage";
import { CommerceShopProductsPage } from "../../features/commerce/pages/CommerceShopProductsPage";
import { CommerceCartPage } from "../../features/commerce/pages/CommerceCartPage";
import { CommerceUserAddressesPage } from "../../features/commerce/pages/CommerceUserAddressesPage";
import { CommerceCheckoutPage } from "../../features/commerce/pages/CommerceCheckoutPage";
import { CommerceCheckoutSuccessPage } from "../../features/commerce/pages/CommerceCheckoutSuccessPage";
import { CommerceCheckoutPaymentResultPage } from "../../features/commerce/pages/CommerceCheckoutPaymentResultPage";
import { CommerceOrderListPage } from "../../features/commerce/pages/CommerceOrderListPage";
import { CommerceOrderDetailPage } from "../../features/commerce/pages/CommerceOrderDetailPage";
import { CommerceShipmentTrackingPage } from "../../features/commerce/pages/CommerceShipmentTrackingPage";
import { CommerceProductReviewsPage } from "../../features/commerce/pages/CommerceProductReviewsPage";
import { CommerceWriteEditReviewPage } from "../../features/commerce/pages/CommerceWriteEditReviewPage";
import { CommerceCreateShopPage } from "../../features/commerce/pages/CommerceCreateShopPage";
import { CommerceShopSettingsPage } from "../../features/commerce/pages/CommerceShopSettingsPage";
import { CommerceSellerProductListPage } from "../../features/commerce/pages/CommerceSellerProductListPage";
import { CommerceSellerProductFormPage } from "../../features/commerce/pages/CommerceSellerProductFormPage";
import { CommerceSellerOrderListPage } from "../../features/commerce/pages/CommerceSellerOrderListPage";
import { CommerceSellerShipmentListPage } from "../../features/commerce/pages/CommerceSellerShipmentListPage";
import { CommerceSellerShipmentDetailPage } from "../../features/commerce/pages/CommerceSellerShipmentDetailPage";
import { CommerceSellerShopReviewsPage } from "../../features/commerce/pages/CommerceSellerShopReviewsPage";
import { CommerceAdminShopModerationPage } from "../../features/commerce/pages/CommerceAdminShopModerationPage";
import { CommerceAdminReviewModerationPage } from "../../features/commerce/pages/CommerceAdminReviewModerationPage";
import { CommerceAdminProductRemovalPage } from "../../features/commerce/pages/CommerceAdminProductRemovalPage";

export const router = createBrowserRouter([
  {
    path: APP_ROUTES.home,
    element: <AppLayout />,
    children: [
      { index: true, element: <Navigate to={APP_ROUTES.socialFeed} replace /> },
      { path: APP_ROUTES.login.slice(1), element: <LoginPage /> },
      { path: APP_ROUTES.register.slice(1), element: <RegisterPage /> },
      { path: APP_ROUTES.forgotPassword.slice(1), element: <ForgotPasswordPage /> },
      { path: APP_ROUTES.verifyEmail.slice(1), element: <VerifyEmailPage /> },
      { path: APP_ROUTES.sessionExpired.slice(1), element: <SessionExpiredPage /> },
      { path: APP_ROUTES.adminLogin.slice(1), element: <AdminLoginPage /> },
      { path: APP_ROUTES.commerceHome.slice(1), element: <CommerceHomePage /> },
      { path: APP_ROUTES.commerceSearch.slice(1), element: <CommerceSearchPage /> },
      {
        path: APP_ROUTES.commerceCategoryProducts.slice(1),
        element: <CommerceCategoryProductsPage />,
      },
      {
        path: APP_ROUTES.commerceShopProducts.slice(1),
        element: <CommerceShopProductsPage />,
      },
      {
        path: APP_ROUTES.commerceProductDetail.slice(1),
        element: <CommerceProductDetailPage />,
      },
      {
        path: APP_ROUTES.commerceProductReviews.slice(1),
        element: <CommerceProductReviewsPage />,
      },
      {
        element: <AuthGuard />,
        children: [
          { path: APP_ROUTES.account.slice(1), element: <AccountPage /> },
          { path: APP_ROUTES.accountSecurity.slice(1), element: <AccountSecurityPage /> },
          { path: APP_ROUTES.accountPassword.slice(1), element: <AccountPasswordPage /> },
          { path: APP_ROUTES.changePassword.slice(1), element: <ChangePasswordPage /> },
          {
            path: APP_ROUTES.commerceAdminShopModeration.slice(1),
            element: <CommerceAdminShopModerationPage />,
          },
          {
            path: APP_ROUTES.commerceAdminReviewModeration.slice(1),
            element: <CommerceAdminReviewModerationPage />,
          },
          {
            path: APP_ROUTES.commerceAdminProductRemoval.slice(1),
            element: <CommerceAdminProductRemovalPage />,
          },
          { path: APP_ROUTES.socialFeed.slice(1), element: <SocialFeedPage /> },
          { path: APP_ROUTES.socialSavedPosts.slice(1), element: <SocialSavedPostsPage /> },
          { path: APP_ROUTES.socialSearchPosts.slice(1), element: <SocialSearchPostsPage /> },
          { path: APP_ROUTES.socialHashtagPosts.slice(1), element: <SocialHashtagPostsPage /> },
          { path: APP_ROUTES.socialProfile.slice(1), element: <SocialProfilePage /> },
          { path: APP_ROUTES.commerceCart.slice(1), element: <CommerceCartPage /> },
          { path: APP_ROUTES.commerceAddresses.slice(1), element: <CommerceUserAddressesPage /> },
          { path: APP_ROUTES.commerceOrders.slice(1), element: <CommerceOrderListPage /> },
          {
            path: APP_ROUTES.commerceOrderDetail.slice(1),
            element: <CommerceOrderDetailPage />,
          },
          {
            path: APP_ROUTES.commerceShipmentTracking.slice(1),
            element: <CommerceShipmentTrackingPage />,
          },
          {
            path: APP_ROUTES.commerceReviewCreate.slice(1),
            element: <CommerceWriteEditReviewPage />,
          },
          {
            path: APP_ROUTES.commerceReviewEdit.slice(1),
            element: <CommerceWriteEditReviewPage />,
          },
          { path: APP_ROUTES.commerceCheckout.slice(1), element: <CommerceCheckoutPage /> },
          {
            path: APP_ROUTES.commerceCheckoutPaymentResult.slice(1),
            element: <CommerceCheckoutPaymentResultPage />,
          },
          {
            path: APP_ROUTES.commerceCheckoutSuccess.slice(1),
            element: <CommerceCheckoutSuccessPage />,
          },
          {
            path: APP_ROUTES.commerceCreateShop.slice(1),
            element: <CommerceCreateShopPage />,
          },
          {
            path: APP_ROUTES.commerceShopSettings.slice(1),
            element: <CommerceShopSettingsPage />,
          },
          {
            path: APP_ROUTES.commerceSellerProducts.slice(1),
            element: <CommerceSellerProductListPage />,
          },
          {
            path: APP_ROUTES.commerceSellerProductCreate.slice(1),
            element: <CommerceSellerProductFormPage mode="create" />,
          },
          {
            path: APP_ROUTES.commerceSellerProductEdit.slice(1),
            element: <CommerceSellerProductFormPage mode="edit" />,
          },
          {
            path: APP_ROUTES.commerceSellerOrders.slice(1),
            element: <CommerceSellerOrderListPage />,
          },
          {
            path: APP_ROUTES.commerceSellerShipments.slice(1),
            element: <CommerceSellerShipmentListPage />,
          },
          {
            path: APP_ROUTES.commerceSellerShipmentDetail.slice(1),
            element: <CommerceSellerShipmentDetailPage />,
          },
          {
            path: APP_ROUTES.commerceSellerReviews.slice(1),
            element: <CommerceSellerShopReviewsPage />,
          },
        ],
      },
      {
        element: <AdminAuthGuard />,
        children: [{ path: APP_ROUTES.admin.slice(1), element: <AdminPage /> }],
      },
    ],
  },
]);

