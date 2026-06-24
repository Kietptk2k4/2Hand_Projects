import { getDevMediaHost } from "./getDevMediaHost";
import { logMediaUrlRewrite } from "./debugMediaLog";

const MINIO_PORT = "9000";
const TWO_HANDS_BUCKET_SEGMENT = /\/2hands-(avatar|social-post|commerce-product|commerce-review|commerce-shop)\//;
const LOCAL_LOOPBACK_HOSTS = new Set(["localhost", "127.0.0.1", "10.0.2.2"]);

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

function isDevMinioObjectUrl(parsed) {
  if (parsed.port !== MINIO_PORT) {
    return false;
  }
  return TWO_HANDS_BUCKET_SEGMENT.test(parsed.pathname);
}

function rewriteMinioHost(url, devHost) {
  try {
    const parsed = new URL(url);
    if (!isDevMinioObjectUrl(parsed)) {
      return url;
    }
    if (!isPrivateLanHost(parsed.hostname)) {
      return url;
    }
    parsed.hostname = devHost;
    parsed.port = MINIO_PORT;
    return parsed.toString();
  } catch {
    return url;
  }
}

function resolveUrl(trimmed, devHost) {
  if (devHost === "localhost") {
    return rewriteMinioHost(trimmed, devHost);
  }

  if (/^https?:\/\//i.test(trimmed)) {
    return rewriteMinioHost(trimmed, devHost);
  }

  if (trimmed.startsWith("//")) {
    return rewriteMinioHost(`http:${trimmed}`, devHost).replace(/^http:/i, "");
  }

  if (trimmed.startsWith("/")) {
    return `http://${devHost}:${MINIO_PORT}${trimmed}`;
  }

  return trimmed;
}

export function resolveDevMediaUrl(url) {
  if (!url || typeof url !== "string") return url;

  const trimmed = url.trim();
  if (!trimmed) return url;

  const devHost = getDevMediaHost();
  const resolved = resolveUrl(trimmed, devHost);

  logMediaUrlRewrite({ devHost, raw: trimmed, resolved });

  return resolved;
}
