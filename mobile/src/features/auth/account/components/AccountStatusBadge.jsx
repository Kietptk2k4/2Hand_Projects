import { StyleSheet, Text, View } from "react-native";
import { colors } from "../../../../shared/theme/colors";
import { getUserStatusLabel } from "../constants/authUiStrings";

const STATUS_STYLES = {
  ACTIVE: {
    backgroundColor: "#E8F5E9",
    color: "#1B5E20",
  },
  PENDING_VERIFICATION: {
    backgroundColor: "#FFF8E1",
    color: "#F57F17",
  },
  default: {
    backgroundColor: colors.surfaceContainerHigh,
    color: colors.onSurfaceVariant,
  },
};

export function AccountStatusBadge({ status }) {
  const palette = STATUS_STYLES[status] || STATUS_STYLES.default;

  return (
    <View style={[styles.badge, { backgroundColor: palette.backgroundColor }]}>
      <Text style={[styles.text, { color: palette.color }]}>
        {getUserStatusLabel(status || "UNKNOWN")}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  badge: {
    alignSelf: "flex-start",
    borderRadius: 999,
    paddingHorizontal: 10,
    paddingVertical: 4,
  },
  text: {
    fontSize: 12,
    fontWeight: "600",
  },
});