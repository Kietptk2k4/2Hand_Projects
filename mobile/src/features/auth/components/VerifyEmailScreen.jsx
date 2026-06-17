import { router } from "expo-router";
import { useEffect, useMemo, useRef, useState } from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { resendEmailVerification, verifyEmail } from "../api/authApi";
import { AuthBanner } from "./AuthBanner";
import { AuthPrimaryButton } from "./AuthButtons";
import { AuthScreenShell } from "./AuthScreenShell";
import { AuthTextField } from "./AuthTextField";
import { GENERIC_ERROR_RETRY } from "../constants/authUiStrings";
import { ROUTES } from "../../../shared/constants/routes";
import {
  normalizeOtpInput,
  validateEmail,
  validateVerifyEmailForm,
  validateVerifyToken,
} from "../utils/authSchemas";
import { resolveFieldErrors } from "../utils/resolveFieldErrors";
import { consumeVerifyEmailAddress } from "../utils/authNavigationState";

const RESEND_COOLDOWN_SECONDS = 90;

const ERROR_MESSAGE_BY_CODE = {
  400: "Ma OTP khong hop le hoac da het han.",
  429: "Ban thao tac qua nhanh. Vui long thu lai sau.",
  500: GENERIC_ERROR_RETRY,
};

export function VerifyEmailScreen() {
  const initialEmail = consumeVerifyEmailAddress();
  const [email, setEmail] = useState(initialEmail);
  const [form, setForm] = useState({ token: "" });
  const [errors, setErrors] = useState({ token: "", email: "" });
  const [globalError, setGlobalError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [resendMessage, setResendMessage] = useState("");
  const [resendError, setResendError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isResending, setIsResending] = useState(false);
  const [resendCountdown, setResendCountdown] = useState(0);
  const redirectTimerRef = useRef(null);
  const countdownTimerRef = useRef(null);

  const needsEmailInput = !initialEmail;
  const emailIsValid = !validateEmail(email);
  const validation = useMemo(() => validateVerifyEmailForm(form), [form]);
  const isSubmitDisabled = !validation.isValid || isSubmitting;
  const isResendDisabled = isResending || isSubmitting || resendCountdown > 0 || !emailIsValid;

  useEffect(() => {
    return () => {
      if (redirectTimerRef.current) clearTimeout(redirectTimerRef.current);
      if (countdownTimerRef.current) clearInterval(countdownTimerRef.current);
    };
  }, []);

  useEffect(() => {
    if (resendCountdown <= 0) return undefined;

    countdownTimerRef.current = setInterval(() => {
      setResendCountdown((prev) => {
        if (prev <= 1) {
          if (countdownTimerRef.current) clearInterval(countdownTimerRef.current);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => {
      if (countdownTimerRef.current) clearInterval(countdownTimerRef.current);
    };
  }, [resendCountdown]);

  const onSubmit = async () => {
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
      setSuccessMessage("Xac thuc email thanh cong. Dang chuyen huong den dang nhap...");
      redirectTimerRef.current = setTimeout(() => {
        router.replace(ROUTES.login);
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
      setResendMessage("Da gui lai ma OTP. Vui long kiem tra hop thu (va thu muc spam).");
      setResendCountdown(RESEND_COOLDOWN_SECONDS);
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

  return (
    <AuthScreenShell
      title="Xac thuc email"
      subtitle="Kiem tra hop thu cua ban. Chung toi da gui ma OTP 6 chu so — nhap ma ben duoi de xac nhan tai khoan."
    >
      {email && !needsEmailInput ? (
        <Text style={styles.emailHint}>
          Ma duoc gui toi: <Text style={styles.emailValue}>{email}</Text>
        </Text>
      ) : null}

      {globalError ? <AuthBanner message={globalError} /> : null}
      {successMessage ? <AuthBanner variant="success" message={successMessage} /> : null}
      {resendError ? <AuthBanner message={resendError} /> : null}
      {resendMessage ? <AuthBanner variant="success" message={resendMessage} /> : null}

      {needsEmailInput ? (
        <AuthTextField
          label="Email dang ky"
          value={email}
          onChangeText={(value) => {
            setEmail(value.trim().toLowerCase());
            setErrors((prev) => ({ ...prev, email: "" }));
            setResendError("");
            setResendMessage("");
          }}
          onBlur={() => setErrors((prev) => ({ ...prev, email: validateEmail(email) }))}
          error={errors.email}
          placeholder="user@example.com"
          keyboardType="email-address"
          textContentType="emailAddress"
          editable={!isSubmitting && !isResending}
        />
      ) : null}

      <AuthTextField
        label="Ma OTP 6 chu so"
        value={form.token}
        onChangeText={(value) => {
          setForm({ token: normalizeOtpInput(value) });
          setErrors((prev) => ({ ...prev, token: "" }));
          setGlobalError("");
          setSuccessMessage("");
        }}
        onBlur={() => setErrors((prev) => ({ ...prev, token: validateVerifyToken(form.token) }))}
        error={errors.token}
        placeholder="000000"
        keyboardType="number-pad"
        maxLength={6}
        textAlign="center"
        editable={!isSubmitting}
      />

      <AuthPrimaryButton
        label="Xac thuc email"
        onPress={onSubmit}
        disabled={isSubmitDisabled}
        loading={isSubmitting}
      />

      <View style={styles.resendBlock}>
        <Text style={styles.resendHint}>Chua nhan duoc email?</Text>
        <Pressable onPress={onResend} disabled={isResendDisabled}>
          <Text style={[styles.resendLink, isResendDisabled && styles.resendDisabled]}>
            {isResending
              ? "Dang gui lai..."
              : resendCountdown > 0
                ? `Gui lai ma (${resendCountdown}s)`
                : "Gui lai ma"}
          </Text>
        </Pressable>
      </View>
    </AuthScreenShell>
  );
}

const styles = StyleSheet.create({
  emailHint: { fontSize: 14, color: "#5f6368", marginBottom: 16 },
  emailValue: { fontWeight: "600", color: "#1f1f1f" },
  resendBlock: { marginTop: 24, alignItems: "center", gap: 8 },
  resendHint: { fontSize: 14, color: "#5f6368" },
  resendLink: { fontSize: 14, fontWeight: "600", color: "#0066ff" },
  resendDisabled: { opacity: 0.5 },
});
