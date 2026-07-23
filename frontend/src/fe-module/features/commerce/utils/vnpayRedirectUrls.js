import { APP_ROUTES } from "../../../shared/constants/routes";
import { resolveServiceBaseUrl } from "../../../services/http/resolveServiceBaseUrl";

function trimTrailingSlash(value) {
  if (!value) return "";
  return value.endsWith("/") ? value.slice(0, -1) : value;
}

/**
 * FE return page after VNPay — always matches the browser origin (localhost, ngrok, LAN, etc.).
 */
export function getVnpayFrontendReturnUrl() {
  if (typeof window === "undefined") {
    return undefined;
  }
  const origin = trimTrailingSlash(window.location.origin);
  if (!origin) {
    return undefined;
  }
  return `${origin}${APP_ROUTES.commerceCheckoutVnpayReturn}`;
}

/**
 * Commerce API callback that VNPay redirects to — follows VITE_COMMERCE_SERVICE_BASE_URL
 * (localhost:3003, ngrok gateway, or any future host).
 */
export function getVnpayBackendReturnUrl() {
  const baseUrl = trimTrailingSlash(
    resolveServiceBaseUrl(import.meta.env.VITE_COMMERCE_SERVICE_BASE_URL)
  );
  if (!baseUrl) {
    return undefined;
  }
  return `${baseUrl}/commerce/api/v1/payments/vnpay/return`;
}

export function buildVnpayCheckoutPayload() {
  const payload = {};
  const frontendReturnUrl = getVnpayFrontendReturnUrl();
  const vnpayReturnUrl = getVnpayBackendReturnUrl();
  if (frontendReturnUrl) {
    payload.frontend_return_url = frontendReturnUrl;
  }
  if (vnpayReturnUrl) {
    payload.vnpay_return_url = vnpayReturnUrl;
  }
  return payload;
}
