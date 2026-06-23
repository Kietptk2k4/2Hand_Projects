import * as Linking from "expo-linking";

import { resolveDevServiceBaseUrl } from "../../../services/http/resolveDevServiceBaseUrl";

export function getVnpayFrontendReturnUrl() {
  return Linking.createURL("commerce/checkout/vnpay-return");
}

export function getVnpayBackendReturnUrl() {
  const baseUrl = resolveDevServiceBaseUrl(
    "commerce",
    process.env.EXPO_PUBLIC_COMMERCE_SERVICE_BASE_URL
  );
  return `${baseUrl}/commerce/api/v1/payments/vnpay/return`;
}

export function buildVnpayCheckoutPayload() {
  return {
    frontend_return_url: getVnpayFrontendReturnUrl(),
    vnpay_return_url: getVnpayBackendReturnUrl(),
  };
}
