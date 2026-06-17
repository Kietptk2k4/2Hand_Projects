import { router } from "expo-router";
import { useEffect, useState } from "react";
import { StyleSheet, Text, View } from "react-native";
import { AuthLinkButton } from "./AuthButtons";
import { AuthScreenShell } from "./AuthScreenShell";
import {
  SESSION_EXPIRED_DEFAULT_MESSAGE,
  SESSION_EXPIRED_SIGN_IN,
  SESSION_EXPIRED_TITLE,
} from "../constants/authUiStrings";
import { ROUTES } from "../../../shared/constants/routes";
import { consumeSessionExpiredMessage } from "../utils/authNavigationState";

export function SessionExpiredScreen() {
  const [message, setMessage] = useState(SESSION_EXPIRED_DEFAULT_MESSAGE);

  useEffect(() => {
    setMessage(consumeSessionExpiredMessage());
  }, []);

  return (
    <AuthScreenShell>
      <View style={styles.content}>
        <Text style={styles.icon}>!</Text>
        <Text style={styles.title}>{SESSION_EXPIRED_TITLE}</Text>
        <Text style={styles.message}>{message}</Text>
        <AuthLinkButton
          label={SESSION_EXPIRED_SIGN_IN}
          onPress={() => router.replace(ROUTES.login)}
        />
      </View>
    </AuthScreenShell>
  );
}

const styles = StyleSheet.create({
  content: { alignItems: "center", paddingVertical: 16, gap: 12 },
  icon: { fontSize: 32 },
  title: { fontSize: 22, fontWeight: "700", color: "#1f1f1f", textAlign: "center" },
  message: { fontSize: 14, lineHeight: 20, color: "#5f6368", textAlign: "center" },
});