import { useEffect, useMemo, useRef, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { resendEmailVerification, verifyEmail } from "../api/authApi";
import { GENERIC_ERROR_RETRY, INVALID_FIELD_MESSAGE } from "../constants/authUiStrings";
import { validateEmail, validateVerifyEmailForm, validateVerifyToken } from "../schemas/authSchemas";
import { APP_ROUTES } from "../../../shared/constants/routes";

const RESEND_COOLDOWN_SECONDS = 90;

const ERROR_MESSAGE_BY_CODE = {
  400: "Mã OTP không hợp lệ hoặc đã hết hạn.",
  429: "Bạn thao tác quá nhanh. Vui lòng thử lại sau.",
  500: GENERIC_ERROR_RETRY,
};

function resolveFieldErrors(errors = []) {
  return errors.reduce((acc, item) => {
    if (item?.field && !acc[item.field]) {
      acc[item.field] = item.reason || INVALID_FIELD_MESSAGE;
    }
    return acc;
  }, {});
}

function normalizeOtpInput(value) {
  return (value || "").replace(/\D/g, "").slice(0, 6);
}

export function VerifyEmailPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const redirectTimerRef = useRef(null);
  const countdownTimerRef = useRef(null);

  const emailFromNavigation = location.state?.email?.trim().toLowerCase() || "";

  const [email, setEmail] = useState(emailFromNavigation);
  const [form, setForm] = useState({ token: "" });
  const [errors, setErrors] = useState({ token: "", email: "" });
  const [globalError, setGlobalError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [resendMessage, setResendMessage] = useState("");
  const [resendError, setResendError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isResending, setIsResending] = useState(false);
  const [resendCountdown, setResendCountdown] = useState(0);

  const needsEmailInput = !emailFromNavigation;
  const emailIsValid = !validateEmail(email);

  const validation = useMemo(() => validateVerifyEmailForm(form), [form]);
  const isSubmitDisabled = !validation.isValid || isSubmitting;

  const isResendDisabled =
    isResending || isSubmitting || resendCountdown > 0 || !emailIsValid;

  useEffect(() => {
    if (emailFromNavigation) {
      setEmail(emailFromNavigation);
    }
  }, [emailFromNavigation]);

  useEffect(() => {
    if (resendCountdown <= 0) return undefined;

    countdownTimerRef.current = window.setInterval(() => {
      setResendCountdown((prev) => {
        if (prev <= 1) {
          if (countdownTimerRef.current) {
            window.clearInterval(countdownTimerRef.current);
            countdownTimerRef.current = null;
          }
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => {
      if (countdownTimerRef.current) {
        window.clearInterval(countdownTimerRef.current);
        countdownTimerRef.current = null;
      }
    };
  }, [resendCountdown]);

  const startResendCountdown = () => {
    setResendCountdown(RESEND_COOLDOWN_SECONDS);
  };

  const onChangeToken = (event) => {
    setForm({ token: normalizeOtpInput(event.target.value) });
    setErrors((prev) => ({ ...prev, token: "" }));
    setGlobalError("");
    setSuccessMessage("");
  };

  const onBlurToken = () => {
    setErrors((prev) => ({ ...prev, token: validateVerifyToken(form.token) }));
  };

  const onChangeEmail = (event) => {
    setEmail(event.target.value.trim().toLowerCase());
    setErrors((prev) => ({ ...prev, email: "" }));
    setResendError("");
    setResendMessage("");
  };

  const onBlurEmail = () => {
    setErrors((prev) => ({ ...prev, email: validateEmail(email) }));
  };

  const onSubmit = async (event) => {
    event.preventDefault();
    if (isSubmitting) return;

    const normalizedToken = form.token.trim();
    const nextValidation = validateVerifyEmailForm({ token: normalizedToken });
    setErrors((prev) => ({ ...prev, token: nextValidation.errors.token }));
    setGlobalError("");
    setSuccessMessage("");
    if (!nextValidation.isValid) return;

    setIsSubmitting(true);
    try {
      await verifyEmail({ token: normalizedToken });
      setSuccessMessage("Xác thực email thành công. Đang chuyển hướng đến đăng nhập...");
      redirectTimerRef.current = window.setTimeout(() => {
        navigate(APP_ROUTES.login, { replace: true });
      }, 1000);
    } catch (error) {
      const serverFieldErrors = resolveFieldErrors(error?.errors);
      if (error?.code === 400 && Object.keys(serverFieldErrors).length > 0) {
        setErrors((prev) => ({ ...prev, ...serverFieldErrors }));
      }
      setGlobalError(ERROR_MESSAGE_BY_CODE[error?.code] || error?.message || GENERIC_ERROR_RETRY);
    } finally {
      setIsSubmitting(false);
    }
  };

  const onResend = async () => {
    if (isResendDisabled) return;

    const emailError = validateEmail(email);
    setErrors((prev) => ({ ...prev, email: emailError }));
    setResendError("");
    setResendMessage("");
    if (emailError) return;

    setIsResending(true);
    try {
      await resendEmailVerification({ email });
      setResendMessage("Đã gửi lại mã OTP. Vui lòng kiểm tra hộp thư (và thư mục spam).");
      startResendCountdown();
    } catch (error) {
      const serverFieldErrors = resolveFieldErrors(error?.errors);
      if (error?.code === 400 && serverFieldErrors.email) {
        setErrors((prev) => ({ ...prev, email: serverFieldErrors.email }));
      }
      setResendError(ERROR_MESSAGE_BY_CODE[error?.code] || error?.message || GENERIC_ERROR_RETRY);
    } finally {
      setIsResending(false);
    }
  };

  useEffect(() => {
    return () => {
      if (redirectTimerRef.current) {
        window.clearTimeout(redirectTimerRef.current);
      }
      if (countdownTimerRef.current) {
        window.clearInterval(countdownTimerRef.current);
      }
    };
  }, []);

  return (
    <div className="flex min-h-screen items-center justify-center bg-background px-4 py-8 sm:px-6">
      <section className="w-full max-w-[560px] overflow-hidden rounded-xl border border-outline-variant bg-white shadow-sm">
        <div className="relative h-[220px] overflow-hidden border-b border-outline-variant bg-surface-container-low">
          <img
            src="https://lh3.googleusercontent.com/aida-public/AB6AXuADCx98402-Hzrcxy8_R_6M18Ja5N45hUQfnlllE8Ttu-QG74PQTz-xZaO5cV-apL4KhRqyYMMGX2y6JjmXFLkshaH1PEOcZx7R4W6Pdst1r3BDt5V3U3npgwrW-1hIBQ7C3IhnvU10D0ymsXtmwffV_xItu9fxB4xzxKdd03ypelzZTlY48PmS8DMx6Txa_d0UJlV_rtwgERy2nFEUpRuE18VvGX2nTGQk7LkfqCviNtp9M6yirLj9QeF-SWMymjyE5YlmrbYu1vE"
            alt="Minh họa xác thực email"
            className="h-full w-full object-cover opacity-90 mix-blend-multiply"
          />
          <div className="absolute inset-0 bg-gradient-to-t from-white/60 to-transparent" />
          <div className="absolute -bottom-8 left-1/2 flex h-16 w-16 -translate-x-1/2 items-center justify-center rounded-full border border-outline-variant bg-white shadow-sm">
            <span className="text-3xl text-primary">✉</span>
          </div>
        </div>

        <div className="px-6 pb-6 pt-12 text-center sm:px-8">
          <h1 className="text-5xl font-semibold text-on-surface">Xác thực email</h1>
          <p className="mx-auto mt-3 max-w-[420px] text-base leading-7 text-on-surface-variant">
            Kiểm tra hộp thư của bạn. Chúng tôi đã gửi mã OTP 6 chữ số — nhập mã bên dưới để xác nhận tài
            khoản.
          </p>

          {email && !needsEmailInput ? (
            <p className="mx-auto mt-2 max-w-[420px] text-sm text-on-surface-variant">
              Mã được gửi tới: <span className="font-medium text-on-surface">{email}</span>
            </p>
          ) : null}

          {globalError ? (
            <div className="mt-6 flex items-center gap-2 rounded border border-error bg-error-container px-3 py-3 text-left text-sm text-on-error-container">
              <span className="text-error" aria-hidden="true">
                ⓘ
              </span>
              <span>{globalError}</span>
            </div>
          ) : null}

          {successMessage ? (
            <div className="mt-6 rounded border border-primary bg-surface-container px-3 py-3 text-left text-sm text-on-surface">
              {successMessage}
            </div>
          ) : null}

          {resendError ? (
            <div className="mt-6 flex items-center gap-2 rounded border border-error bg-error-container px-3 py-3 text-left text-sm text-on-error-container">
              <span className="text-error" aria-hidden="true">
                ⓘ
              </span>
              <span>{resendError}</span>
            </div>
          ) : null}

          {resendMessage ? (
            <div className="mt-6 rounded border border-primary bg-surface-container px-3 py-3 text-left text-sm text-on-surface">
              {resendMessage}
            </div>
          ) : null}

          <form className="mx-auto mt-6 w-full max-w-[360px] text-left" onSubmit={onSubmit} noValidate>
            {needsEmailInput ? (
              <div className="mb-4">
                <label htmlFor="verify-email" className="text-xs font-semibold text-on-surface">
                  Email đăng ký
                </label>
                <input
                  id="verify-email"
                  name="email"
                  type="email"
                  autoComplete="email"
                  value={email}
                  onChange={onChangeEmail}
                  onBlur={onBlurEmail}
                  disabled={isSubmitting || isResending}
                  placeholder="user@example.com"
                  aria-invalid={Boolean(errors.email)}
                  aria-describedby={errors.email ? "verify-email-error" : undefined}
                  className={[
                    "mt-2 w-full rounded border bg-white px-3 py-3 text-base outline-none transition",
                    errors.email
                      ? "border-error focus:border-error"
                      : "border-outline-variant focus:border-primary",
                    isSubmitting || isResending ? "cursor-not-allowed opacity-70" : "",
                  ].join(" ")}
                />
                {errors.email ? (
                  <p id="verify-email-error" className="mt-1 text-xs text-error">
                    {errors.email}
                  </p>
                ) : null}
              </div>
            ) : null}

            <label htmlFor="verify-token" className="text-xs font-semibold text-on-surface">
              Mã OTP 6 chữ số
            </label>
            <input
              id="verify-token"
              name="token"
              autoComplete="one-time-code"
              type="text"
              inputMode="numeric"
              pattern="[0-9]{6}"
              maxLength={6}
              value={form.token}
              onChange={onChangeToken}
              onBlur={onBlurToken}
              disabled={isSubmitting}
              placeholder="000000"
              aria-invalid={Boolean(errors.token)}
              aria-describedby={errors.token ? "verify-token-error" : undefined}
              className={[
                "mt-2 w-full rounded border bg-white px-3 py-3 text-center text-lg tracking-[0.35em] outline-none transition",
                errors.token ? "border-error focus:border-error" : "border-outline-variant focus:border-primary",
                isSubmitting ? "cursor-not-allowed opacity-70" : "",
              ].join(" ")}
            />
            {errors.token ? (
              <p id="verify-token-error" className="mt-1 text-xs text-error">
                {errors.token}
              </p>
            ) : null}

            <button
              type="submit"
              disabled={isSubmitDisabled}
              className="mt-4 w-full rounded bg-primary px-4 py-3 text-sm font-semibold text-white transition hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-75"
            >
              {isSubmitting ? "Đang xác thực..." : "Xác thực email"}
            </button>
          </form>

          <div className="mx-auto mt-8 w-full max-w-[420px] border-t border-outline-variant pt-4 text-sm text-on-surface-variant">
            <p>Chưa nhận được email?</p>
            <div className="mt-2 flex flex-wrap items-center justify-center gap-4">
              <button
                type="button"
                onClick={onResend}
                disabled={isResendDisabled}
                className="font-medium text-primary transition hover:underline disabled:cursor-not-allowed disabled:text-primary/50 disabled:no-underline"
              >
                {isResending
                  ? "Đang gửi lại..."
                  : resendCountdown > 0
                    ? `Gửi lại mã (${resendCountdown}s)`
                    : "Gửi lại mã"}
              </button>
              <span className="text-outline-variant">|</span>
              <Link to={APP_ROUTES.home} className="font-medium text-primary hover:underline">
                Liên hệ hỗ trợ
              </Link>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
