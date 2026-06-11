import { useState } from "react";
import {
  ActivityIndicator,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import { router } from "expo-router";
import { loginWithEmail } from "../../src/features/auth/api/authApi";
import { setSessionTokens } from "../../src/services/auth/tokenStorage";
import { colors } from "../../src/shared/theme/colors";

const ERROR_MESSAGE_BY_CODE = {
  401: "Email hoac mat khau khong chinh xac.",
  403: "Tai khoan hien khong kha dung.",
  429: "Ban dang thu qua nhieu lan. Vui long doi it phut roi thu lai.",
  500: "He thong dang ban. Vui long thu lai sau.",
};

function validateEmail(email) {
  if (!email) return "Vui long nhap email.";
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) return "Email khong hop le.";
  return "";
}

function validatePassword(password) {
  if (!password) return "Vui long nhap mat khau.";
  return "";
}

function resolveFieldErrors(errors = []) {
  return errors.reduce((acc, item) => {
    if (item?.field && !acc[item.field]) {
      acc[item.field] = item.reason || "Du lieu khong hop le.";
    }
    return acc;
  }, {});
}

function getErrorMessage(error) {
  const code = error?.code || 500;
  if (code === 400) {
    return error?.message || "Du lieu khong hop le. Vui long kiem tra lai.";
  }
  return ERROR_MESSAGE_BY_CODE[code] || error?.message || "Co loi xay ra. Vui long thu lai.";
}

export default function LoginScreen() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [fieldErrors, setFieldErrors] = useState({ email: "", password: "" });
  const [globalError, setGlobalError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);

  const onSubmit = async () => {
    if (isSubmitting) return;

    const normalizedEmail = email.trim().toLowerCase();
    const emailError = validateEmail(normalizedEmail);
    const passwordError = validatePassword(password);

    setFieldErrors({ email: emailError, password: passwordError });
    setGlobalError("");

    if (emailError || passwordError) return;

    setIsSubmitting(true);
    try {
      const loginData = await loginWithEmail({
        email: normalizedEmail,
        password,
      });

      await setSessionTokens({
        accessToken: loginData.access_token,
        refreshToken: loginData.refresh_token,
      });

      router.replace("/(tabs)/feed");
    } catch (error) {
      const serverFieldErrors = resolveFieldErrors(error?.errors);
      if (error?.code === 400 && Object.keys(serverFieldErrors).length > 0) {
        setFieldErrors((prev) => ({ ...prev, ...serverFieldErrors }));
      }
      setGlobalError(getErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <KeyboardAvoidingView
      style={styles.flex}
      behavior={Platform.OS === "ios" ? "padding" : undefined}
    >
      <ScrollView
        contentContainerStyle={styles.scrollContent}
        keyboardShouldPersistTaps="handled"
      >
        <View style={styles.card}>
          <Text style={styles.brand}>2Hands</Text>
          <Text style={styles.heading}>Dang nhap</Text>
          <Text style={styles.description}>
            Mua ban thoi trang second-hand va ket noi cong dong.
          </Text>

          {globalError ? (
            <View style={styles.errorBanner}>
              <Text style={styles.errorBannerText}>{globalError}</Text>
            </View>
          ) : null}

          <View style={styles.field}>
            <Text style={styles.label}>Email</Text>
            <TextInput
              style={[styles.input, fieldErrors.email ? styles.inputError : null]}
              value={email}
              onChangeText={(value) => {
                setEmail(value);
                setFieldErrors((prev) => ({ ...prev, email: "" }));
                setGlobalError("");
              }}
              autoCapitalize="none"
              autoCorrect={false}
              keyboardType="email-address"
              textContentType="emailAddress"
              placeholder="ban@example.com"
              placeholderTextColor={colors.onSurfaceVariant}
            />
            {fieldErrors.email ? (
              <Text style={styles.fieldError}>{fieldErrors.email}</Text>
            ) : null}
          </View>

          <View style={styles.field}>
            <Text style={styles.label}>Mat khau</Text>
            <View style={styles.passwordRow}>
              <TextInput
                style={[
                  styles.input,
                  styles.passwordInput,
                  fieldErrors.password ? styles.inputError : null,
                ]}
                value={password}
                onChangeText={(value) => {
                  setPassword(value);
                  setFieldErrors((prev) => ({ ...prev, password: "" }));
                  setGlobalError("");
                }}
                secureTextEntry={!isPasswordVisible}
                textContentType="password"
                placeholder="Nhap mat khau"
                placeholderTextColor={colors.onSurfaceVariant}
              />
              <Pressable
                style={styles.togglePassword}
                onPress={() => setIsPasswordVisible((prev) => !prev)}
              >
                <Text style={styles.togglePasswordText}>
                  {isPasswordVisible ? "An" : "Hien"}
                </Text>
              </Pressable>
            </View>
            {fieldErrors.password ? (
              <Text style={styles.fieldError}>{fieldErrors.password}</Text>
            ) : null}
          </View>

          <Pressable
            style={[styles.submitButton, isSubmitting && styles.submitButtonDisabled]}
            onPress={onSubmit}
            disabled={isSubmitting}
          >
            {isSubmitting ? (
              <ActivityIndicator color={colors.onPrimary} />
            ) : (
              <Text style={styles.submitButtonText}>Dang nhap</Text>
            )}
          </Pressable>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  flex: {
    flex: 1,
    backgroundColor: colors.surface,
  },
  scrollContent: {
    flexGrow: 1,
    justifyContent: "center",
    padding: 24,
  },
  card: {
    backgroundColor: colors.surfaceContainerLowest,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: colors.outlineVariant,
    padding: 24,
  },
  brand: {
    fontSize: 14,
    fontWeight: "700",
    color: colors.primary,
    marginBottom: 8,
  },
  heading: {
    fontSize: 24,
    fontWeight: "700",
    color: colors.onSurface,
    marginBottom: 8,
  },
  description: {
    fontSize: 14,
    lineHeight: 20,
    color: colors.onSurfaceVariant,
    marginBottom: 24,
  },
  errorBanner: {
    backgroundColor: "#FFEDEA",
    borderRadius: 8,
    padding: 12,
    marginBottom: 16,
  },
  errorBannerText: {
    color: colors.error,
    fontSize: 14,
    lineHeight: 20,
  },
  field: {
    marginBottom: 16,
  },
  label: {
    fontSize: 14,
    fontWeight: "600",
    color: colors.onSurface,
    marginBottom: 8,
  },
  input: {
    borderWidth: 1,
    borderColor: colors.outlineVariant,
    borderRadius: 8,
    minHeight: 48,
    paddingHorizontal: 12,
    fontSize: 16,
    color: colors.onSurface,
    backgroundColor: colors.surfaceContainerLowest,
  },
  inputError: {
    borderColor: colors.error,
  },
  passwordRow: {
    position: "relative",
  },
  passwordInput: {
    paddingRight: 64,
  },
  togglePassword: {
    position: "absolute",
    right: 12,
    top: 0,
    bottom: 0,
    justifyContent: "center",
  },
  togglePasswordText: {
    color: colors.primary,
    fontSize: 14,
    fontWeight: "600",
  },
  fieldError: {
    marginTop: 6,
    fontSize: 12,
    color: colors.error,
  },
  submitButton: {
    marginTop: 8,
    backgroundColor: colors.primary,
    borderRadius: 8,
    minHeight: 48,
    alignItems: "center",
    justifyContent: "center",
  },
  submitButtonDisabled: {
    opacity: 0.8,
  },
  submitButtonText: {
    color: colors.onPrimary,
    fontSize: 16,
    fontWeight: "600",
  },
});
