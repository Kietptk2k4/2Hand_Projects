import { StyleSheet, Text, View } from "react-native";
import { useThemeColors } from "../../../shared/theme/useThemeColors";

const CHECKLIST_ITEMS = [
  { key: "length", label: "8-32 ky tu" },
  { key: "uppercase", label: "It nhat 1 chu hoa" },
  { key: "lowercase", label: "It nhat 1 chu thuong" },
  { key: "number", label: "It nhat 1 chu so" },
];

export function PasswordChecklist({ checklistState }) {
  const colors = useThemeColors();
  const styles = StyleSheet.create({
    list: { gap: 6, marginTop: 8, marginBottom: 8 },
    row: { flexDirection: "row", alignItems: "center", gap: 8 },
    dot: { width: 8, height: 8, borderRadius: 4 },
    label: { fontSize: 12 },
  });

  return (
    <View style={styles.list}>
      {CHECKLIST_ITEMS.map((item) => {
        const met = Boolean(checklistState?.[item.key]);
        return (
          <View key={item.key} style={styles.row}>
            <View style={[styles.dot, { backgroundColor: met ? colors.primary : colors.outlineVariant }]} />
            <Text style={[styles.label, { color: met ? colors.onSurface : colors.onSurfaceVariant }]}>{item.label}</Text>
          </View>
        );
      })}
    </View>
  );
}