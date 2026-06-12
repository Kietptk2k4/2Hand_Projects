import {
  ActivityIndicator,
  Modal,
  Pressable,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { colors } from "../../../../shared/theme/colors";

export function AccountConfirmModal({
  visible,
  title,
  message,
  confirmLabel,
  cancelLabel = "Hủy",
  onConfirm,
  onCancel,
  isLoading = false,
  danger = false,
  errorMessage,
  icon = "🗑️",
}) {
  return (
    <Modal
      visible={visible}
      transparent
      animationType="fade"
      onRequestClose={onCancel}
      accessibilityViewIsModal
    >
      <Pressable style={styles.backdrop} onPress={isLoading ? undefined : onCancel}>
        <Pressable style={styles.dialog} onPress={(event) => event.stopPropagation()}>
          <View style={styles.header}>
            <View style={[styles.iconWrap, danger && styles.iconWrapDanger]}>
              <Text style={styles.icon}>{icon}</Text>
            </View>
            <View style={styles.copy}>
              <Text style={styles.title}>{title}</Text>
              {message ? <Text style={styles.message}>{message}</Text> : null}
              {errorMessage ? <Text style={styles.errorText}>{errorMessage}</Text> : null}
            </View>
          </View>

          <View style={styles.actions}>
            <Pressable
              onPress={onCancel}
              disabled={isLoading}
              style={({ pressed }) => [styles.cancelButton, pressed && styles.buttonPressed]}
            >
              <Text style={styles.cancelButtonText}>{cancelLabel}</Text>
            </Pressable>
            <Pressable
              onPress={onConfirm}
              disabled={isLoading}
              style={({ pressed }) => [
                styles.confirmButton,
                danger && styles.confirmButtonDanger,
                isLoading && styles.confirmButtonDisabled,
                pressed && !isLoading && styles.buttonPressed,
              ]}
            >
              {isLoading ? (
                <ActivityIndicator color={colors.onPrimary} />
              ) : (
                <Text style={styles.confirmButtonText}>{confirmLabel}</Text>
              )}
            </Pressable>
          </View>
        </Pressable>
      </Pressable>
    </Modal>
  );
}

const styles = StyleSheet.create({
  backdrop: {
    flex: 1,
    backgroundColor: "rgba(17, 28, 45, 0.4)",
    justifyContent: "center",
    paddingHorizontal: 16,
  },
  dialog: {
    backgroundColor: colors.surfaceContainerLowest,
    borderRadius: 16,
    overflow: "hidden",
    borderWidth: 1,
    borderColor: colors.outlineVariant,
  },
  header: {
    flexDirection: "row",
    alignItems: "flex-start",
    gap: 16,
    padding: 24,
  },
  iconWrap: {
    width: 48,
    height: 48,
    borderRadius: 24,
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: colors.surfaceContainerLow,
  },
  iconWrapDanger: {
    backgroundColor: colors.errorContainer,
  },
  icon: {
    fontSize: 22,
  },
  copy: {
    flex: 1,
    gap: 8,
  },
  title: {
    fontSize: 18,
    fontWeight: "600",
    color: colors.onSurface,
  },
  message: {
    fontSize: 14,
    lineHeight: 20,
    color: colors.onSurfaceVariant,
  },
  errorText: {
    fontSize: 14,
    lineHeight: 20,
    color: colors.error,
  },
  actions: {
    flexDirection: "row",
    justifyContent: "flex-end",
    gap: 12,
    borderTopWidth: 1,
    borderTopColor: colors.outlineVariant,
    backgroundColor: colors.surfaceContainerLow,
    paddingHorizontal: 24,
    paddingVertical: 16,
  },
  cancelButton: {
    minHeight: 44,
    justifyContent: "center",
    paddingHorizontal: 8,
  },
  cancelButtonText: {
    fontSize: 14,
    fontWeight: "600",
    color: colors.onSurfaceVariant,
  },
  confirmButton: {
    minHeight: 44,
    minWidth: 120,
    borderRadius: 8,
    backgroundColor: colors.primary,
    alignItems: "center",
    justifyContent: "center",
    paddingHorizontal: 16,
  },
  confirmButtonDanger: {
    backgroundColor: colors.error,
  },
  confirmButtonDisabled: {
    opacity: 0.8,
  },
  confirmButtonText: {
    color: colors.onPrimary,
    fontSize: 14,
    fontWeight: "600",
  },
  buttonPressed: {
    opacity: 0.9,
  },
});