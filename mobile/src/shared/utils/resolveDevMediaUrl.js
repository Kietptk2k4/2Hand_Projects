import { getDevMediaHost } from "./getDevMediaHost";
import { logMediaUrlRewrite } from "./debugMediaLog";

const LOCAL_HOST_IN_URL = /^(https?:\/\/)(localhost|127\.0\.0\.1)(?=:\d+|\/|$)/i;

function rewriteLocalHost(url, devHost) {
  if (!LOCAL_HOST_IN_URL.test(url)) {
    return url;
  }

  return url.replace(LOCAL_HOST_IN_URL, `$1${devHost}`);
}

function resolveUrl(trimmed, devHost) {
  if (devHost === "localhost") {
    return trimmed;
  }

  if (/^https?:\/\//i.test(trimmed)) {
    return rewriteLocalHost(trimmed, devHost);
  }

  if (trimmed.startsWith("//")) {
    return rewriteLocalHost(`http:${trimmed}`, devHost).replace(/^http:/i, "");
  }

  if (trimmed.startsWith("/")) {
    return `http://${devHost}:9000${trimmed}`;
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
