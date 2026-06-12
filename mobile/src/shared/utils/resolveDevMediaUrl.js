import { getDevMediaHost } from "./getDevMediaHost";

const LOCAL_HOSTS = ["localhost", "127.0.0.1"];

function shouldRewriteHost(host) {
  const devHost = getDevMediaHost();
  return devHost !== "localhost" && LOCAL_HOSTS.includes(host.toLowerCase());
}

export function resolveDevMediaUrl(url) {
  if (!url || typeof url !== "string") return url;

  const trimmed = url.trim();
  if (!trimmed) return url;

  if (!/^https?:\/\//i.test(trimmed)) {
    return trimmed;
  }

  try {
    const parsed = new URL(trimmed);
    if (!shouldRewriteHost(parsed.hostname)) {
      return trimmed;
    }

    const devHost = getDevMediaHost();
    parsed.hostname = devHost;
    return parsed.toString().replace(/\/$/, "");
  } catch {
    const devHost = getDevMediaHost();
    if (devHost === "localhost") return trimmed;

    return trimmed
      .replace(/^http:\/\/localhost/i, `http://${devHost}`)
      .replace(/^http:\/\/127\.0\.0\.1/i, `http://${devHost}`)
      .replace(/^https:\/\/localhost/i, `https://${devHost}`)
      .replace(/^https:\/\/127\.0\.0\.1/i, `https://${devHost}`);
  }
}