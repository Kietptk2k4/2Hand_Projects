import { ActivityIndicator, Pressable, Text, TextInput, View } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { MAX_COMMENT_LENGTH } from "../constants/commentConstants";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

function createStyles(colors) {
  return {
    root: {
      flex: 1,
      flexDirection: "row",
      alignItems: "flex-end",
      gap: 8,
    },
    input: {
      flex: 1,
      minHeight: 44,
      maxHeight: 120,
      borderRadius: 22,
      borderWidth: 1,
      borderColor: colors.outlineVariant,
      backgroundColor: colors.surfaceContainerLow,
      paddingHorizontal: 16,
      paddingVertical: 10,
      fontSize: 14,
      color: colors.onSurface,
    },
    inputCompact: {
      borderRadius: 8,
      minHeight: 40,
    },
    sendButton: {
      width: 44,
      height: 44,
      alignItems: "center",
      justifyContent: "center",
    },
    sendCompact: {
      backgroundColor: colors.primary,
      borderRadius: 8,
      paddingHorizontal: 12,
      paddingVertical: 10,
      minHeight: 40,
      justifyContent: "center",
    },
    sendCompactText: {
      color: colors.onPrimary,
      fontSize: 12,
      fontWeight: "600",
    },
    sendDisabled: {
      opacity: 0.5,
    },
  };
}

export function CommentComposer({
  value,
  onChange,
  onSubmit,
  placeholder = "Thêm bình luận...",
  disabled = false,
  isSubmitting = false,
  inputRef,
  variant = "default",
  onClearError,
}) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const isCompact = variant === "compact";
  const canSubmit = !disabled && !isSubmitting && value.trim().length > 0;

  return (
    <View style={styles.root}>
      <TextInput
        ref={inputRef}
        value={value}
        onChangeText={(text) => {
          onChange?.(text);
          onClearError?.();
        }}
        placeholder={placeholder}
        placeholderTextColor={colors.outline}
        editable={!disabled && !isSubmitting}
        multiline
        maxLength={MAX_COMMENT_LENGTH}
        style={[styles.input, isCompact && styles.inputCompact]}
      />
      {isCompact ? (
        <Pressable
          style={[styles.sendCompact, !canSubmit && styles.sendDisabled]}
          onPress={onSubmit}
          disabled={!canSubmit}
        >
          <Text style={styles.sendCompactText}>
            {isSubmitting ? "Đang gửi…" : "Gửi"}
          </Text>
        </Pressable>
      ) : (
        <Pressable
          style={[styles.sendButton, !canSubmit && styles.sendDisabled]}
          onPress={onSubmit}
          disabled={!canSubmit}
          accessibilityLabel="Gửi bình luận"
        >
          {isSubmitting ? (
            <ActivityIndicator size="small" color={colors.primary} />
          ) : (
            <Ionicons name="send" size={20} color={colors.primary} />
          )}
        </Pressable>
      )}
    </View>
  );
}
