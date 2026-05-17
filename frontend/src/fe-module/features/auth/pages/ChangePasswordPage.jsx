import { useEffect, useMemo, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { changePassword } from "../api/authApi";
import {
  getPasswordChecklistState,
  validateChangePasswordForm,
  validateConfirmNewPassword,
  validateCurrentPassword,
  validateNewPassword,
} from "../schemas/authSchemas";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { useAuthSession } from "../hooks/useAuthSession";

const ERROR_MESSAGE_BY_CODE = {
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

function PasswordField({
  id,
  name,
  label,
  value,
  onChange,
  onBlur,
  placeholder,
  error,
  visible,
  onToggleVisible,
  disabled,
}) {
  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={id} className="text-sm font-medium text-on-surface">
        {label}
      </label>
      <div className="relative">
        <input
          id={id}
          name={name}
          type={visible ? "text" : "password"}
          value={value}
          onChange={onChange}
          onBlur={onBlur}
          placeholder={placeholder}
          disabled={disabled}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? `${id}-error` : undefined}
          className={[
            "w-full rounded-lg border bg-white px-3 py-3 pr-12 text-base outline-none transition",
            error ? "border-error focus:border-error" : "border-outline-variant focus:border-primary",
            disabled ? "cursor-not-allowed opacity-70" : "",
          ].join(" ")}
        />
        <button
          type="button"
          onClick={onToggleVisible}
          disabled={disabled}
          className="absolute inset-y-0 right-0 flex w-12 items-center justify-center text-sm text-on-surface-variant disabled:cursor-not-allowed"
          aria-label={visible ? `An ${label}` : `Hien ${label}`}
        >
          {visible ? "Hide" : "Show"}
        </button>
      </div>
      {error ? (
        <p id={`${id}-error`} className="text-xs text-error">
          {error}
        </p>
      ) : null}
    </div>
  );
}

export function ChangePasswordPage() {
  const navigate = useNavigate();
  const { accessToken, clearSession } = useAuthSession();
  const [form, setForm] = useState({
    current_password: "",
    new_password: "",
    confirm_new_password: "",
  });
  const [errors, setErrors] = useState({
    current_password: "",
    new_password: "",
    confirm_new_password: "",
  });
  const [globalError, setGlobalError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [visibility, setVisibility] = useState({
    current_password: false,
    new_password: false,
    confirm_new_password: false,
  });
  const redirectTimerRef = useRef(null);

  const validation = useMemo(() => validateChangePasswordForm(form), [form]);
  const checklistState = useMemo(() => getPasswordChecklistState(form.new_password || ""), [form.new_password]);
  const isSubmitDisabled = !validation.isValid || isSubmitting;

  const updateField = (field) => (event) => {
    const nextValue = event.target.value;
    setForm((prev) => ({ ...prev, [field]: nextValue }));
    setErrors((prev) => ({ ...prev, [field]: "" }));
    setGlobalError("");
    setSuccessMessage("");
  };

  const onBlurField = (field) => {
    if (field === "current_password") {
      setErrors((prev) => ({
        ...prev,
        current_password: validateCurrentPassword(form.current_password),
      }));
      return;
    }

    if (field === "new_password") {
      setErrors((prev) => ({
        ...prev,
        new_password: validateNewPassword(form.new_password, form.current_password),
      }));
      return;
    }

    if (field === "confirm_new_password") {
      setErrors((prev) => ({
        ...prev,
        confirm_new_password: validateConfirmNewPassword(form.new_password, form.confirm_new_password),
      }));
    }
  };

  const goToLoginAndClearSession = () => {
    clearSession();
    navigate(APP_ROUTES.login, { replace: true });
  };

  const getErrorMessage = (error) => {
    if (error?.code === 400) {
      return error?.message || "Thong tin khong hop le. Vui long kiem tra lai.";
    }
    if (error?.code === 401) {
      return "Phien dang nhap het han. Vui long dang nhap lai.";
    }
    return ERROR_MESSAGE_BY_CODE[error?.code] || "Co loi xay ra. Vui long thu lai.";
  };

  const onSubmit = async (event) => {
    event.preventDefault();
    if (isSubmitting) return;

    const nextValidation = validateChangePasswordForm(form);
    setErrors(nextValidation.errors);
    setGlobalError("");
    setSuccessMessage("");

    if (!nextValidation.isValid) return;

    setIsSubmitting(true);
    try {
      await changePassword(form, accessToken || localStorage.getItem("twohands_access_token"));
      setSuccessMessage(
        "Doi mat khau thanh cong. Ban can dang nhap lai, va cac phien dang nhap khac se bi thu hoi."
      );
      redirectTimerRef.current = window.setTimeout(() => {
        goToLoginAndClearSession();
      }, 1200);
    } catch (error) {
      if (error?.code === 401) {
        goToLoginAndClearSession();
        return;
      }

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
    <div className="min-h-screen bg-background text-on-background">
      <header className="border-b border-outline-variant/60 bg-surface">
        <div className="mx-auto flex w-full max-w-[1280px] items-center justify-between px-4 py-3 md:px-8">
          <Link to={APP_ROUTES.home} className="text-3xl font-bold text-primary">
            2Hands
          </Link>
          <nav className="hidden items-center gap-6 text-sm font-medium text-on-surface-variant md:flex">
            <span>Services</span>
            <span>How it Works</span>
            <span>About</span>
          </nav>
          <div className="flex items-center gap-4">
            <Link to={APP_ROUTES.login} className="text-sm font-semibold text-primary">
              Sign In
            </Link>
            <Link
              to={APP_ROUTES.register}
              className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-on-primary"
            >
              Register
            </Link>
          </div>
        </div>
      </header>

      <main className="flex items-center justify-center px-4 py-12 md:px-8 md:py-16">
        <section className="w-full max-w-[500px] rounded-xl border border-outline-variant/70 bg-white p-6 shadow-sm">
          <header className="mb-6 text-center">
            <h1 className="text-5xl font-semibold leading-tight text-on-surface">Doi mat khau</h1>
            <p className="mt-2 text-base text-on-surface-variant">
              Chon mot mat khau manh de bao ve tai khoan cua ban.
            </p>
          </header>

          {successMessage ? (
            <div className="mb-4 rounded-lg border border-primary bg-surface-container px-4 py-3 text-sm text-on-surface">
              <p className="font-semibold">{successMessage}</p>
              <p className="text-on-surface-variant">Dang chuyen huong den trang dang nhap...</p>
            </div>
          ) : null}

          {globalError ? (
            <div className="mb-4 rounded-lg border border-error bg-error-container px-4 py-3 text-sm text-on-error-container">
              {globalError}
            </div>
          ) : null}

          <form className="space-y-4" onSubmit={onSubmit} noValidate>
            <PasswordField
              id="current-password"
              name="current_password"
              label="Current Password"
              value={form.current_password}
              onChange={updateField("current_password")}
              onBlur={() => onBlurField("current_password")}
              placeholder="Enter current password"
              error={errors.current_password}
              visible={visibility.current_password}
              onToggleVisible={() =>
                setVisibility((prev) => ({ ...prev, current_password: !prev.current_password }))
              }
              disabled={isSubmitting}
            />

            <div className="space-y-2">
              <PasswordField
                id="new-password"
                name="new_password"
                label="New Password"
                value={form.new_password}
                onChange={updateField("new_password")}
                onBlur={() => onBlurField("new_password")}
                placeholder="Enter new password"
                error={errors.new_password}
                visible={visibility.new_password}
                onToggleVisible={() =>
                  setVisibility((prev) => ({ ...prev, new_password: !prev.new_password }))
                }
                disabled={isSubmitting}
              />

              <div className="rounded-lg bg-surface-container p-3 text-sm">
                <div
                  className={`flex items-center gap-2 ${
                    checklistState.length ? "text-primary" : "text-on-surface-variant"
                  }`}
                >
                  <span>{checklistState.length ? "◉" : "○"}</span>
                  <span>8-32 characters</span>
                </div>
                <div
                  className={`flex items-center gap-2 ${
                    checklistState.uppercase ? "text-primary" : "text-on-surface-variant"
                  }`}
                >
                  <span>{checklistState.uppercase ? "◉" : "○"}</span>
                  <span>1 uppercase letter</span>
                </div>
                <div
                  className={`flex items-center gap-2 ${
                    checklistState.lowercase ? "text-primary" : "text-on-surface-variant"
                  }`}
                >
                  <span>{checklistState.lowercase ? "◉" : "○"}</span>
                  <span>1 lowercase letter</span>
                </div>
                <div
                  className={`flex items-center gap-2 ${
                    checklistState.number ? "text-primary" : "text-on-surface-variant"
                  }`}
                >
                  <span>{checklistState.number ? "◉" : "○"}</span>
                  <span>1 number</span>
                </div>
              </div>
            </div>

            <PasswordField
              id="confirm-password"
              name="confirm_new_password"
              label="Confirm New Password"
              value={form.confirm_new_password}
              onChange={updateField("confirm_new_password")}
              onBlur={() => onBlurField("confirm_new_password")}
              placeholder="Confirm new password"
              error={errors.confirm_new_password}
              visible={visibility.confirm_new_password}
              onToggleVisible={() =>
                setVisibility((prev) => ({
                  ...prev,
                  confirm_new_password: !prev.confirm_new_password,
                }))
              }
              disabled={isSubmitting}
            />

            <button
              type="submit"
              disabled={isSubmitDisabled}
              className="mt-3 flex w-full items-center justify-center gap-2 rounded-lg bg-primary px-4 py-3 text-sm font-semibold text-white transition hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-75"
            >
              {isSubmitting ? (
                <>
                  <span className="inline-block h-4 w-4 animate-spin rounded-full border-2 border-white/50 border-t-white" />
                  <span>Changing Password...</span>
                </>
              ) : (
                "Doi mat khau"
              )}
            </button>
          </form>
        </section>
      </main>

      <footer className="border-t border-outline-variant/60 bg-surface-container">
        <div className="mx-auto flex w-full max-w-[1280px] flex-col items-center justify-between gap-4 px-4 py-8 md:flex-row md:px-8">
          <div className="text-center md:text-left">
            <p className="text-3xl font-bold text-primary">2Hands</p>
            <p className="text-sm text-on-surface-variant">© 2024 2Hands. Professional Service Marketplace.</p>
          </div>
          <nav className="flex flex-wrap justify-center gap-4 text-sm text-on-surface-variant">
            <span>Privacy Policy</span>
            <span>Terms of Service</span>
            <span>Support</span>
            <span>Contact Us</span>
          </nav>
        </div>
      </footer>
    </div>
  );
}
