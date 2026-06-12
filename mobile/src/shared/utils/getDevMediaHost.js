import { Platform } from "react-native";
import Constants from "expo-constants";

function parseDevHostEnv() {
  const raw = process.env.EXPO_PUBLIC_DEV_HOST || "";
  const trimmed = raw.trim();
  if (!trimmed) return "";

  const withoutProtocol = trimmed.replace(/^https?:\/\//i, "");
  return withoutProtocol.split("/")[0].split(":")[0];
}

export function getDevMediaHost() {
  if (Platform.OS === "android" && !Constants.isDevice) {
    return "10.0.2.2";
  }

  const fromEnv = parseDevHostEnv();
  if (fromEnv) {
    return fromEnv;
  }

  if (Platform.OS === "ios" && !Constants.isDevice) {
    return "localhost";
  }

  return "localhost";
}