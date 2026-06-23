import * as Linking from "expo-linking";
import { resolveDevServiceBaseUrl } from "../../../services/http/resolveDevServiceBaseUrl";

export function getOAuthAppReturnUrl(kind = "success") {
  const path = kind === "failure" ? "oauth/failure" : "oauth/success";
  return Linking.createURL(path);
}

export function getAuthServiceBaseUrl() {
  return resolveDevServiceBaseUrl("auth", process.env.EXPO_PUBLIC_AUTH_SERVICE_BASE_URL);
}

export function getOAuthMobileBridgeUrl(appReturnUrl = getOAuthAppReturnUrl("success")) {
  const baseUrl = getAuthServiceBaseUrl();
  const params = new URLSearchParams({ app_return: appReturnUrl });
  return `${baseUrl}/api/v1/auth/oauth/mobile-complete?${params.toString()}`;
}

export function getOAuthAuthorizationUrl(provider) {
  const providers = {
    google: "/oauth2/authorization/google",
    facebook: "/oauth2/authorization/facebook",
  };

  const endpoint = providers[provider];
  if (!endpoint) {
    return "";
  }

  const bridgeUrl = getOAuthMobileBridgeUrl();
  const params = new URLSearchParams({ redirect_uri: bridgeUrl });
  return `${getAuthServiceBaseUrl()}${endpoint}?${params.toString()}`;
}
