import { router } from "expo-router";
import { useEffect, useMemo, useRef, useState } from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { registerWithEmail } from "../api/authApi";
import { AuthBanner } from "../components/AuthBanner";
import { AuthLinkButton, AuthPrimaryButton } from "../components/AuthButtons";
import { AuthScreenShell } from "../components/AuthScreenShell";
import { AuthTextField } from "../components/AuthTextField";
import { SocialLoginButtons } from "../components/SocialLoginButtons";
import { GENERIC_ERROR_RETRY } from "../constants/authUiStrings";
import { ROUTES } from "../../../shared/constants/routes";
import {
  validateConfirmPassword,
  validateEmail,
  validatePassword,
  validateRegisterForm,
} from "../utils/authSchemas";
import { resolveFieldErrors } from "../utils/resolveFieldErrors";
import { setVerifyEmailAddress } from "../utils/authNavigationState";

const ERROR_MESSAGE_BY_CODE = {
  409: "Email da duoc su dung.",
  429: "Ban thao tac qua nhanh, vui long thu lai sau.",
  500: "Co loi he thong. Vui long thu lai sau.",
};

export function RegisterScreen() {
  const [form, setForm] = useState({ email: "", password: "", confirm_password: "" });
  const [errors, setErrors] = useState({ email: "", password: "", confirm_password: "" });
  const [globalError, setGlobalError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isSocialRedirecting, setIsSocialRedirecting] = useState(false);
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  const [isConfirmPasswordVisible, setIsConfirmPasswordVisible] = useState(false);
  const redirectTimerRef = useRef(null);

  const validation = useMemo(() => validateRegisterForm(form), [form]);
  const isSubmitDisabled = !validation.isValid || isSubmitting || isSocialRedirecting;

  useEffect(() => {
    return () => {
      if (redirectTimerRef.current) clearTimeout(redirectTimerRef.current);
    };
  }, []);

  const updateField = (key, value) => {
    setForm((prev) => ({ ...prev, [key]: value }));
    setErrors((prev) => ({ ...prev, [key]: "" }));
    setGlobalError("");
    setSuccessMessage("");
  };

  const getErrorMessage = (error) => {
    if (error?.code === 400) return error?.message || "Thong tin dang ky khong hop le.";
    return ERROR_MESSAGE_BY_CODE[error?.code] || GENERIC_ERROR_RETRY;
  };

  const onSubmit = async () => {
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

      const pendingVerification = registerData?.status === "PENDING_VERIFICATION";
      redirectTimerRef.current = setTimeout(() => {
        if (pendingVerification) {
          setVerifyEmailAddress(normalizedForm.email);
          router.replace(ROUTES.verifyEmail);
        } else {
          router.replace(ROUTES.login);
        }
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

  return (
    <AuthScreenShell
      title="Tao tai khoan moi"
      subtitle="Vui long dien thong tin de bat dau hanh trinh cua ban."
      footer={
        <Text style={styles.footerText}>
          Da co tai khoan?{" "}
          <Text style={styles.footerLink} onPress={() => router.push(ROUTES.login)}>
            Dang nhap ngay
          </Text>
        </Text>
      }
    >
      {successMessage ? (
        <AuthBanner variant="success" title="Thanh cong" message={`${successMessage} Dang tu dong chuyen huong...`} />
      ) : null}
      {globalError ? <AuthBanner title="Loi dang ky" message={globalError} /> : null}

      <AuthTextField
        label="Dia chi Email"
        value={form.email}
        onChangeText={(value) => updateField("email", value)}
        onBlur={() => setErrors((prev) => ({ ...prev, email: validateEmail(form.email.trim()) }))}
        error={errors.email}
        placeholder="user@example.com"
        keyboardType="email-address"
        textContentType="emailAddress"
        editable={!isSubmitting}
      />

      <AuthTextField
        label="Mat khau"
        value={form.password}
        onChangeText={(value) => updateField("password", value)}
        onBlur={() => setErrors((prev) => ({ ...prev, password: validatePassword(form.password) }))}
        error={errors.password}
        placeholder="Nhap mat khau"
        secureTextEntry={!isPasswordVisible}
        showToggle
        isPasswordVisible={isPasswordVisible}
        onTogglePassword={() => setIsPasswordVisible((prev) => !prev)}
        textContentType="newPassword"
        editable={!isSubmitting}
      />

      <AuthTextField
        label="Xac nhan mat khau"
        value={form.confirm_password}
        onChangeText={(value) => updateField("confirm_password", value)}
        onBlur={() =>
          setErrors((prev) => ({
            ...prev,
            confirm_password: validateConfirmPassword(form.password, form.confirm_password),
          }))
        }
        error={errors.confirm_password}
        placeholder="Nhap lai mat khau"
        secureTextEntry={!isConfirmPasswordVisible}
        showToggle
        isPasswordVisible={isConfirmPasswordVisible}
        onTogglePassword={() => setIsConfirmPasswordVisible((prev) => !prev)}
        textContentType="newPassword"
        editable={!isSubmitting}
      />

      <AuthPrimaryButton
        label="Dang ky"
        onPress={onSubmit}
        disabled={isSubmitDisabled}
        loading={isSubmitting}
      />

      <SocialLoginButtons
        disabled={isSubmitting || isSocialRedirecting}
        onRedirectStart={() => setIsSocialRedirecting(true)}
        onRedirectEnd={() => setIsSocialRedirecting(false)}
      />
    </AuthScreenShell>
  );
}

const styles = StyleSheet.create({
  footerText: { fontSize: 14, color: "#5f6368" },
  footerLink: { fontWeight: "600", color: "#0066ff" },
});
