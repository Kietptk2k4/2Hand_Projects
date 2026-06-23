import * as SecureStore from "expo-secure-store";
import { CHECKOUT_IDEMPOTENCY_STORAGE_KEY } from "../constants/checkoutConstants";

export async function getOrCreateCheckoutIdempotencyKey() {
  try {
    let key = await SecureStore.getItemAsync(CHECKOUT_IDEMPOTENCY_STORAGE_KEY);
    if (!key) {
      key = globalThis.crypto?.randomUUID?.() || `${Date.now()}-${Math.random()}`;
      await SecureStore.setItemAsync(CHECKOUT_IDEMPOTENCY_STORAGE_KEY, key);
    }
    return key;
  } catch (error) {
    if (__DEV__) {
      console.warn("[checkout-idempotency] SecureStore failed, using ephemeral key", {
        storageKey: CHECKOUT_IDEMPOTENCY_STORAGE_KEY,
        message: error?.message ?? String(error),
      });
    }
    return globalThis.crypto?.randomUUID?.() || `${Date.now()}-${Math.random()}`;
  }
}

export async function clearCheckoutIdempotencyKey() {
  try {
    await SecureStore.deleteItemAsync(CHECKOUT_IDEMPOTENCY_STORAGE_KEY);
  } catch (error) {
    if (__DEV__) {
      console.warn("[checkout-idempotency] clear failed", {
        storageKey: CHECKOUT_IDEMPOTENCY_STORAGE_KEY,
        message: error?.message ?? String(error),
      });
    }
  }
}