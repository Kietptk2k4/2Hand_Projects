import { useCallback, useEffect, useRef, useState } from "react";
import { fetchCheckoutQuote, fetchShippingFee, submitCheckout } from "../api/checkoutApi";
import { createUserAddress, fetchUserAddresses } from "../api/userAddressApi";
import {
  CHECKOUT_IDEMPOTENCY_STORAGE_KEY,
  DEFAULT_PAYMENT_METHOD,
  DEFAULT_SHIPMENT_TYPE,
} from "../constants/checkoutConstants";
import {
  mapAddressesResponse,
  mapCreateAddressResponse,
  toCreateAddressPayload,
} from "../utils/addressMapper";
import {
  mapCheckoutResponse,
  mapQuoteResponse,
  mapShippingFeeResponse,
} from "../utils/checkoutMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { useValidateCartItems } from "./useValidateCartItems";

const CHECKOUT_VALIDATE_ERROR =
  "Một hoặc nhiều sản phẩm không còn hợp lệ. Vui lòng quay lại giỏ hàng.";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401");
}

function getOrCreateIdempotencyKey() {
  let key = sessionStorage.getItem(CHECKOUT_IDEMPOTENCY_STORAGE_KEY);
  if (!key) {
    key = crypto.randomUUID();
    sessionStorage.setItem(CHECKOUT_IDEMPOTENCY_STORAGE_KEY, key);
  }
  return key;
}

export function clearCheckoutIdempotencyKey() {
  sessionStorage.removeItem(CHECKOUT_IDEMPOTENCY_STORAGE_KEY);
}

export function useCheckout(cartItemIds) {
  const { showSessionExpired } = useAuthSession();
  const { validate } = useValidateCartItems();

  const [addresses, setAddresses] = useState([]);
  const [selectedAddressId, setSelectedAddressId] = useState(null);
  const [shipmentType, setShipmentType] = useState(DEFAULT_SHIPMENT_TYPE);
  const [paymentMethod, setPaymentMethod] = useState(DEFAULT_PAYMENT_METHOD);
  const [quote, setQuote] = useState(null);
  const [shippingFee, setShippingFee] = useState(null);
  const [status, setStatus] = useState("idle");
  const [quoteError, setQuoteError] = useState("");
  const [submitError, setSubmitError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [addressesLoaded, setAddressesLoaded] = useState(false);
  const [isCreatingAddress, setIsCreatingAddress] = useState(false);

  const debounceRef = useRef(null);
  const requestIdRef = useRef(0);

  const loadAddresses = useCallback(
    async ({ selectAddressId } = {}) => {
      setStatus("loadingAddresses");
      try {
        const raw = await fetchUserAddresses();
        const list = mapAddressesResponse(raw);
        setAddresses(list);

        if (selectAddressId && list.some((item) => item.id === selectAddressId)) {
          setSelectedAddressId(selectAddressId);
        } else {
          const defaultAddress = list.find((item) => item.isDefault) || list[0];
          setSelectedAddressId(defaultAddress?.id || null);
        }

        setAddressesLoaded(true);
        setStatus("ready");
        return list;
      } catch (error) {
        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
          return [];
        }
        setQuoteError(error?.message || "Không tải được địa chỉ.");
        setAddressesLoaded(true);
        setStatus("error");
        return [];
      }
    },
    [showSessionExpired]
  );

  useEffect(() => {
    loadAddresses();
  }, [loadAddresses]);

  const refreshQuote = useCallback(async () => {
    if (!cartItemIds?.length || !selectedAddressId) {
      setQuote(null);
      setShippingFee(null);
      return;
    }

    const requestId = ++requestIdRef.current;
    setStatus("loadingQuote");
    setQuoteError("");

    try {
      const validation = await validate(cartItemIds);
      if (requestId !== requestIdRef.current) return;

      if (!validation?.canCheckout) {
        setQuote(null);
        setShippingFee(null);
        setQuoteError(CHECKOUT_VALIDATE_ERROR);
        setStatus("ready");
        return;
      }

      const [quoteRaw, shippingRaw] = await Promise.all([
        fetchCheckoutQuote({
          cartItemIds,
          addressId: selectedAddressId,
          shipmentType,
        }),
        fetchShippingFee({
          cartItemIds,
          addressId: selectedAddressId,
          shipmentType,
        }),
      ]);

      if (requestId !== requestIdRef.current) return;

      setQuote(mapQuoteResponse(quoteRaw));
      setShippingFee(mapShippingFeeResponse(shippingRaw));
      setStatus("ready");
    } catch (error) {
      if (requestId !== requestIdRef.current) return;

      if (isUnauthorizedError(error)) {
        showSessionExpired(error?.message);
        return;
      }

      setQuote(null);
      setShippingFee(null);
      setQuoteError(error?.message || "Không tính được tổng tiền. Vui lòng thử lại.");
      setStatus("ready");
    }
  }, [cartItemIds, selectedAddressId, shipmentType, showSessionExpired, validate]);

  useEffect(() => {
    if (!addressesLoaded || !selectedAddressId || !cartItemIds?.length) return;

    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
    }

    debounceRef.current = setTimeout(() => {
      refreshQuote();
    }, 300);

    return () => {
      if (debounceRef.current) clearTimeout(debounceRef.current);
    };
  }, [addressesLoaded, selectedAddressId, shipmentType, cartItemIds, refreshQuote]);

  const selectAddress = useCallback((addressId) => {
    setSelectedAddressId(addressId);
  }, []);

  const selectShipment = useCallback((nextType) => {
    setShipmentType(nextType);
  }, []);

  const selectPayment = useCallback((nextMethod) => {
    setPaymentMethod(nextMethod);
  }, []);

  const createAddress = useCallback(
    async (formValues) => {
      setIsCreatingAddress(true);
      try {
        const raw = await createUserAddress(toCreateAddressPayload(formValues));
        const created = mapCreateAddressResponse(raw);
        await loadAddresses({ selectAddressId: created.id });
        return created;
      } catch (error) {
        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
          throw error;
        }
        throw error;
      } finally {
        setIsCreatingAddress(false);
      }
    },
    [loadAddresses, showSessionExpired]
  );

  const submitOrder = useCallback(async () => {
    if (!cartItemIds?.length || !selectedAddressId || !quote) return null;

    setIsSubmitting(true);
    setSubmitError("");

    try {
      const idempotencyKey = getOrCreateIdempotencyKey();
      const raw = await submitCheckout({
        cartItemIds,
        addressId: selectedAddressId,
        paymentMethod,
        shipmentType,
        idempotencyKey,
      });
      const result = mapCheckoutResponse(raw);
      clearCheckoutIdempotencyKey();
      return result;
    } catch (error) {
      if (isUnauthorizedError(error)) {
        showSessionExpired(error?.message);
        return null;
      }
      setSubmitError(error?.message || "Không thể đặt hàng. Vui lòng thử lại.");
      return null;
    } finally {
      setIsSubmitting(false);
    }
  }, [
    cartItemIds,
    paymentMethod,
    quote,
    selectedAddressId,
    shipmentType,
    showSessionExpired,
  ]);

  const isLoadingAddresses = status === "loadingAddresses";
  const isLoadingQuote = status === "loadingQuote";

  return {
    addresses,
    selectedAddressId,
    shipmentType,
    paymentMethod,
    quote,
    shippingFee,
    status,
    quoteError,
    submitError,
    isSubmitting,
    isLoadingAddresses,
    isLoadingQuote,
    hasAddresses: addresses.length > 0,
    isEmptyAddresses: addressesLoaded && addresses.length === 0,
    canSubmit:
      Boolean(selectedAddressId && quote && cartItemIds?.length) &&
      !isSubmitting &&
      !isLoadingQuote &&
      !quoteError,
    selectAddress,
    selectShipment,
    selectPayment,
    refreshQuote,
    submitOrder,
    createAddress,
    reloadAddresses: loadAddresses,
    isCreatingAddress,
  };
}
