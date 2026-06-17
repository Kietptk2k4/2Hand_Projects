import { SESSION_EXPIRED_DEFAULT_MESSAGE } from "../constants/authUiStrings";

let verifyEmailAddress = "";
let loginBannerMessage = "";
let sessionExpiredMessage = "";

export function setVerifyEmailAddress(email) {
  verifyEmailAddress = email || "";
}

export function consumeVerifyEmailAddress() {
  const value = verifyEmailAddress;
  verifyEmailAddress = "";
  return value;
}

export function peekVerifyEmailAddress() {
  return verifyEmailAddress;
}

export function setLoginBannerMessage(message) {
  loginBannerMessage = message || "";
}

export function consumeLoginBannerMessage() {
  const value = loginBannerMessage;
  loginBannerMessage = "";
  return value;
}

export function setSessionExpiredMessage(message) {
  sessionExpiredMessage = message || SESSION_EXPIRED_DEFAULT_MESSAGE;
}

export function consumeSessionExpiredMessage() {
  const value = sessionExpiredMessage || SESSION_EXPIRED_DEFAULT_MESSAGE;
  sessionExpiredMessage = "";
  return value;
}