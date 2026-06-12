import { StyleSheet, Text, View } from "react-native";
import { colors } from "../../../../shared/theme/colors";
import { NOT_UPDATED } from "../constants/authUiStrings";

function resolveValue(value) {
  if (value === null || value === undefined || value === "") {
    return NOT_UPDATED;
  }
  return String(value);
}

export function AccountInfoRow({ label, value, children }) {
  return (
    <View style={styles.row}>
      <Text style={styles.label}>{label}</Text>
      <View style={styles.valueWrap}>
        {children ?? <Text style={styles.value}>{resolveValue(value)}</Text>}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: colors.outlineVariant,
    gap: 4,
  },
  label: {
    fontSize: 14,
    fontWeight: "500",
    color: colors.onSurfaceVariant,
  },
  valueWrap: {
    flexShrink: 1,
  },
  value: {
    fontSize: 14,
    color: colors.onSurface,
  },
});