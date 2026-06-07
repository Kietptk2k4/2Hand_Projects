import { useEffect, useMemo, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { AdminShieldIcon, ArrowBackIcon } from "../components/AdminAuthIcons.jsx";
import {
  ADMIN_PORTAL_SUBTITLE,
  ADMIN_PORTAL_TITLE,
} from "../constants/adminAuthUiStrings";
import { HIDE_PASSWORD, INVALID_FIELD_MESSAGE, SHOW_PASSWORD } from "../constants/authUiStrings";
import { useAdminLogin } from "../hooks/useAdminLogin.js";
import { useAuthSession } from "../hooks/useAuthSession.jsx";
import { validateEmail, validateLoginForm } from "../schemas/authSchemas";
import { hasAdminPortalAccess } from "../utils/adminSession";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { AuthAlert } from "../../../shared/ui/auth/authUi.jsx";

const REMEMBER_EMAIL_KEY = "twohands_admin_remember_email";

function resolveFieldErrors(errors = []) {
  return errors.reduce((acc, item) => {
    if (item?.field && !acc[item.field]) {
      acc[item.field] = item.reason || INVALID_FIELD_MESSAGE;
    }
    return acc;
  }, {});
}

function isSafeAdminRedirectUrl(value) {
  return (
    typeof value === "string" &&
    value.startsWith("/admin") &&
    !value.startsWith("//") &&
    value !== APP_ROUTES.adminLogin
  );
}

export function AdminLoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated, user, isAdminSession } = useAuthSession();
  const [form, setForm] = useState({
    email: localStorage.getItem(REMEMBER_EMAIL_KEY) || "",
    password: "",
  });
  const [errors, setErrors] = useState({ email: "", password: "" });
  const [globalError, setGlobalError] = useState("");
  const [logoutInfo, setLogoutInfo] = useState("");
  const [rememberMe, setRememberMe] = useState(Boolean(localStorage.getItem(REMEMBER_EMAIL_KEY)));
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);

  const redirectUrl = useMemo(() => {
    const params = new URLSearchParams(location.search);
    const value = params.get("redirectUrl");
    return isSafeAdminRedirectUrl(value) ? value : APP_ROUTES.admin;
  }, [location.search]);

  const { performAdminLogin, isSubmitting } = useAdminLogin({ redirectUrl });

  useEffect(() => {
    if (location.state?.logoutMessage) {
      setLogoutInfo(location.state.logoutMessage);
      navigate(`${location.pathname}${location.search}`, { replace: true, state: {} });
    }
  }, [location.pathname, location.search, location.state, navigate]);

  useEffect(() => {
    if (
      isAuthenticated &&
      (isAdminSession || hasAdminPortalAccess({ roles: user?.roles, permissions: user?.permissions }))
    ) {
      navigate(redirectUrl, { replace: true });
    }
  }, [isAuthenticated, isAdminSession, navigate, redirectUrl, user?.permissions, user?.roles]);

  const onChange = (key) => (event) => {
    const nextValue = event.target.value;
    setForm((prev) => ({ ...prev, [key]: nextValue }));
    setErrors((prev) => ({ ...prev, [key]: "" }));
    setGlobalError("");
  };

  const onBlurEmail = () => {
    const emailError = validateEmail(form.email.trim());
    setErrors((prev) => ({ ...prev, email: emailError }));
  };

  const onSubmit = async (event) => {
    event.preventDefault();
    if (isSubmitting) return;

    const normalizedForm = {
      email: form.email.trim().toLowerCase(),
      password: form.password,
    };
    const validationResult = validateLoginForm(normalizedForm);
    setErrors(validationResult.errors);
    setGlobalError("");

    if (!validationResult.isValid) return;

    if (rememberMe) {
      localStorage.setItem(REMEMBER_EMAIL_KEY, normalizedForm.email);
    } else {
      localStorage.removeItem(REMEMBER_EMAIL_KEY);
    }

    const result = await performAdminLogin(normalizedForm);
    if (!result.success) {
      const serverFieldErrors = resolveFieldErrors(result.error?.errors);
      if (result.error?.code === 400 && Object.keys(serverFieldErrors).length > 0) {
        setErrors((prev) => ({ ...prev, ...serverFieldErrors }));
      }
      setGlobalError(result.message);
    }
  };

  return (
    <section className="flex min-h-[calc(100vh-8rem)] flex-col justify-center bg-surface-container-low py-12">
      <div className="mx-auto w-full max-w-md px-4 sm:px-6">
        <div className="mb-6 flex justify-center">
          <AdminShieldIcon />
        </div>
        <h1 className="text-center text-2xl font-semibold tracking-tight text-on-surface">{ADMIN_PORTAL_TITLE}</h1>
        <p className="mt-2 text-center text-sm text-on-surface-variant">{ADMIN_PORTAL_SUBTITLE}</p>

        <div className="mt-8 rounded-lg border border-outline-variant bg-surface-container-lowest px-4 py-8 shadow-[0_4px_6px_-1px_rgba(0,0,0,0.1)] sm:px-10">
          {logoutInfo ? (
            <div className="mb-6">
              <AuthAlert variant="success" message={logoutInfo} onDismiss={() => setLogoutInfo("")} />
            </div>
          ) : null}

          {globalError ? (
            <div className="mb-6">
              <AuthAlert variant="error" title="Lỗi đăng nhập" message={globalError} />
            </div>
          ) : null}

          <form onSubmit={onSubmit} className="space-y-6" noValidate>
            <div className="space-y-2">
              <label htmlFor="admin-login-email" className="text-sm font-medium text-on-surface">
                Email address
              </label>
              <input
                id="admin-login-email"
                name="email"
                type="email"
                autoComplete="email"
                value={form.email}
                onChange={onChange("email")}
                onBlur={onBlurEmail}
                aria-invalid={Boolean(errors.email)}
                aria-describedby={errors.email ? "admin-login-email-error" : undefined}
                className={[
                  "w-full rounded-lg border bg-surface-container-lowest px-3 py-2 text-sm outline-none transition focus:ring-2 focus:ring-primary",
                  errors.email ? "border-error focus:border-error" : "border-outline-variant focus:border-primary",
                ].join(" ")}
              />
              {errors.email ? (
                <p id="admin-login-email-error" className="text-xs text-error">
                  {errors.email}
                </p>
              ) : null}
            </div>

            <div className="space-y-2">
              <label htmlFor="admin-login-password" className="text-sm font-medium text-on-surface">
                Password
              </label>
              <div className="relative">
                <input
                  id="admin-login-password"
                  name="password"
                  type={isPasswordVisible ? "text" : "password"}
                  autoComplete="current-password"
                  value={form.password}
                  onChange={onChange("password")}
                  aria-invalid={Boolean(errors.password)}
                  aria-describedby={errors.password ? "admin-login-password-error" : undefined}
                  className={[
                    "w-full rounded-lg border bg-surface-container-lowest px-3 py-2 pr-12 text-sm outline-none transition focus:ring-2 focus:ring-primary",
                    errors.password
                      ? "border-error focus:border-error"
                      : "border-outline-variant focus:border-primary",
                  ].join(" ")}
                />
                <button
                  type="button"
                  onClick={() => setIsPasswordVisible((prev) => !prev)}
                  className="absolute inset-y-0 right-0 flex w-12 items-center justify-center text-on-surface-variant"
                  aria-label={isPasswordVisible ? "Ẩn mật khẩu" : "Hiện mật khẩu"}
                >
                  {isPasswordVisible ? HIDE_PASSWORD : SHOW_PASSWORD}
                </button>
              </div>
              {errors.password ? (
                <p id="admin-login-password-error" className="text-xs text-error">
                  {errors.password}
                </p>
              ) : null}
            </div>

            <div className="flex items-center justify-between gap-3">
              <label className="flex items-center gap-2 text-sm text-on-surface-variant">
                <input
                  type="checkbox"
                  checked={rememberMe}
                  onChange={(event) => setRememberMe(event.target.checked)}
                  className="h-4 w-4 rounded border-outline-variant text-primary focus:ring-primary"
                />
                Remember me
              </label>
              <Link to={APP_ROUTES.forgotPassword} className="text-sm font-medium text-primary hover:underline">
                Forgot your password?
              </Link>
            </div>

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full rounded-lg bg-primary-container px-4 py-2.5 text-sm font-medium text-on-primary shadow-sm transition hover:bg-primary disabled:cursor-not-allowed disabled:opacity-70"
            >
              {isSubmitting ? "Đang đăng nhập..." : "Login"}
            </button>
          </form>
        </div>

        <div className="mt-6 text-center">
          <Link
            to={APP_ROUTES.socialFeed}
            className="inline-flex items-center justify-center gap-2 text-xs font-semibold text-on-surface-variant transition-colors hover:text-primary"
          >
            <ArrowBackIcon />
            Return to marketplace
          </Link>
        </div>
      </div>
    </section>
  );
}
