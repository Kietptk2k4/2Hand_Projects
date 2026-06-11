import { StyleSheet, Text, View } from "react-native";
import { colors } from "../../src/shared/theme/colors";

export default function ShopTabScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Cua hang</Text>
      <Text style={styles.subtitle}>Commerce module se duoc them o phase tiep theo.</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    padding: 24,
    backgroundColor: colors.surface,
  },
  title: {
    fontSize: 20,
    fontWeight: "600",
    color: colors.onSurface,
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 14,
    color: colors.onSurfaceVariant,
    textAlign: "center",
  },
});
