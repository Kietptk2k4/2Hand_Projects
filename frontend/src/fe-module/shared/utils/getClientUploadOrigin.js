const LOCAL_HOSTS = new Set(["localhost", "127.0.0.1"]);
const MINIO_PORT = "9000";
const TWO_HANDS_BUCKET_SEGMENT = /\/2hands-(avatar|social-post|commerce-product|commerce-review|commerce-shop)\//;
const REWRITABLE_MINIO_HOSTS = new Set(["localhost", "127.0.0.1", "10.0.2.2"]);

function isPrivateLanHost(host) {
  if (!host) return false;
  const normalized = host.toLowerCase();
  if (REWRITABLE_MINIO_HOSTS.has(normalized)) return true;
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
 * Host used to reach MinIO from the current browser session (dev only).
 */
export function getDevMediaHost() {
  if (!import.meta.env.DEV) {
    return null;
  }

  const host = typeof window !== "undefined" ? window.location.hostname : "";
  if (!host || LOCAL_HOSTS.has(host.toLowerCase())) {
    return "localhost";
  }

  return host;
}

/**
 * Rewrites local MinIO media URLs to match the current dev environment host.
 * In production builds served via dev-gateway (ngrok), rewrites dev-origin object URLs
 * to same-origin so remote clients can load media without DB migrations.
 */
export function resolveDevMediaUrl(url) {
  if (!url || typeof url !== "string") {
    return url;
  }

  const trimmed = url.trim();
  if (!trimmed) {
    return trimmed;
  }

  if (!import.meta.env.DEV) {
    return rewriteStoredMediaToSameOrigin(trimmed);
  }

  const devHost = getDevMediaHost();
  if (!devHost) {
    return trimmed;
  }

  try {
    const parsed = new URL(trimmed);
    if (!TWO_HANDS_BUCKET_SEGMENT.test(parsed.pathname)) {
      return trimmed;
    }
    if (!isPrivateLanHost(parsed.hostname)) {
      return trimmed;
    }
    parsed.hostname = devHost;
    parsed.port = MINIO_PORT;
    return parsed.toString();
  } catch {
    return trimmed;
  }
}

function rewriteStoredMediaToSameOrigin(url) {
  if (typeof window === "undefined") {
    return url;
  }
  try {
    const parsed = new URL(url);
    if (!TWO_HANDS_BUCKET_SEGMENT.test(parsed.pathname)) {
      return url;
    }
    if (!isPrivateLanHost(parsed.hostname)) {
      return url;
    }
    return `${window.location.origin}${parsed.pathname}${parsed.search}${parsed.hash}`;
  } catch {
    return url;
  }
}

/**
 * Dev-only MinIO origin for presigned PUT (must match signature host).
 * Omit on localhost - server presigns with default localhost.
 * Omit on public gateway (ngrok): server uses *_MINIO_PRESIGNED_ENDPOINT instead.
 */
export function getClientUploadOrigin() {
  if (!import.meta.env.DEV) {
    return undefined;
  }

  const host = typeof window !== "undefined" ? window.location.hostname : "";
  if (!host || LOCAL_HOSTS.has(host.toLowerCase())) {
    return undefined;
  }

  if (!isPrivateLanHost(host)) {
    return undefined;
  }

  return `http://${host}:9000`;
}

export function withClientUploadOrigin(body) {
  const clientUploadOrigin = getClientUploadOrigin();
  if (!clientUploadOrigin) {
    return body;
  }
  return { ...body, client_upload_origin: clientUploadOrigin };
}