import { useEffect, useState } from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { router } from "expo-router";
import { loginWithEmail } from "../../src/features/auth/api/authApi";
import { AuthBanner } from "../../src/features/auth/components/AuthBanner";
import { AuthLinkButton, AuthPrimaryButton } from "../../src/features/auth/components/AuthButtons";
import { AuthScreenShell } from "../../src/features/auth/components/AuthScreenShell";
import { AuthTextField } from "../../src/features/auth/components/AuthTextField";
import { SocialLoginButtons } from "../../src/features/auth/components/SocialLoginButtons";
import { GENERIC_ERROR_RETRY } from "../../src/features/auth/constants/authUiStrings";
import { setSessionTokens } from "../../src/services/auth/tokenStorage";
import { ROUTES } from "../../src/shared/constants/routes";
import { validateLoginForm } from "../../src/features/auth/utils/authSchemas";
import {
  consumeLoginBannerMessage,
  setVerifyEmailAddress,
} from "../../src/features/auth/utils/authNavigationState";
import { resolveFieldErrors } from "../../src/features/auth/utils/resolveFieldErrors";

const ERROR_MESSAGE_BY_CODE = {
  401: "Email hoac mat khau khong chinh xac.",
  403: "Tai khoan hien khong kha dung.",
  429: "Ban dang thu qua nhieu lan. Vui long doi it phut roi thu lai.",
  500: "He thong dang ban. Vui long thu lai sau.",
};

export default function LoginScreen() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [fieldErrors, setFieldErrors] = useState({ email: "", password: "" });
  const [globalError, setGlobalError] = useState("");
  const [logoutInfo, setLogoutInfo] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isSocialRedirecting, setIsSocialRedirecting] = useState(false);
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);

  useEffect(() => {
    const message = consumeLoginBannerMessage();
    if (message) setLogoutInfo(message);
  }, []);

  const onSubmit = async () => {
    if (isSubmitting) return;

    const normalizedForm = {
      email: email.trim().toLowerCase(),
      password,
    };
    const validationResult = validateLoginForm(normalizedForm);
    setFieldErrors(validationResult.errors);
    setGlobalError("");

    if (!validationResult.isValid) return;

    setIsSubmitting(true);
    try {
      const loginData = await loginWithEmail(normalizedForm);

      await setSessionTokens({
        accessToken: loginData.access_token,
        refreshToken: loginData.refresh_token,
      });

      const isPendingVerification = loginData?.user?.status === "PENDING_VERIFICATION";
      if (isPendingVerification) {
        setVerifyEmailAddress(normalizedForm.email);
        router.replace(ROUTES.verifyEmail);
        return;
      }

      router.replace(ROUTES.feed);
    } catch (error) {
      const serverFieldErrors = resolveFieldErrors(error?.errors);
      if (error?.code === 400 && Object.keys(serverFieldErrors).length > 0) {
        setFieldErrors((prev) => ({ ...prev, ...serverFieldErrors }));
      }
      const code = error?.code || 500;
      const message =
        code === 400
          ? error?.message || "Du lieu khong hop le. Vui long kiem tra lai."
          : ERROR_MESSAGE_BY_CODE[code] || error?.message || GENERIC_ERROR_RETRY;
      setGlobalError(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <AuthScreenShell
      title="Dang nhap"
      subtitle="Mua ban thoi trang second-hand va ket noi cong dong."
      footer={
        <Text style={styles.footerText}>
          Chua co tai khoan?{" "}
          <Text style={styles.footerLink} onPress={() => router.push(ROUTES.register)}>
            Dang ky ngay
          </Text>
        </Text>
      }
    >
      {logoutInfo ? <AuthBanner variant="info" message={logoutInfo} /> : null}
      {globalError ? <AuthBanner title="Loi dang nhap" message={globalError} /> : null}

      <AuthTextField
        label="Email"
        value={email}
        onChangeText={(value) => {
          setEmail(value);
          setFieldErrors((prev) => ({ ...prev, email: "" }));
          setGlobalError("");
        }}
        error={fieldErrors.email}
        placeholder="ban@example.com"
        keyboardType="email-address"
        textContentType="emailAddress"
      />

      <View style={styles.passwordHeader}>
        <Text style={styles.passwordLabel}>Mat khau</Text>
        <AuthLinkButton
          label="Quen mat khau?"
          align="left"
          onPress={() => router.push(ROUTES.forgotPassword)}
        />
      </View>

      <AuthTextField
        value={password}
        onChangeText={(value) => {
          setPassword(value);
          setFieldErrors((prev) => ({ ...prev, password: "" }));
          setGlobalError("");
        }}
        error={fieldErrors.password}
        placeholder="Nhap mat khau"
        secureTextEntry={!isPasswordVisible}
        showToggle
        isPasswordVisible={isPasswordVisible}
        onTogglePassword={() => setIsPasswordVisible((prev) => !prev)}
        textContentType="password"
      />

      <AuthPrimaryButton
        label="Dang nhap"
        onPress={onSubmit}
        disabled={isSubmitting || isSocialRedirecting}
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
  passwordHeader: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    marginBottom: 8,
  },
  passwordLabel: {
    fontSize: 14,
    fontWeight: "600",
    color: "#1f1f1f",
  },
  footerText: { fontSize: 14, color: "#5f6368", textAlign: "center" },
  footerLink: { fontWeight: "600", color: "#0066ff" },
});
