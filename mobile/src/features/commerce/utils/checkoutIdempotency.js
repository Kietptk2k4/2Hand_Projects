import * as SecureStore from "expo-secure-store";
import { CHECKOUT_IDEMPOTENCY_STORAGE_KEY } from "../constants/checkoutConstants";

export async function getOrCreateCheckoutIdempotencyKey() {
  let key = await SecureStore.getItemAsync(CHECKOUT_IDEMPOTENCY_STORAGE_KEY);
  if (!key) {
    key = globalThis.crypto?.randomUUID?.() || `${Date.now()}-${Math.random()}`;
    await SecureStore.setItemAsync(CHECKOUT_IDEMPOTENCY_STORAGE_KEY, key);
  }
  return key;
}

export async function clearCheckoutIdempotencyKey() {
  await SecureStore.deleteItemAsync(CHECKOUT_IDEMPOTENCY_STORAGE_KEY);
}