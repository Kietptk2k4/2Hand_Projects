import * as WebBrowser from "expo-web-browser";

import { getVnpayFrontendReturnUrl } from "./vnpayRedirectUrls";

WebBrowser.maybeCompleteAuthSession();

export async function openVnpayBrowser(checkoutUrl) {
  if (!checkoutUrl) return null;
  return WebBrowser.openAuthSessionAsync(checkoutUrl, getVnpayFrontendReturnUrl());
}
