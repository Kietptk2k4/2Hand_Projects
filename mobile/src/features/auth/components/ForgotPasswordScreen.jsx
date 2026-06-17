import { router } from "expo-router";
import { useMemo, useState } from "react";
import { forgotPassword } from "../api/authApi";
import { AuthBanner } from "./AuthBanner";
import { AuthLinkButton, AuthPrimaryButton } from "./AuthButtons";
import { AuthScreenShell } from "./AuthScreenShell";
import { AuthTextField } from "./AuthTextField";
import { GENERIC_ERROR_RETRY } from "../constants/authUiStrings";
import { ROUTES } from "../../../shared/constants/routes";
import { validateEmail, validateForgotPasswordForm } from "../utils/authSchemas";
import { resolveFieldErrors } from "../utils/resolveFieldErrors";

const PRIVACY_SAFE_SUCCESS_MESSAGE =
  "Neu email hop le, chung toi da gui huong dan dat lai mat khau.";

const ERROR_MESSAGE_BY_CODE = {
  429: "Ban thao tac qua nhanh, vui long thu lai sau.",
  500: GENERIC_ERROR_RETRY,
};

export function ForgotPasswordScreen() {
  const [form, setForm] = useState({ email: "" });
  const [errors, setErrors] = useState({ email: "" });
  const [globalError, setGlobalError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const validation = useMemo(() => validateForgotPasswordForm(form), [form]);
  const isSubmitDisabled = !validation.isValid || isSubmitting;

  const onSubmit = async () => {
    if (isSubmitting) return;

    const normalizedForm = { email: form.email.trim().toLowerCase() };
    const nextValidation = validateForgotPasswordForm(normalizedForm);
    setErrors(nextValidation.errors);
    setGlobalError("");
    setSuccessMessage("");
    if (!nextValidation.isValid) return;

    setIsSubmitting(true);
    try {
      await forgotPassword(normalizedForm);
      setSuccessMessage(PRIVACY_SAFE_SUCCESS_MESSAGE);
    } catch (error) {
      const serverFieldErrors = resolveFieldErrors(error?.errors);
      if (error?.code === 400 && Object.keys(serverFieldErrors).length > 0) {
        setErrors((prev) => ({ ...prev, ...serverFieldErrors }));
      }
      const message =
        error?.code === 400
          ? error?.message || "Du lieu khong hop le. Vui long kiem tra lai email."
          : ERROR_MESSAGE_BY_CODE[error?.code] || GENERIC_ERROR_RETRY;
      setGlobalError(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <AuthScreenShell
      title="Quen mat khau"
      subtitle="Nhap dia chi email cua ban, chung toi se gui lien ket de dat lai mat khau."
      footer={<AuthLinkButton label="Quay lai dang nhap" onPress={() => router.replace(ROUTES.login)} />}
    >
      {globalError ? <AuthBanner message={globalError} /> : null}
      {successMessage ? <AuthBanner variant="success" message={successMessage} /> : null}

      <AuthTextField
        label="Email"
        value={form.email}
        onChangeText={(value) => {
          setForm({ email: value });
          setErrors({ email: "" });
          setGlobalError("");
          setSuccessMessage("");
        }}
        onBlur={() => setErrors({ email: validateEmail(form.email.trim()) })}
        error={errors.email}
        placeholder="user@example.com"
        keyboardType="email-address"
        textContentType="emailAddress"
        editable={!isSubmitting}
      />

      <AuthPrimaryButton
        label="Gui lien ket dat lai"
        loadingLabel="Dang gui lien ket..."
        onPress={onSubmit}
        disabled={isSubmitDisabled}
        loading={isSubmitting}
      />
    </AuthScreenShell>
  );
}
