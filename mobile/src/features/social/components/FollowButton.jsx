import { ActivityIndicator, Pressable, Text } from "react-native";
import { useThemeColors } from "../../../shared/theme/useThemeColors";
import { useThemedStyles } from "../../../shared/theme/useThemedStyles";

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

function createStyles(colors) {
  return {
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
  };
}

export function FollowButton({
  followStatus,
  onPress,
  isLoading = false,
  disabled = false,
  disabledTitle,
}) {
  const colors = useThemeColors();
  const styles = useThemedStyles(createStyles);
  const label = followButtonLabel(followStatus);
  if (!label) return null;

  const isPrimary = followStatus === "NONE";
  const isDisabled = disabled || isLoading;

  return (
    <Pressable
      onPress={onPress}
      disabled={isDisabled}
      style={[
        styles.button,
        isPrimary ? styles.primary : styles.secondary,
        isDisabled && styles.disabled,
      ]}
      accessibilityRole="button"
      accessibilityLabel={label}
      accessibilityHint={isDisabled && disabledTitle ? disabledTitle : undefined}
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
