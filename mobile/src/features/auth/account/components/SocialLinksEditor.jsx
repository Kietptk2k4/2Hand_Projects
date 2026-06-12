import { useState } from "react";
import {
  Modal,
  Pressable,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { SOCIAL_PLATFORMS } from "../../utils/accountSchemas";
import { colors } from "../../../../shared/theme/colors";
import { AccountTextInput } from "./AccountTextInput";

const MAX_SOCIAL_LINKS = 10;

export function SocialLinksEditor({
  rows,
  errors,
  onUpdateRow,
  onAddRow,
  onRemoveRow,
  disabled = false,
}) {
  const [pickerIndex, setPickerIndex] = useState(null);

  const selectedPlatform = pickerIndex === null ? null : rows[pickerIndex]?.platform;

  return (
    <View style={styles.wrap}>
      <View style={styles.header}>
        <View style={styles.headerText}>
          <Text style={styles.title}>Liên kết mạng xã hội</Text>
          <Text style={styles.subtitle}>Thêm liên kết đến các trang cá nhân của bạn.</Text>
        </View>
        <Pressable
          onPress={onAddRow}
          disabled={disabled || rows.length >= MAX_SOCIAL_LINKS}
          style={({ pressed }) => [
            styles.addButton,
            (disabled || rows.length >= MAX_SOCIAL_LINKS) && styles.addButtonDisabled,
            pressed && styles.addButtonPressed,
          ]}
        >
          <Text style={styles.addButtonText}>+ Thêm liên kết</Text>
        </Pressable>
      </View>

      {rows.map((row, index) => (
        <View key={`social-${index}`} style={styles.rowCard}>
          <Pressable
            onPress={() => setPickerIndex(index)}
            disabled={disabled}
            style={styles.platformButton}
          >
            <Text style={styles.platformButtonText}>
              {SOCIAL_PLATFORMS.find((p) => p.key === row.platform)?.label || row.platform}
            </Text>
          </Pressable>
          <View style={styles.urlInput}>
            <AccountTextInput
              value={row.url ?? ""}
              onChangeText={(value) => onUpdateRow(index, "url", value)}
              placeholder="https://"
              keyboardType="url"
              autoCapitalize="none"
              error={errors[`social_links.${index}.url`]}
            />
          </View>
          <Pressable
            onPress={() => onRemoveRow(index)}
            disabled={disabled}
            accessibilityLabel="Xóa liên kết"
          >
            <Text style={styles.removeText}>Xóa</Text>
          </Pressable>
        </View>
      ))}

      {errors.social_links ? <Text style={styles.globalError}>{errors.social_links}</Text> : null}

      <Modal
        visible={pickerIndex !== null}
        transparent
        animationType="fade"
        onRequestClose={() => setPickerIndex(null)}
      >
        <Pressable style={styles.modalBackdrop} onPress={() => setPickerIndex(null)}>
          <View style={styles.modalCard}>
            <Text style={styles.modalTitle}>Chọn nền tảng</Text>
            {SOCIAL_PLATFORMS.map((platform) => (
              <Pressable
                key={platform.key}
                onPress={() => {
                  if (pickerIndex !== null) {
                    onUpdateRow(pickerIndex, "platform", platform.key);
                  }
                  setPickerIndex(null);
                }}
                style={({ pressed }) => [styles.modalOption, pressed && styles.modalOptionPressed]}
              >
                <Text
                  style={[
                    styles.modalOptionText,
                    platform.key === selectedPlatform && styles.modalOptionTextActive,
                  ]}
                >
                  {platform.label}
                </Text>
              </Pressable>
            ))}
          </View>
        </Pressable>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: {
    gap: 12,
  },
  header: {
    gap: 12,
  },
  headerText: {
    gap: 4,
  },
  title: {
    fontSize: 16,
    fontWeight: "600",
    color: colors.onSurface,
  },
  subtitle: {
    fontSize: 14,
    color: colors.onSurfaceVariant,
  },
  addButton: {
    alignSelf: "flex-start",
    borderWidth: 1,
    borderColor: colors.outlineVariant,
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 10,
    backgroundColor: colors.surfaceContainerLowest,
  },
  addButtonPressed: {
    backgroundColor: colors.surfaceContainerLow,
  },
  addButtonDisabled: {
    opacity: 0.5,
  },
  addButtonText: {
    fontSize: 14,
    fontWeight: "600",
    color: colors.primary,
  },
  rowCard: {
    borderWidth: 1,
    borderColor: colors.outlineVariant,
    borderRadius: 12,
    padding: 12,
    gap: 10,
    backgroundColor: colors.surfaceContainerLow,
  },
  platformButton: {
    alignSelf: "flex-start",
    borderWidth: 1,
    borderColor: colors.outlineVariant,
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 8,
    backgroundColor: colors.surfaceContainerLowest,
  },
  platformButtonText: {
    fontSize: 14,
    color: colors.onSurface,
  },
  urlInput: {
    flex: 1,
  },
  removeText: {
    fontSize: 14,
    color: colors.error,
    alignSelf: "flex-end",
  },
  globalError: {
    fontSize: 12,
    color: colors.error,
  },
  modalBackdrop: {
    flex: 1,
    backgroundColor: "rgba(0,0,0,0.4)",
    justifyContent: "center",
    padding: 24,
  },
  modalCard: {
    backgroundColor: colors.surfaceContainerLowest,
    borderRadius: 16,
    padding: 16,
    gap: 4,
  },
  modalTitle: {
    fontSize: 16,
    fontWeight: "600",
    color: colors.onSurface,
    marginBottom: 8,
  },
  modalOption: {
    paddingVertical: 12,
    paddingHorizontal: 8,
    borderRadius: 8,
  },
  modalOptionPressed: {
    backgroundColor: colors.surfaceContainerLow,
  },
  modalOptionText: {
    fontSize: 16,
    color: colors.onSurface,
  },
  modalOptionTextActive: {
    color: colors.primary,
    fontWeight: "600",
  },
});