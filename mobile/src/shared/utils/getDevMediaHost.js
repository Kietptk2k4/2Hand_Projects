import { Platform } from "react-native";
import Constants from "expo-constants";
import { logDevMediaHostContext } from "./debugMediaLog";

const LOCAL_LOOPBACK_HOSTS = new Set(["localhost", "127.0.0.1", "10.0.2.2"]);

function parseHostFromRaw(value) {
  const trimmed = String(value || "").trim();
  if (!trimmed) return "";

  const withoutProtocol = trimmed.replace(/^https?:\/\//i, "");
  const hostPort = withoutProtocol.split("/")[0];
  return hostPort.split(":")[0];
}

function isLanHost(host) {
  if (!host) return false;
  return !LOCAL_LOOPBACK_HOSTS.has(host.toLowerCase());
}

function parseDevHostEnv() {
  return parseHostFromRaw(process.env.EXPO_PUBLIC_DEV_HOST);
}

function parseHostFromServiceBaseUrl() {
  return parseHostFromRaw(process.env.EXPO_PUBLIC_AUTH_SERVICE_BASE_URL);
}

function parseHostFromExpoConfig() {
  const extra = Constants.expoConfig?.extra;
  const fromDevHost = parseHostFromRaw(extra?.devHost);
  if (isLanHost(fromDevHost)) return fromDevHost;

  const fromAuthBase = parseHostFromRaw(extra?.authServiceBaseUrl);
  if (isLanHost(fromAuthBase)) return fromAuthBase;

  const hostUri = Constants.expoConfig?.hostUri;
  if (hostUri) {
    const host = hostUri.split(":")[0];
    if (isLanHost(host)) return host;
  }

  return "";
}

function isAndroidEmulator() {
  // Expo Go on a physical phone may leave isDevice undefined — only treat explicit false as emulator.
  return Platform.OS === "android" && Constants.isDevice === false;
}

function isIosSimulator() {
  return Platform.OS === "ios" && Constants.isDevice === false;
}

function logHostSelection(selectedHost, fromEnv, fromAuth, fromExpo) {
  logDevMediaHostContext({
    isDevice: Constants.isDevice,
    platform: Platform.OS,
    selectedHost,
    fromEnv,
    fromAuth,
    fromExpo,
    hostUri: Constants.expoConfig?.hostUri ?? null,
    extra: Constants.expoConfig?.extra ?? null,
  });
}

export function getDevMediaHost() {
  const fromEnv = parseDevHostEnv();
  const fromAuth = parseHostFromServiceBaseUrl();
  const fromExpo = parseHostFromExpoConfig();

  const candidates = [fromEnv, fromAuth, fromExpo];
  for (const host of candidates) {
    if (isLanHost(host)) {
      logHostSelection(host, fromEnv, fromAuth, fromExpo);
      return host;
    }
  }

  if (isAndroidEmulator()) {
    logHostSelection("10.0.2.2", fromEnv, fromAuth, fromExpo);
    return "10.0.2.2";
  }

  if (isIosSimulator()) {
    logHostSelection("localhost", fromEnv, fromAuth, fromExpo);
    return "localhost";
  }

  logHostSelection("localhost", fromEnv, fromAuth, fromExpo);
  return "localhost";
}

function isPrivateLanHost(host) {
  if (!host) return false;
  const normalized = host.toLowerCase();
  if (LOCAL_LOOPBACK_HOSTS.has(normalized)) return true;
  if (normalized.startsWith("192.168.")) return true;
  if (normalized.startsWith("10.")) return true;
  const match = /^172\.(\d+)\./.exec(normalized);
  if (match) {
    const secondOctet = Number.parseInt(match[1], 10);
    return secondOctet >= 16 && secondOctet <= 31;
  }
  return false;
}

/**
 * Dev-only MinIO origin for presigned PUT (must match signature host).
 * Omit for localhost / simulator — server presigns with default localhost.
 * Omit for public ngrok gateway — server uses *_MINIO_PRESIGNED_ENDPOINT.
 */
export function getClientUploadOrigin() {
  if (!__DEV__) return undefined;

  const host = getDevMediaHost();
  if (!host) return undefined;

  const lower = host.toLowerCase();
  if (lower === "localhost" || lower === "127.0.0.1") return undefined;

  if (!isPrivateLanHost(host)) return undefined;

  return `http://${host}:9000`;
}
