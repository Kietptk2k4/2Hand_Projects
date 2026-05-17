import { useEffect, useMemo, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { registerWithEmail } from "../api/authApi";
import { SocialLoginButtons } from "../components/SocialLoginButtons";
import {
  validateConfirmPassword,
  validateEmail,
  validatePassword,
  validateRegisterForm,
} from "../schemas/authSchemas";
import { APP_ROUTES } from "../../../shared/constants/routes";

const ERROR_MESSAGE_BY_CODE = {
  409: "Email da duoc su dung.",
  429: "Ban thao tac qua nhanh, vui long thu lai sau.",
  500: "Co loi he thong. Vui long thu lai sau.",
};

function resolveFieldErrors(errors = []) {
  return errors.reduce((acc, item) => {
    if (item?.field && !acc[item.field]) {
      acc[item.field] = item.reason || "Truong du lieu khong hop le.";
    }
    return acc;
  }, {});
}

export function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    email: "",
    password: "",
    confirm_password: "",
  });
  const [errors, setErrors] = useState({
    email: "",
    password: "",
    confirm_password: "",
  });
  const [globalError, setGlobalError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isSocialRedirecting, setIsSocialRedirecting] = useState(false);
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  const [isConfirmPasswordVisible, setIsConfirmPasswordVisible] = useState(false);
  const redirectTimerRef = useRef(null);

  const validation = useMemo(() => validateRegisterForm(form), [form]);
  const isSubmitDisabled = !validation.isValid || isSubmitting || isSocialRedirecting;

  const onChange = (key) => (event) => {
    const nextValue = event.target.value;
    setForm((prev) => ({ ...prev, [key]: nextValue }));
    setErrors((prev) => ({ ...prev, [key]: "" }));
    setGlobalError("");
  };

  const onBlur = (field) => {
    if (field === "email") {
      setErrors((prev) => ({ ...prev, email: validateEmail(form.email.trim()) }));
      return;
    }
    if (field === "password") {
      setErrors((prev) => ({ ...prev, password: validatePassword(form.password) }));
      return;
    }
    if (field === "confirm_password") {
      setErrors((prev) => ({
        ...prev,
        confirm_password: validateConfirmPassword(form.password, form.confirm_password),
      }));
    }
  };

  const getErrorMessage = (error) => {
    if (error?.code === 400) {
      return error?.message || "Thong tin dang ky khong hop le.";
    }
    return ERROR_MESSAGE_BY_CODE[error?.code] || "Co loi xay ra. Vui long thu lai.";
  };

  const onSubmit = async (event) => {
    event.preventDefault();
    if (isSubmitting) return;

    const normalizedForm = {
      email: form.email.trim().toLowerCase(),
      password: form.password,
      confirm_password: form.confirm_password,
    };
    const nextValidation = validateRegisterForm(normalizedForm);
    setErrors(nextValidation.errors);
    setGlobalError("");
    setSuccessMessage("");

    if (!nextValidation.isValid) return;

    setIsSubmitting(true);
    try {
      const registerData = await registerWithEmail(normalizedForm);
      setSuccessMessage("Dang ky thanh cong. Vui long kiem tra email de xac thuc.");

      const nextRoute =
        registerData?.status === "PENDING_VERIFICATION" ? APP_ROUTES.verifyEmail : APP_ROUTES.login;
      redirectTimerRef.current = window.setTimeout(() => {
        navigate(nextRoute, { replace: true });
      }, 900);
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

  useEffect(() => {
    return () => {
      if (redirectTimerRef.current) {
        window.clearTimeout(redirectTimerRef.current);
      }
    };
  }, []);

  return (
    <section className="relative overflow-hidden rounded-2xl border border-outline-variant/40 bg-white lg:grid lg:min-h-[760px] lg:grid-cols-[1fr_1fr]">
      <div className="relative hidden overflow-hidden lg:flex lg:flex-col lg:justify-between">
        <div className="absolute inset-0 bg-gradient-to-br from-[#dae1ff] to-[#d8e3fb]" />
        <div className="absolute inset-0 bg-[linear-gradient(180deg,rgba(255,255,255,0.2),rgba(17,28,45,0.08))]" />
        <div className="absolute inset-0 opacity-75 mix-blend-multiply [background-image:url('https://lh3.googleusercontent.com/aida-public/AB6AXuAQu1URSNNqU-vw6JEtCouDo_b-CuS0TUX0RTRWZ_AIKUpCjes3cc6YXGJ-nkEklF4Jseykl8W4BKJKUBcvi0urYPd9UmYMvS4X0vm24ILR7w51qRc6sLrbft4qfEKVA9J-T5soMKoYSWfaNxUra_MZX72QBxF0kIvf-xALC3zeznTCop_N81Qf1-DgejNVf-cEbyRY5C9MmZEnoLGu61lRwtj6SmJiR3RbMD3phQ7QOCgbdUzx4mW-IyursqLWBuoNOz0C8hWpkp4')] bg-cover bg-center" />

        <div className="relative z-10 p-10">
          <p className="text-[44px] font-bold leading-none text-primary">2Hands</p>
          <p className="mt-4 max-w-md text-4xl font-semibold leading-tight text-on-surface">
            Ket noi dang tin cay cho cac dich vu hien dai.
          </p>
        </div>

        <div className="relative z-10 p-10">
          <div className="max-w-lg rounded-xl border border-outline-variant/50 bg-white/90 p-6 shadow-sm backdrop-blur-sm">
            <p className="text-sm font-semibold text-on-surface">Cong dong chuyen nghiep</p>
            <p className="mt-3 text-base leading-7 text-on-surface-variant">
              Tham gia mang luoi hang ngan chuyen gia va khach hang uy tin. Xay dung ho so va phat
              trien cong viec cua ban ngay hom nay.
            </p>
          </div>
        </div>
      </div>

      <div className="relative flex items-center justify-center bg-white px-5 py-8 sm:px-8 sm:py-10 lg:px-12">
        <div className="w-full max-w-[420px] space-y-6">
          <div className="text-center lg:hidden">
            <p className="text-3xl font-bold text-primary">2Hands</p>
          </div>

          <header>
            <h1 className="text-5xl font-semibold tracking-tight text-on-surface">Tao tai khoan moi</h1>
            <p className="mt-2 text-base text-on-surface-variant">
              Vui long dien thong tin de bat dau hanh trinh cua ban.
            </p>
          </header>

          {successMessage ? (
            <div className="rounded-lg border border-primary bg-surface-container px-4 py-3 text-sm text-on-surface">
              <p className="font-semibold">{successMessage}</p>
              <p className="text-on-surface-variant">Dang tu dong chuyen huong...</p>
            </div>
          ) : null}

          {globalError ? (
            <div className="rounded-lg border border-error bg-error-container px-4 py-3 text-sm text-on-error-container">
              {globalError}
            </div>
          ) : null}

          <form onSubmit={onSubmit} className="space-y-4" noValidate>
            <div className="space-y-2">
              <label htmlFor="register-email" className="text-sm font-medium text-on-surface">
                Dia chi Email
              </label>
              <input
                id="register-email"
                name="email"
                type="email"
                autoComplete="email"
                value={form.email}
                onChange={onChange("email")}
                onBlur={() => onBlur("email")}
                disabled={isSubmitting}
                placeholder="user@example.com"
                aria-invalid={Boolean(errors.email)}
                aria-describedby={errors.email ? "register-email-error" : undefined}
                className={[
                  "w-full rounded-lg border bg-white px-3 py-3 text-base outline-none transition",
                  errors.email ? "border-error focus:border-error" : "border-outline-variant focus:border-primary",
                  isSubmitting ? "cursor-not-allowed opacity-70" : "",
                ].join(" ")}
              />
              {errors.email ? (
                <p id="register-email-error" className="text-xs text-error">
                  {errors.email}
                </p>
              ) : null}
            </div>

            <div className="space-y-2">
              <label htmlFor="register-password" className="text-sm font-medium text-on-surface">
                Mat khau
              </label>
              <div className="relative">
                <input
                  id="register-password"
                  name="password"
                  type={isPasswordVisible ? "text" : "password"}
                  autoComplete="new-password"
                  value={form.password}
                  onChange={onChange("password")}
                  onBlur={() => onBlur("password")}
                  disabled={isSubmitting}
                  placeholder="Nhap mat khau"
                  aria-invalid={Boolean(errors.password)}
                  aria-describedby={errors.password ? "register-password-error" : undefined}
                  className={[
                    "w-full rounded-lg border bg-white px-3 py-3 pr-12 text-base outline-none transition",
                    errors.password
                      ? "border-error focus:border-error"
                      : "border-outline-variant focus:border-primary",
                    isSubmitting ? "cursor-not-allowed opacity-70" : "",
                  ].join(" ")}
                />
                <button
                  type="button"
                  onClick={() => setIsPasswordVisible((prev) => !prev)}
                  disabled={isSubmitting}
                  className="absolute inset-y-0 right-0 flex w-12 items-center justify-center text-sm text-on-surface-variant disabled:cursor-not-allowed"
                  aria-label={isPasswordVisible ? "An mat khau" : "Hien mat khau"}
                >
                  {isPasswordVisible ? "Hide" : "Show"}
                </button>
              </div>
              {errors.password ? (
                <p id="register-password-error" className="text-xs text-error">
                  {errors.password}
                </p>
              ) : null}
            </div>

            <div className="space-y-2">
              <label htmlFor="register-confirm-password" className="text-sm font-medium text-on-surface">
                Xac nhan mat khau
              </label>
              <div className="relative">
                <input
                  id="register-confirm-password"
                  name="confirm_password"
                  type={isConfirmPasswordVisible ? "text" : "password"}
                  autoComplete="new-password"
                  value={form.confirm_password}
                  onChange={onChange("confirm_password")}
                  onBlur={() => onBlur("confirm_password")}
                  disabled={isSubmitting}
                  placeholder="Nhap lai mat khau"
                  aria-invalid={Boolean(errors.confirm_password)}
                  aria-describedby={errors.confirm_password ? "register-confirm-password-error" : undefined}
                  className={[
                    "w-full rounded-lg border bg-white px-3 py-3 pr-12 text-base outline-none transition",
                    errors.confirm_password
                      ? "border-error focus:border-error"
                      : "border-outline-variant focus:border-primary",
                    isSubmitting ? "cursor-not-allowed opacity-70" : "",
                  ].join(" ")}
                />
                <button
                  type="button"
                  onClick={() => setIsConfirmPasswordVisible((prev) => !prev)}
                  disabled={isSubmitting}
                  className="absolute inset-y-0 right-0 flex w-12 items-center justify-center text-sm text-on-surface-variant disabled:cursor-not-allowed"
                  aria-label={isConfirmPasswordVisible ? "An xac nhan mat khau" : "Hien xac nhan mat khau"}
                >
                  {isConfirmPasswordVisible ? "Hide" : "Show"}
                </button>
              </div>
              {errors.confirm_password ? (
                <p id="register-confirm-password-error" className="text-xs text-error">
                  {errors.confirm_password}
                </p>
              ) : null}
            </div>

            <button
              type="submit"
              disabled={isSubmitDisabled}
              className="mt-2 flex w-full items-center justify-center gap-2 rounded-full bg-primary px-4 py-3 text-sm font-semibold text-white transition hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-75"
            >
              {isSubmitting ? (
                <>
                  <span className="inline-block h-4 w-4 animate-spin rounded-full border-2 border-white/50 border-t-white" />
                  <span>Dang ky...</span>
                </>
              ) : (
                "Dang ky"
              )}
            </button>
          </form>

          <div className="flex items-center gap-3">
            <div className="h-px flex-1 bg-outline-variant/60" />
            <span className="text-sm text-on-surface-variant">Hoac tiep tuc voi</span>
            <div className="h-px flex-1 bg-outline-variant/60" />
          </div>

          <SocialLoginButtons
            disabled={isSubmitting || isSocialRedirecting}
            onRedirectStart={() => setIsSocialRedirecting(true)}
          />

          <p className="pt-2 text-center text-sm text-on-surface-variant">
            Da co tai khoan?{" "}
            <Link to={APP_ROUTES.login} className="font-semibold text-primary hover:underline">
              Dang nhap ngay
            </Link>
          </p>
        </div>

        {isSocialRedirecting ? (
          <div className="absolute inset-0 z-20 flex items-center justify-center bg-white/80 backdrop-blur-[1px]">
            <p className="rounded-lg border border-outline-variant bg-white px-4 py-2 text-sm font-medium text-on-surface">
              Dang chuyen huong den nha cung cap dang nhap...
            </p>
          </div>
        ) : null}
      </div>
    </section>
  );
}

