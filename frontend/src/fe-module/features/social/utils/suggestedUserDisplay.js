import { DEFAULT_USER_DISPLAY_NAME } from "../constants/socialUiStrings";
import { authorAvatarUrl } from "./authorDisplay";

const PLACEHOLDER_NAME_PATTERN = /^User [a-f0-9]{8}$/i;
const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

export function isPlaceholderDisplayName(name, userId) {
  if (!name || !String(name).trim()) {
    return true;
  }

  const trimmed = String(name).trim();
  if (PLACEHOLDER_NAME_PATTERN.test(trimmed)) {
    return true;
  }
  if (UUID_PATTERN.test(trimmed)) {
    return true;
  }
  if (userId && trimmed === userId) {
    return true;
  }

  return false;
}

export function resolveSuggestedDisplayName(name, userId) {
  if (!isPlaceholderDisplayName(name, userId)) {
    return String(name).trim();
  }
  return DEFAULT_USER_DISPLAY_NAME;
}

export function resolveSuggestedAvatarUrl(userId, avatarUrl) {
  if (avatarUrl && String(avatarUrl).trim()) {
    return String(avatarUrl).trim();
  }
  return authorAvatarUrl(userId);
}