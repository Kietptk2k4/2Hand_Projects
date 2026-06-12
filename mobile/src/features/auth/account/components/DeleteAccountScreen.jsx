import { useCallback } from "react";
import {
  ActivityIndicator,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useSocialToast } from "../../../../shared/components/SocialToastProvider";
import { colors } from "../../../../shared/theme/colors";
import { useDeleteAccount } from "../hooks/useDeleteAccount";
import { AccountCard } from "./AccountCard";
import { AccountConfirmModal } from "./AccountConfirmModal";
import { AccountFieldLabel } from "./AccountFieldLabel";
import { AccountPasswordInput } from "./AccountPasswordInput";

const CONSEQUENCES = [
  {
    title: "Mất dữ liệu vĩnh viễn",
    description:
      "Tất cả thông tin cá nhân, cài đặt và tùy chọn sẽ bị xóa khỏi hệ thống.",
  },
  {
    title: "Xóa lịch sử",
    description:
      "Lịch sử đặt dịch vụ, đánh giá và các giao dịch cũ sẽ không thể khôi phục.",
  },
  {
    title: "Hủy bỏ kết nối",
    description:
      "Tất cả các kết nối với nhà cung cấp dịch vụ và khách hàng sẽ bị ngắt.",
  },
];

export function DeleteAccountScreen() {
  const insets = useSafeAreaInsets();
  const { showToast } = useSocialToast();

  const onSuccess = useCallback(() => {
    showToast("Tài khoản đã được xóa.");
  }, [showToast]);

  const onError = useCallback(
    (message) => {
      showToast(message, "error");
    },
    [showToast]
  );

  const deleteAccount = useDeleteAccount({ onSuccess, onError });
  const showFieldError = deleteAccount.fieldError && !deleteAccount.isModalOpen;

  return (
    <>
      <ScrollView
        style={styles.screen}
        contentContainerStyle={[styles.content, { paddingBottom: insets.bottom + 24 }]}
        keyboardShouldPersistTaps="handled"
      >
        <View style={styles.header}>
          <Text style={styles.headerTitle}>Xóa tài khoản</Text>
          <Text style={styles.headerSubtitle}>
            Quản lý việc xóa tài khoản một cách an toàn.
          </Text>
        </View>

        <AccountCard>
          <View style={styles.dangerHeader}>
            <Text style={styles.dangerIcon}>⚠️</Text>
            <Text style={styles.dangerTitle}>Vùng nguy hiểm</Text>
          </View>

          <View style={styles.body}>
            <Text style={styles.lead}>Khi bạn xóa tài khoản của mình:</Text>

            <View style={styles.list}>
              {CONSEQUENCES.map((item) => (
                <View key={item.title} style={styles.listItem}>
                  <Text style={styles.listTitle}>{item.title}</Text>
                  <Text style={styles.listDescription}>{item.description}</Text>
                </View>
              ))}
            </View>

            <View style={styles.passwordCard}>
              <AccountFieldLabel>Xác nhận mật khẩu để tiếp tục</AccountFieldLabel>
              <AccountPasswordInput
                value={deleteAccount.password}
                onChangeText={deleteAccount.updatePassword}
                isVisible={deleteAccount.isPasswordVisible}
                onToggleVisibility={deleteAccount.togglePasswordVisibility}
                error={showFieldError ? deleteAccount.fieldError : ""}
              />
            </View>

            <View style={styles.actions}>
              <Pressable
                onPress={deleteAccount.openConfirmModal}
                disabled={deleteAccount.isSubmitting}
                style={({ pressed }) => [
                  styles.deleteButton,
                  deleteAccount.isSubmitting && styles.deleteButtonDisabled,
                  pressed && !deleteAccount.isSubmitting && styles.buttonPressed,
                ]}
              >
                {deleteAccount.isSubmitting ? (
                  <ActivityIndicator color={colors.onPrimary} />
                ) : (
                  <Text style={styles.deleteButtonText}>Xóa tài khoản</Text>
                )}
              </Pressable>
            </View>
          </View>
        </AccountCard>
      </ScrollView>

      <AccountConfirmModal
        visible={deleteAccount.isModalOpen}
        title="Xác nhận xóa tài khoản?"
        message="Bạn sắp xóa vĩnh viễn quyền truy cập vào tài khoản này. Hành động không thể hoàn tác từ phía bạn."
        confirmLabel="Xóa tài khoản"
        onConfirm={deleteAccount.confirmDelete}
        onCancel={deleteAccount.closeConfirmModal}
        isLoading={deleteAccount.isSubmitting}
        danger
        errorMessage={deleteAccount.isModalOpen ? deleteAccount.fieldError : ""}
      />
    </>
  );
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: colors.surface,
  },
  content: {
    paddingHorizontal: 16,
    paddingTop: 16,
    gap: 16,
  },
  header: {
    gap: 8,
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: "600",
    color: colors.onSurface,
  },
  headerSubtitle: {
    fontSize: 14,
    lineHeight: 20,
    color: colors.onSurfaceVariant,
  },
  dangerHeader: {
    flexDirection: "row",
    alignItems: "center",
    gap: 12,
    borderBottomWidth: 1,
    borderBottomColor: "rgba(186, 26, 26, 0.2)",
    backgroundColor: colors.errorContainer,
    marginHorizontal: -16,
    marginTop: -16,
    marginBottom: 16,
    paddingHorizontal: 16,
    paddingVertical: 16,
    borderTopLeftRadius: 16,
    borderTopRightRadius: 16,
  },
  dangerIcon: {
    fontSize: 20,
  },
  dangerTitle: {
    fontSize: 18,
    fontWeight: "600",
    color: colors.error,
  },
  body: {
    gap: 16,
  },
  lead: {
    fontSize: 16,
    fontWeight: "600",
    color: colors.onSurface,
  },
  list: {
    gap: 16,
    borderLeftWidth: 2,
    borderLeftColor: colors.outlineVariant,
    paddingLeft: 16,
  },
  listItem: {
    gap: 4,
  },
  listTitle: {
    fontSize: 14,
    fontWeight: "600",
    color: colors.onSurface,
  },
  listDescription: {
    fontSize: 14,
    lineHeight: 20,
    color: colors.onSurfaceVariant,
  },
  passwordCard: {
    borderWidth: 1,
    borderColor: colors.outlineVariant,
    borderRadius: 12,
    backgroundColor: colors.surfaceContainerLow,
    padding: 16,
    gap: 8,
  },
  actions: {
    borderTopWidth: 1,
    borderTopColor: colors.outlineVariant,
    paddingTop: 16,
    alignItems: "flex-end",
  },
  deleteButton: {
    minHeight: 48,
    minWidth: 160,
    borderRadius: 8,
    backgroundColor: colors.error,
    alignItems: "center",
    justifyContent: "center",
    paddingHorizontal: 20,
  },
  deleteButtonDisabled: {
    opacity: 0.8,
  },
  deleteButtonText: {
    color: colors.onPrimary,
    fontSize: 14,
    fontWeight: "600",
  },
  buttonPressed: {
    opacity: 0.9,
  },
});