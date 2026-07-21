const NGROK_HOST_PATTERNS = [
  /\.ngrok-free\.dev$/i,
  /\.ngrok-free\.app$/i,
  /\.ngrok\.io$/i,
];

export const NGROK_SKIP_BROWSER_WARNING = "ngrok-skip-browser-warning";

/**
 * ngrok free tier returns an HTML browser warning unless this header is sent.
 * Required for Vite dev (localhost) calling APIs via the ngrok gateway URL.
 */
export function isNgrokGatewayUrl(url) {
  if (!url || typeof url !== "string") {
    return false;
  }
  try {
    const hostname = new URL(url).hostname;
    return NGROK_HOST_PATTERNS.some((pattern) => pattern.test(hostname));
  } catch {
    return false;
  }
}

export function resolveRequestUrl(config) {
  const requestUrl = config?.url || "";
  if (requestUrl.startsWith("http://") || requestUrl.startsWith("https://")) {
    return requestUrl;
  }
  const baseUrl = config?.baseURL || "";
  if (!baseUrl) {
    return requestUrl;
  }
  return `${baseUrl.replace(/\/$/, "")}/${requestUrl.replace(/^\//, "")}`;
}

export function applyNgrokGatewayHeaders(config) {
  if (!isNgrokGatewayUrl(resolveRequestUrl(config))) {
    return config;
  }
  config.headers = config.headers || {};
  config.headers[NGROK_SKIP_BROWSER_WARNING] = "true";
  return config;
}

export function ngrokGatewayHeaderObject(baseUrl) {
  if (!isNgrokGatewayUrl(baseUrl)) {
    return {};
  }
  return { [NGROK_SKIP_BROWSER_WARNING]: "true" };
}

export function attachNgrokGatewayInterceptor(axiosInstance) {
  axiosInstance.interceptors.request.use(applyNgrokGatewayHeaders);
}
