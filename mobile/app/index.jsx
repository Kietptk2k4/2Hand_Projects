import { useCallback, useEffect, useState } from "react";
import { ActivityIndicator, StyleSheet, View } from "react-native";
import { router } from "expo-router";
import { getAccessToken } from "../src/services/auth/tokenStorage";
import { ROUTES } from "../src/shared/constants/routes";
import { colors } from "../src/shared/theme/colors";

export default function IndexScreen() {
  const [isChecking, setIsChecking] = useState(true);

  const checkSession = useCallback(async () => {
    const token = await getAccessToken();
    if (token) {
      router.replace(ROUTES.feed);
    } else {
      router.replace(ROUTES.login);
    }
    setIsChecking(false);
  }, []);

  useEffect(() => {
    checkSession();
  }, [checkSession]);

  if (!isChecking) {
    return null;
  }

  return (
    <View style={styles.centered}>
      <ActivityIndicator size="large" color={colors.primary} />
    </View>
  );
}

const styles = StyleSheet.create({
  centered: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: colors.surface,
  },
});
