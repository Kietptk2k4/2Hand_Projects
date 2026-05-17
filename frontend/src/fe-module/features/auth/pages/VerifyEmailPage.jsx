import { useEffect, useMemo, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { verifyEmail } from "../api/authApi";
import { validateVerifyEmailForm, validateVerifyToken } from "../schemas/authSchemas";
import { APP_ROUTES } from "../../../shared/constants/routes";

const ERROR_MESSAGE_BY_CODE = {
  400: "Token khong hop le hoac da het han.",
  500: "Co loi xay ra. Vui long thu lai.",
};

function resolveFieldErrors(errors = []) {
  return errors.reduce((acc, item) => {
    if (item?.field && !acc[item.field]) {
      acc[item.field] = item.reason || "Truong du lieu khong hop le.";
    }
    return acc;
  }, {});
}

export function VerifyEmailPage() {
  const navigate = useNavigate();
  const redirectTimerRef = useRef(null);
  const [form, setForm] = useState({ token: "" });
  const [errors, setErrors] = useState({ token: "" });
  const [globalError, setGlobalError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const validation = useMemo(() => validateVerifyEmailForm(form), [form]);
  const isSubmitDisabled = !validation.isValid || isSubmitting;

  const onChangeToken = (event) => {
    setForm({ token: event.target.value });
    setErrors({ token: "" });
    setGlobalError("");
    setSuccessMessage("");
  };

  const onBlurToken = () => {
    setErrors({ token: validateVerifyToken(form.token) });
  };

  const onSubmit = async (event) => {
    event.preventDefault();
    if (isSubmitting) return;

    const normalizedToken = form.token.trim();
    const nextValidation = validateVerifyEmailForm({ token: normalizedToken });
    setErrors(nextValidation.errors);
    setGlobalError("");
    setSuccessMessage("");
    if (!nextValidation.isValid) return;

    setIsSubmitting(true);
    try {
      // Payload key must be exactly `token` per API contract.
      await verifyEmail({ token: normalizedToken });
      setSuccessMessage("Xac thuc email thanh cong. Dang chuyen huong den dang nhap...");
      redirectTimerRef.current = window.setTimeout(() => {
        navigate(APP_ROUTES.login, { replace: true });
      }, 1000);
    } catch (error) {
      const serverFieldErrors = resolveFieldErrors(error?.errors);
      if (error?.code === 400 && Object.keys(serverFieldErrors).length > 0) {
        setErrors((prev) => ({ ...prev, ...serverFieldErrors }));
      }
      setGlobalError(ERROR_MESSAGE_BY_CODE[error?.code] || "Co loi xay ra. Vui long thu lai.");
    } finally {
      setIsSubmitting(false);
    }
  };

  useEffect(() => {
    return () => {
      if (redirectTimerRef.current) {
        window.clearTimeout(redirectTimerRef.current);
      }
    };
  }, []);

  return (
    <div className="flex min-h-screen items-center justify-center bg-background px-4 py-8 sm:px-6">
      <section className="w-full max-w-[560px] overflow-hidden rounded-xl border border-outline-variant bg-white shadow-sm">
        <div className="relative h-[220px] overflow-hidden border-b border-outline-variant bg-surface-container-low">
          <img
            src="https://lh3.googleusercontent.com/aida-public/AB6AXuADCx98402-Hzrcxy8_R_6M18Ja5N45hUQfnlllE8Ttu-QG74PQTz-xZaO5cV-apL4KhRqyYMMGX2y6JjmXFLkshaH1PEOcZx7R4W6Pdst1r3BDt5V3U3npgwrW-1hIBQ7C3IhnvU10D0ymsXtmwffV_xItu9fxB4xzxKdd03ypelzZTlY48PmS8DMx6Txa_d0UJlV_rtwgERy2nFEUpRuE18VvGX2nTGQk7LkfqCviNtp9M6yirLj9QeF-SWMymjyE5YlmrbYu1vE"
            alt="Mail verification illustration"
            className="h-full w-full object-cover opacity-90 mix-blend-multiply"
          />
          <div className="absolute inset-0 bg-gradient-to-t from-white/60 to-transparent" />
          <div className="absolute -bottom-8 left-1/2 flex h-16 w-16 -translate-x-1/2 items-center justify-center rounded-full border border-outline-variant bg-white shadow-sm">
            <span className="text-3xl text-primary">✉</span>
          </div>
        </div>

        <div className="px-6 pb-6 pt-12 text-center sm:px-8">
          <h1 className="text-5xl font-semibold text-on-surface">Verify your email</h1>
          <p className="mx-auto mt-3 max-w-[420px] text-base leading-7 text-on-surface-variant">
            Check your inbox. We&apos;ve sent a 6-digit code to your email. Please enter it below to confirm
            your account.
          </p>

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

          <form className="mx-auto mt-6 w-full max-w-[360px] text-left" onSubmit={onSubmit} noValidate>
            <label htmlFor="verify-token" className="text-xs font-semibold text-on-surface">
              6-Digit Code
            </label>
            <input
              id="verify-token"
              name="token"
              autoComplete="one-time-code"
              type="text"
              value={form.token}
              onChange={onChangeToken}
              onBlur={onBlurToken}
              disabled={isSubmitting}
              placeholder="Enter code"
              aria-invalid={Boolean(errors.token)}
              aria-describedby={errors.token ? "verify-token-error" : undefined}
              className={[
                "mt-2 w-full rounded border bg-white px-3 py-3 text-base outline-none transition",
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
              {isSubmitting ? "Verifying..." : "Verify Email"}
            </button>
          </form>

          <div className="mx-auto mt-8 w-full max-w-[420px] border-t border-outline-variant pt-4 text-sm text-on-surface-variant">
            <p>Didn&apos;t receive the email?</p>
            <div className="mt-2 flex items-center justify-center gap-4">
              <button type="button" disabled className="font-medium text-primary/60">
                Resend Code
              </button>
              <span className="text-outline-variant">|</span>
              <Link to={APP_ROUTES.home} className="font-medium text-primary hover:underline">
                Contact Support
              </Link>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
