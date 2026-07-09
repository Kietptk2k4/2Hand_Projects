import { useEffect, useMemo, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { loginWithEmail } from "../api/authApi";
import { SocialLoginButtons } from "../components/SocialLoginButtons";
import { validateEmail, validateLoginForm } from "../schemas/authSchemas";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { useAuthSession } from "../hooks/useAuthSession";
import { USER_SESSION_KIND } from "../utils/adminSession";
import { GENERIC_ERROR_RETRY, HIDE_PASSWORD, INVALID_FIELD_MESSAGE, SHOW_PASSWORD } from "../constants/authUiStrings";

const ERROR_MESSAGE_BY_CODE = {
  401: "Email hoặc mật khẩu không chính xác.",
  403: "Tài khoản hiện không khả dụng.",
  429: "Bạn đang thử quá nhiều lần. Vui lòng đợi ít phút rồi thử lại.",
  500: "Hệ thống đang bận. Vui lòng thử lại sau.",
};

function resolveFieldErrors(errors = []) {
  return errors.reduce((acc, item) => {
    if (item?.field && !acc[item.field]) {
      acc[item.field] = item.reason || INVALID_FIELD_MESSAGE;
    }
    return acc;
  }, {});
}

function isSafeRedirectUrl(value) {
  return typeof value === "string" && value.startsWith("/") && !value.startsWith("//");
}

export function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { setSession, hideSessionExpired } = useAuthSession();
  const [form, setForm] = useState({ email: "", password: "" });
  const [errors, setErrors] = useState({ email: "", password: "" });
  const [globalError, setGlobalError] = useState("");
  const [logoutInfo, setLogoutInfo] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isSocialRedirecting, setIsSocialRedirecting] = useState(false);
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);

  const redirectUrl = useMemo(() => {
    const params = new URLSearchParams(location.search);
    const value = params.get("redirectUrl");
    return isSafeRedirectUrl(value) ? value : APP_ROUTES.socialFeed;
  }, [location.search]);

  useEffect(() => {
    hideSessionExpired();
  }, [hideSessionExpired]);

  useEffect(() => {
    if (location.state?.logoutMessage) {
      setLogoutInfo(location.state.logoutMessage);
      navigate(`${location.pathname}${location.search}`, { replace: true, state: {} });
    }
  }, [location.pathname, location.search, location.state, navigate]);

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

  const getErrorMessage = (error) => {
    const code = error?.code || 500;
    if (code === 400) {
      return error?.message || "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại.";
    }
    return ERROR_MESSAGE_BY_CODE[code] || GENERIC_ERROR_RETRY;
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

    setIsSubmitting(true);
    try {
      const loginData = await loginWithEmail(normalizedForm);
      setSession({
        accessToken: loginData.access_token,
        refreshToken: loginData.refresh_token,
        user: loginData.user,
        sessionKind: USER_SESSION_KIND,
      });
      hideSessionExpired();

      const isPendingVerification = loginData?.user?.status === "PENDING_VERIFICATION";
      navigate(isPendingVerification ? APP_ROUTES.verifyEmail : redirectUrl, {
        replace: true,
        state: isPendingVerification ? { email: normalizedForm.email } : undefined,
      });
    } catch (error) {
      const serverFieldErrors = resolveFieldErrors(error?.errors);
      if (error?.code === 400 && Object.keys(serverFieldErrors).length > 0) {
        setErrors((prev) => ({ ...prev, ...serverFieldErrors }));
      }
      setGlobalError(getErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="relative overflow-hidden rounded-2xl border border-outline-variant/40 bg-white lg:grid lg:min-h-[720px] lg:grid-cols-[1.05fr_1fr]">
      <div className="relative hidden overflow-hidden lg:block">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_30%_20%,rgba(96,99,238,0.38),rgba(17,28,45,0.98)_70%)]" />
        <div className="absolute inset-x-0 bottom-0 h-48 bg-gradient-to-t from-[#1f2f49] to-transparent" />
        <div className="relative z-10 flex h-full flex-col justify-end p-10 text-white">
          <h2 className="max-w-[16ch] text-[52px] font-semibold leading-[1.05]">
            Dịch vụ chuyên nghiệp, đáng tin cậy.
          </h2>
          <p className="mt-5 max-w-md text-base text-white/85">
            Kết nối với hàng ngàn chuyên gia hàng đầu cho mọi nhu cầu dịch vụ của bạn.
          </p>
        </div>
      </div>

      <div className="relative flex items-center justify-center px-5 py-8 sm:px-8 sm:py-10 lg:px-12">
        <div className="w-full max-w-[440px]">
          <header className="space-y-2">
            <h1 className="text-[44px] font-semibold leading-none text-primary">2Hands</h1>
            <p className="text-sm text-on-surface-variant">Chào mừng bạn quay trở lại. Vui lòng đăng nhập.</p>
          </header>

          {logoutInfo ? (
            <div className="mt-7 rounded-lg border border-primary/30 bg-primary/5 px-4 py-3 text-sm text-on-surface">
              <p>{logoutInfo}</p>
            </div>
          ) : null}

          {globalError ? (
            <div className="mt-7 rounded-lg border border-error bg-error-container px-4 py-3 text-sm text-on-error-container">
              <p className="font-semibold">Lỗi đăng nhập</p>
              <p>{globalError}</p>
            </div>
          ) : null}

          <form onSubmit={onSubmit} className="mt-6 space-y-4" noValidate>
            <div className="space-y-2">
              <label htmlFor="login-email" className="text-sm font-medium text-on-surface">
                Email
              </label>
              <input
                id="login-email"
                name="email"
                type="email"
                autoComplete="email"
                value={form.email}
                onChange={onChange("email")}
                onBlur={onBlurEmail}
                placeholder="user@example.com"
                aria-invalid={Boolean(errors.email)}
                aria-describedby={errors.email ? "login-email-error" : undefined}
                className={[
                  "w-full rounded-lg border bg-white px-3 py-3 text-base outline-none transition",
                  errors.email ? "border-error focus:border-error" : "border-outline-variant focus:border-primary",
                ].join(" ")}
              />
              {errors.email ? (
                <p id="login-email-error" className="text-xs text-error">
                  {errors.email}
                </p>
              ) : null}
            </div>

            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <label htmlFor="login-password" className="text-sm font-medium text-on-surface">
                  Mật khẩu
                </label>
                <Link
                  to={APP_ROUTES.forgotPassword}
                  className="text-sm font-medium text-primary hover:underline"
                >
                  Quên mật khẩu?
                </Link>
              </div>

              <div className="relative">
                <input
                  id="login-password"
                  name="password"
                  type={isPasswordVisible ? "text" : "password"}
                  autoComplete="current-password"
                  value={form.password}
                  onChange={onChange("password")}
                  placeholder="Nhập mật khẩu"
                  aria-invalid={Boolean(errors.password)}
                  aria-describedby={errors.password ? "login-password-error" : undefined}
                  className={[
                    "w-full rounded-lg border bg-white px-3 py-3 pr-12 text-base outline-none transition",
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
                <p id="login-password-error" className="text-xs text-error">
                  {errors.password}
                </p>
              ) : null}
            </div>

            <button
              type="submit"
              disabled={isSubmitting || isSocialRedirecting}
              className="mt-2 w-full rounded-lg bg-primary px-4 py-3 text-sm font-semibold text-white transition hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-70"
            >
              {isSubmitting ? "Đang đăng nhập..." : "Đăng nhập"}
            </button>
          </form>

          <div className="my-6 flex items-center gap-4 text-xs font-semibold uppercase tracking-[0.08em] text-on-surface-variant/80">
            <div className="h-px flex-1 bg-outline-variant/80" />
            <span>Hoặc tiếp tục với</span>
            <div className="h-px flex-1 bg-outline-variant/80" />
          </div>

          <SocialLoginButtons
            disabled={isSubmitting || isSocialRedirecting}
            onRedirectStart={() => setIsSocialRedirecting(true)}
          />

          <p className="mt-8 text-center text-sm text-on-surface-variant">
            Chưa có tài khoản?{" "}
            <Link to={APP_ROUTES.register} className="font-semibold text-primary hover:underline">
              Đăng ký ngay
            </Link>
          </p>
        </div>

        {isSocialRedirecting ? (
          <div className="absolute inset-0 z-20 flex items-center justify-center bg-white/80 backdrop-blur-[1px]">
            <p className="rounded-lg border border-outline-variant bg-white px-4 py-2 text-sm font-medium text-on-surface">
              Đang chuyển hướng đến nhà cung cấp đăng nhập...
            </p>
          </div>
        ) : null}
      </div>
    </section>
  );
}
