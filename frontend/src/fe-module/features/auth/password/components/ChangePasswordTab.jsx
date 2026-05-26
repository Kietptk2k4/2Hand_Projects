import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { changePassword } from "../../api/authApi";
import {
  getPasswordChecklistState,
  validateChangePasswordForm,
  validateConfirmNewPassword,
  validateCurrentPassword,
  validateNewPassword,
} from "../../schemas/authSchemas";
import { APP_ROUTES } from "../../../../shared/constants/routes";
import { useAuthSession } from "../../hooks/useAuthSession.jsx";
import {
  AccountCard,
  AuthAlert,
  MaterialIcon,
  PasswordChecklist,
  PasswordField,
} from "../../../../shared/ui/auth/authUi.jsx";

const ERROR_MESSAGE_BY_CODE = {
  500: "Co loi xay ra. Vui long thu lai.",
};

function resolveFieldErrors(errors = []) {
  return errors.reduce((acc, item) => {
    if (!item?.field) return acc;
    const field = item.field === "password" ? "current_password" : item.field;
    if (!acc[field]) {
      acc[field] = item.reason || "Truong du lieu khong hop le.";
    }
    return acc;
  }, {});
}

export function ChangePasswordTab() {
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
    <AccountCard className="mx-auto max-w-[500px]">
      <header className="mb-6 text-center">
        <h1 className="text-2xl font-semibold text-on-surface md:text-3xl">Doi mat khau</h1>
        <p className="mt-2 text-base text-on-surface-variant">
          Chon mot mat khau manh de bao ve tai khoan cua ban.
        </p>
      </header>

      {successMessage ? (
        <div className="mb-4">
          <AuthAlert variant="success" title="Thanh cong" message={successMessage} />
          <p className="mt-2 text-sm text-on-surface-variant">Dang chuyen huong den trang dang nhap...</p>
        </div>
      ) : null}

      {globalError ? (
        <div className="mb-4">
          <AuthAlert variant="error" title="Loi" message={globalError} />
        </div>
      ) : null}

      <form className="space-y-4" onSubmit={onSubmit} noValidate>
        <PasswordField
          id="current-password"
          name="current_password"
          label="Mat khau hien tai"
          value={form.current_password}
          onChange={updateField("current_password")}
          onBlur={() => onBlurField("current_password")}
          placeholder="Nhap mat khau hien tai"
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
            label="Mat khau moi"
            value={form.new_password}
            onChange={updateField("new_password")}
            onBlur={() => onBlurField("new_password")}
            placeholder="Nhap mat khau moi"
            error={errors.new_password}
            visible={visibility.new_password}
            onToggleVisible={() => setVisibility((prev) => ({ ...prev, new_password: !prev.new_password }))}
            disabled={isSubmitting}
          />
          <PasswordChecklist checklistState={checklistState} />
        </div>

        <PasswordField
          id="confirm-password"
          name="confirm_new_password"
          label="Xac nhan mat khau moi"
          value={form.confirm_new_password}
          onChange={updateField("confirm_new_password")}
          onBlur={() => onBlurField("confirm_new_password")}
          placeholder="Nhap lai mat khau moi"
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
          className="mt-3 flex w-full items-center justify-center gap-2 rounded-lg bg-primary-container px-4 py-3 text-sm font-semibold text-white transition hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {isSubmitting ? (
            <>
              <MaterialIcon name="progress_activity" className="animate-spin text-xl" />
              <span>Dang doi mat khau...</span>
            </>
          ) : (
            "Doi mat khau"
          )}
        </button>
      </form>
    </AccountCard>
  );
}
