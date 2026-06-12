import { ActivityIndicator, StyleSheet, Switch, Text, View } from "react-native";
import { colors } from "../../../../shared/theme/colors";

export function AccountSettingSwitch({
  title,
  description,
  value,
  onValueChange,
  disabled = false,
  isSaving = false,
  testID,
}) {
  return (
    <View style={styles.row}>
      <View style={styles.copy}>
        <Text style={styles.title}>{title}</Text>
        {description ? <Text style={styles.description}>{description}</Text> : null}
      </View>
      <View style={styles.control}>
        {isSaving ? (
          <ActivityIndicator size="small" color={colors.primary} style={styles.spinner} />
        ) : null}
        <Switch
          testID={testID}
          value={value}
          onValueChange={onValueChange}
          disabled={disabled || isSaving}
          trackColor={{ false: colors.outlineVariant, true: colors.primary }}
          thumbColor={colors.onPrimary}
          accessibilityRole="switch"
          accessibilityState={{ checked: value, disabled: disabled || isSaving }}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: "row",
    alignItems: "flex-start",
    justifyContent: "space-between",
    gap: 16,
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
  description: {
    fontSize: 14,
    lineHeight: 20,
    color: colors.onSurfaceVariant,
  },
  control: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
    paddingTop: 4,
  },
  spinner: {
    marginRight: 4,
  },
});
