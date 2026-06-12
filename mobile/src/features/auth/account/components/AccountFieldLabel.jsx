import { StyleSheet, Text, View } from "react-native";
import { colors } from "../../../../shared/theme/colors";

export function AccountFieldLabel({ children, required = false }) {
  return (
    <View style={styles.row}>
      <Text style={styles.label}>{children}</Text>
      {required ? <Text style={styles.required}>*</Text> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: "row",
    alignItems: "center",
    gap: 4,
    marginBottom: 6,
  },
  label: {
    fontSize: 14,
    fontWeight: "600",
    color: colors.onSurface,
  },
  required: {
    fontSize: 14,
    color: colors.error,
  },
});