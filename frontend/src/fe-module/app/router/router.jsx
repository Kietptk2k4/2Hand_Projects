import { createBrowserRouter } from "react-router-dom";
import { APP_ROUTES } from "../../shared/constants/routes";
import { AppLayout } from "./AppLayout";
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
import { AdminPage } from "../../features/auth/pages/AdminPage";
import { SocialFeedPage } from "../../features/social/pages/SocialFeedPage";
import { SocialProfilePage } from "../../features/social/pages/SocialProfilePage";

function HomePage() {
  return (
    <section className="rounded-2xl border border-outline-variant/40 bg-surface-container p-6">
      <h1 className="text-2xl font-semibold text-on-surface">Frontend Module Scaffold Ready</h1>
      <p className="mt-2 text-sm text-on-surface-variant">
        Bat dau implement tung flow auth theo `docs/api-FE_behavior/*`.
      </p>
    </section>
  );
}

export const router = createBrowserRouter([
  {
    path: APP_ROUTES.home,
    element: <AppLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: APP_ROUTES.login.slice(1), element: <LoginPage /> },
      { path: APP_ROUTES.register.slice(1), element: <RegisterPage /> },
      { path: APP_ROUTES.forgotPassword.slice(1), element: <ForgotPasswordPage /> },
      { path: APP_ROUTES.verifyEmail.slice(1), element: <VerifyEmailPage /> },
      { path: APP_ROUTES.sessionExpired.slice(1), element: <SessionExpiredPage /> },
      {
        element: <AuthGuard />,
        children: [
          { path: APP_ROUTES.account.slice(1), element: <AccountPage /> },
          { path: APP_ROUTES.accountSecurity.slice(1), element: <AccountSecurityPage /> },
          { path: APP_ROUTES.accountPassword.slice(1), element: <AccountPasswordPage /> },
          { path: APP_ROUTES.changePassword.slice(1), element: <ChangePasswordPage /> },
          { path: APP_ROUTES.admin.slice(1), element: <AdminPage /> },
          { path: APP_ROUTES.socialFeed.slice(1), element: <SocialFeedPage /> },
          { path: APP_ROUTES.socialProfile.slice(1), element: <SocialProfilePage /> },
        ],
      },
    ],
  },
]);

