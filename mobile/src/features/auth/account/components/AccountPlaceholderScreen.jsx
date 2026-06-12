import { StyleSheet, Text, View } from "react-native";
import { colors } from "../../../../shared/theme/colors";

export function AccountPlaceholderScreen({ title, phaseLabel }) {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>{title}</Text>
      <Text style={styles.body}>
        Màn hình này sẽ được triển khai ở {phaseLabel}.
      </Text>
      <Text style={styles.hint}>Phase 1 — navigation shell only.</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.surface,
    paddingHorizontal: 16,
    paddingTop: 24,
    alignItems: "center",
  },
  title: {
    fontSize: 20,
    fontWeight: "600",
    color: colors.onSurface,
    textAlign: "center",
  },
  body: {
    marginTop: 12,
    fontSize: 16,
    lineHeight: 24,
    color: colors.onSurfaceVariant,
    textAlign: "center",
  },
  hint: {
    marginTop: 24,
    fontSize: 12,
    color: colors.outline,
    textAlign: "center",
  },
});
