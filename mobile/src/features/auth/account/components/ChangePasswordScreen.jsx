import { router } from "expo-router";
import { useEffect, useMemo, useRef, useState } from "react";
import { ScrollView, StyleSheet, Text, View } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { changePassword } from "../../api/authApi";
import { AuthBanner } from "../../components/AuthBanner";
import { AuthPrimaryButton } from "../../components/AuthButtons";
import { AuthTextField } from "../../components/AuthTextField";
import { PasswordChecklist } from "../../components/PasswordChecklist";
import { GENERIC_ERROR_RETRY } from "../../constants/authUiStrings";
import { ROUTES } from "../../../../shared/constants/routes";
import { useThemeColors } from "../../../../shared/theme/useThemeColors";
import { clearAuthSession } from "../../utils/clearAuthSession";
import {
  getPasswordChecklistState,
  validateChangePasswordForm,
  validateConfirmNewPassword,
  validateCurrentPassword,
  validateNewPassword,
} from "../../utils/authSchemas";
import { resolveFieldErrors } from "../../utils/resolveFieldErrors";
import { setSessionExpiredMessage } from "../../utils/authNavigationState";
import { AccountCard } from "./AccountCard";

const ERROR_MESSAGE_BY_CODE = {
  500: GENERIC_ERROR_RETRY,
};

export function ChangePasswordScreen() {
  const colors = useThemeColors();
  const insets = useSafeAreaInsets();
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
  const [visibility, setVisibility] = useState({
    current_password: false,
    new_password: false,
    confirm_new_password: false,
  });
  const [globalError, setGlobalError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const redirectTimerRef = useRef(null);

  const validation = useMemo(() => validateChangePasswordForm(form), [form]);
  const checklistState = useMemo(
    () => getPasswordChecklistState(form.new_password || ""),
    [form.new_password]
  );
  const isSubmitDisabled = !validation.isValid || isSubmitting;

  useEffect(() => {
    return () => {
      if (redirectTimerRef.current) clearTimeout(redirectTimerRef.current);
    };
  }, []);

  const updateField = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
    setErrors((prev) => ({ ...prev, [field]: "" }));
    setGlobalError("");
    setSuccessMessage("");
  };

  const goToLoginAndClearSession = async () => {
    await clearAuthSession({ redirectToLogin: false });
    router.replace(ROUTES.login);
  };

  const onSubmit = async () => {
    if (isSubmitting) return;

    const nextValidation = validateChangePasswordForm(form);
    setErrors(nextValidation.errors);
    setGlobalError("");
    setSuccessMessage("");
    if (!nextValidation.isValid) return;

    setIsSubmitting(true);
    try {
      await changePassword(form);
      setSuccessMessage(
        "Doi mat khau thanh cong. Ban can dang nhap lai, va cac phien dang nhap khac se bi thu hoi."
      );
      redirectTimerRef.current = setTimeout(() => {
        goToLoginAndClearSession();
      }, 1200);
    } catch (error) {
      if (error?.code === 401) {
        setSessionExpiredMessage(error?.message);
        await clearAuthSession({ redirectToLogin: false });
        router.replace(ROUTES.sessionExpired);
        return;
      }

      const serverFieldErrors = resolveFieldErrors(error?.errors);
      const mappedErrors = { ...serverFieldErrors };
      if (mappedErrors.password) {
        mappedErrors.current_password = mappedErrors.password;
        delete mappedErrors.password;
      }

      if (error?.code === 400 && Object.keys(mappedErrors).length > 0) {
        setErrors((prev) => ({ ...prev, ...mappedErrors }));
      }

      const message =
        error?.code === 400
          ? error?.message || "Thong tin khong hop le. Vui long kiem tra lai."
          : ERROR_MESSAGE_BY_CODE[error?.code] || GENERIC_ERROR_RETRY;
      setGlobalError(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const styles = StyleSheet.create({
    screen: { flex: 1, backgroundColor: colors.surface },
    content: { paddingHorizontal: 16, paddingTop: 16, gap: 16 },
    headerTitle: { fontSize: 20, fontWeight: "600", color: colors.onSurface },
    headerSubtitle: { fontSize: 14, lineHeight: 20, color: colors.onSurfaceVariant },
  });

  return (
    <ScrollView
      style={styles.screen}
      contentContainerStyle={[styles.content, { paddingBottom: insets.bottom + 24 }]}
      keyboardShouldPersistTaps="handled"
    >
      <View>
        <Text style={styles.headerTitle}>Doi mat khau</Text>
        <Text style={styles.headerSubtitle}>
          Chon mot mat khau manh de bao ve tai khoan cua ban.
        </Text>
      </View>

      <AccountCard>
        {successMessage ? (
          <AuthBanner variant="success" title="Thanh cong" message={successMessage} />
        ) : null}
        {globalError ? <AuthBanner title="Loi" message={globalError} /> : null}

        <AuthTextField
          label="Mat khau hien tai"
          value={form.current_password}
          onChangeText={(value) => updateField("current_password", value)}
          onBlur={() =>
            setErrors((prev) => ({
              ...prev,
              current_password: validateCurrentPassword(form.current_password),
            }))
          }
          error={errors.current_password}
          placeholder="Nhap mat khau hien tai"
          secureTextEntry={!visibility.current_password}
          showToggle
          isPasswordVisible={visibility.current_password}
          onTogglePassword={() =>
            setVisibility((prev) => ({ ...prev, current_password: !prev.current_password }))
          }
          editable={!isSubmitting}
        />

        <AuthTextField
          label="Mat khau moi"
          value={form.new_password}
          onChangeText={(value) => updateField("new_password", value)}
          onBlur={() =>
            setErrors((prev) => ({
              ...prev,
              new_password: validateNewPassword(form.new_password, form.current_password),
            }))
          }
          error={errors.new_password}
          placeholder="Nhap mat khau moi"
          secureTextEntry={!visibility.new_password}
          showToggle
          isPasswordVisible={visibility.new_password}
          onTogglePassword={() =>
            setVisibility((prev) => ({ ...prev, new_password: !prev.new_password }))
          }
          editable={!isSubmitting}
        />
        <PasswordChecklist checklistState={checklistState} />

        <AuthTextField
          label="Xac nhan mat khau moi"
          value={form.confirm_new_password}
          onChangeText={(value) => updateField("confirm_new_password", value)}
          onBlur={() =>
            setErrors((prev) => ({
              ...prev,
              confirm_new_password: validateConfirmNewPassword(
                form.new_password,
                form.confirm_new_password
              ),
            }))
          }
          error={errors.confirm_new_password}
          placeholder="Nhap lai mat khau moi"
          secureTextEntry={!visibility.confirm_new_password}
          showToggle
          isPasswordVisible={visibility.confirm_new_password}
          onTogglePassword={() =>
            setVisibility((prev) => ({
              ...prev,
              confirm_new_password: !prev.confirm_new_password,
            }))
          }
          editable={!isSubmitting}
        />

        <AuthPrimaryButton
          label="Doi mat khau"
          onPress={onSubmit}
          disabled={isSubmitDisabled}
          loading={isSubmitting}
        />
      </AccountCard>
    </ScrollView>
  );
}
