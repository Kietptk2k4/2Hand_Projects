import { ActivityIndicator, Pressable, StyleSheet, Text } from "react-native";
import { colors } from "../../../shared/theme/colors";

function followButtonLabel(followStatus) {
  switch (followStatus) {
    case "SELF":
      return null;
    case "PENDING":
      return "Đã gửi yêu cầu";
    case "ACCEPTED":
      return "Đang theo dõi";
    default:
      return "Theo dõi";
  }
}

export function FollowButton({
  followStatus,
  onPress,
  isLoading = false,
  disabled = false,
}) {
  const label = followButtonLabel(followStatus);
  if (!label) return null;

  const isPrimary = followStatus === "NONE";

  return (
    <Pressable
      onPress={onPress}
      disabled={disabled || isLoading}
      style={[
        styles.button,
        isPrimary ? styles.primary : styles.secondary,
        (disabled || isLoading) && styles.disabled,
      ]}
      accessibilityRole="button"
      accessibilityLabel={label}
    >
      {isLoading ? (
        <ActivityIndicator size="small" color={isPrimary ? colors.onPrimary : colors.primary} />
      ) : (
        <Text style={[styles.text, isPrimary ? styles.primaryText : styles.secondaryText]}>
          {label}
        </Text>
      )}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  button: {
    minWidth: 140,
    minHeight: 44,
    borderRadius: 8,
    paddingHorizontal: 20,
    alignItems: "center",
    justifyContent: "center",
  },
  primary: {
    backgroundColor: colors.primary,
  },
  secondary: {
    borderWidth: 2,
    borderColor: colors.outlineVariant,
    backgroundColor: colors.surfaceContainerLowest,
  },
  disabled: {
    opacity: 0.6,
  },
  text: {
    fontSize: 14,
    fontWeight: "600",
  },
  primaryText: {
    color: colors.onPrimary,
  },
  secondaryText: {
    color: colors.onSurface,
  },
});
