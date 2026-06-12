import { ActivityIndicator, Pressable, StyleSheet, Text, View } from "react-native";
import { colors } from "../../../../shared/theme/colors";
import { AccountCard } from "./AccountCard";
import { AccountFieldLabel } from "./AccountFieldLabel";
import { AccountTextInput } from "./AccountTextInput";
import { SocialLinksEditor } from "./SocialLinksEditor";

export function EditProfileForm({
  form,
  errors,
  globalError,
  validation,
  isSubmitting,
  updateField,
  updateSocialRow,
  addSocialRow,
  removeSocialRow,
  resetForm,
  submit,
}) {
  const nameCount = form.display_name?.length || 0;
  const bioCount = form.bio?.length || 0;

  return (
    <AccountCard>
      <View style={styles.header}>
        <Text style={styles.subtitle}>
          Cập nhật thông tin cá nhân và cách bạn xuất hiện trên nền tảng.
        </Text>
      </View>

      {globalError ? <Text style={styles.globalError}>{globalError}</Text> : null}

      <View style={styles.field}>
        <AccountFieldLabel required>Tên hiển thị</AccountFieldLabel>
        <AccountTextInput
          value={form.display_name}
          onChangeText={(value) => updateField("display_name", value)}
          maxLength={100}
          error={errors.display_name}
        />
        <Text style={styles.counter}>{nameCount}/100</Text>
      </View>

      <View style={styles.field}>
        <AccountFieldLabel>Giới thiệu</AccountFieldLabel>
        <AccountTextInput
          value={form.bio}
          onChangeText={(value) => updateField("bio", value)}
          multiline
          numberOfLines={4}
          maxLength={500}
          error={errors.bio}
        />
        <Text style={styles.counter}>{bioCount}/500</Text>
      </View>

      <View style={styles.field}>
        <AccountFieldLabel>Website</AccountFieldLabel>
        <AccountTextInput
          value={form.website}
          onChangeText={(value) => updateField("website", value)}
          placeholder="https://example.com"
          keyboardType="url"
          autoCapitalize="none"
          error={errors.website}
        />
      </View>

      <View style={styles.divider} />

      <SocialLinksEditor
        rows={form.social_links}
        errors={errors}
        onUpdateRow={updateSocialRow}
        onAddRow={addSocialRow}
        onRemoveRow={removeSocialRow}
        disabled={isSubmitting}
      />

      <View style={styles.actions}>
        <Pressable
          onPress={resetForm}
          disabled={isSubmitting}
          style={({ pressed }) => [styles.secondaryButton, pressed && styles.buttonPressed]}
        >
          <Text style={styles.secondaryButtonText}>Hủy</Text>
        </Pressable>
        <Pressable
          onPress={submit}
          disabled={isSubmitting || !validation.isValid}
          style={({ pressed }) => [
            styles.primaryButton,
            (isSubmitting || !validation.isValid) && styles.primaryButtonDisabled,
            pressed && styles.buttonPressed,
          ]}
        >
          {isSubmitting ? (
            <ActivityIndicator color={colors.onPrimary} />
          ) : (
            <Text style={styles.primaryButtonText}>Lưu thay đổi</Text>
          )}
        </Pressable>
      </View>
    </AccountCard>
  );
}

const styles = StyleSheet.create({
  header: {
    marginBottom: 16,
  },
  subtitle: {
    fontSize: 14,
    lineHeight: 20,
    color: colors.onSurfaceVariant,
  },
  globalError: {
    marginBottom: 12,
    fontSize: 14,
    color: colors.error,
  },
  field: {
    marginBottom: 16,
  },
  counter: {
    marginTop: 4,
    fontSize: 12,
    color: colors.onSurfaceVariant,
  },
  divider: {
    height: 1,
    backgroundColor: colors.outlineVariant,
    marginVertical: 8,
  },
  actions: {
    flexDirection: "row",
    justifyContent: "flex-end",
    gap: 12,
    marginTop: 24,
    paddingTop: 16,
    borderTopWidth: 1,
    borderTopColor: colors.outlineVariant,
  },
  primaryButton: {
    minHeight: 48,
    minWidth: 132,
    borderRadius: 8,
    backgroundColor: colors.primary,
    alignItems: "center",
    justifyContent: "center",
    paddingHorizontal: 16,
  },
  primaryButtonDisabled: {
    opacity: 0.7,
  },
  secondaryButton: {
    minHeight: 48,
    minWidth: 88,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: colors.outlineVariant,
    alignItems: "center",
    justifyContent: "center",
    paddingHorizontal: 16,
    backgroundColor: colors.surfaceContainerLowest,
  },
  buttonPressed: {
    opacity: 0.9,
  },
  primaryButtonText: {
    color: colors.onPrimary,
    fontSize: 14,
    fontWeight: "600",
  },
  secondaryButtonText: {
    color: colors.onSurface,
    fontSize: 14,
    fontWeight: "600",
  },
});