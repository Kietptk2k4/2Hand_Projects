import { useCallback, useEffect, useRef, useState } from "react";
import { fetchCheckoutQuote, fetchShippingFee, submitCheckout } from "../api/checkoutApi";
import {
  CHECKOUT_COD_ONLY_ENABLED,
  DEFAULT_PAYMENT_METHOD,
  DEFAULT_SHIPMENT_TYPE,
} from "../constants/checkoutConstants";
import {
  mapCheckoutResponse,
  mapQuoteResponse,
  mapShippingFeeResponse,
} from "../utils/checkoutMapper";
import {
  clearCheckoutIdempotencyKey,
  getOrCreateCheckoutIdempotencyKey,
} from "../utils/checkoutIdempotency";
import { buildVnpayCheckoutPayload } from "../utils/vnpayRedirectUrls";
import { useAddresses } from "./useAddresses";
import { useValidateCartItems } from "./useValidateCartItems";

const CHECKOUT_VALIDATE_ERROR =
  "Một hoặc nhiều sản phẩm không còn hợp lệ. Vui lòng quay lại giỏ hàng.";

export function useCheckout(cartItemIds) {
  const { validate } = useValidateCartItems();
  const {
    addresses,
    labelVersion: addressLabelVersion,
    isLoading: isLoadingAddresses,
    isFetching: isFetchingAddresses,
    errorMessage: addressErrorMessage,
    isEmpty: isEmptyAddresses,
    retry: reloadAddresses,
  } = useAddresses();

  const [selectedAddressId, setSelectedAddressId] = useState(null);
  const [paymentMethod, setPaymentMethod] = useState(DEFAULT_PAYMENT_METHOD);
  const [quote, setQuote] = useState(null);
  const [shippingFee, setShippingFee] = useState(null);
  const [status, setStatus] = useState("idle");
  const [quoteError, setQuoteError] = useState("");
  const [submitError, setSubmitError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const debounceRef = useRef(null);
  const requestIdRef = useRef(0);
  const submitLockRef = useRef(false);
  const selectedInitializedRef = useRef(false);

  useEffect(() => {
    if (isLoadingAddresses || isFetchingAddresses) return;

    if (!addresses.length) {
      setSelectedAddressId(null);
      selectedInitializedRef.current = false;
      return;
    }

    if (
      !selectedAddressId ||
      !addresses.some((item) => item.id === selectedAddressId) ||
      !selectedInitializedRef.current
    ) {
      const defaultAddress = addresses.find((item) => item.isDefault) || addresses[0];
      setSelectedAddressId(defaultAddress?.id || null);
      selectedInitializedRef.current = true;
    }
  }, [addresses, isFetchingAddresses, isLoadingAddresses, selectedAddressId]);

  useEffect(() => {
    if (addressErrorMessage) {
      setQuoteError(addressErrorMessage);
    }
  }, [addressErrorMessage]);

  const refreshQuote = useCallback(async () => {
    if (!cartItemIds?.length || !selectedAddressId) {
      setQuote(null);
      setShippingFee(null);
      return;
    }

    const requestId = ++requestIdRef.current;
    setStatus("loadingQuote");
    setQuoteError(addressErrorMessage || "");

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
          shipmentType: DEFAULT_SHIPMENT_TYPE,
        }),
        fetchShippingFee({
          cartItemIds,
          addressId: selectedAddressId,
          shipmentType: DEFAULT_SHIPMENT_TYPE,
        }),
      ]);

      if (requestId !== requestIdRef.current) return;

      setQuote(mapQuoteResponse(quoteRaw));
      setShippingFee(mapShippingFeeResponse(shippingRaw));
      setStatus("ready");
    } catch (error) {
      if (requestId !== requestIdRef.current) return;

      setQuote(null);
      setShippingFee(null);
      setQuoteError(error?.message || "Không tính được tổng tiền. Vui lòng thử lại.");
      setStatus("ready");
    }
  }, [addressErrorMessage, cartItemIds, selectedAddressId, validate]);

  useEffect(() => {
    if (isLoadingAddresses || !selectedAddressId || !cartItemIds?.length) return;

    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
    }

    debounceRef.current = setTimeout(() => {
      refreshQuote();
    }, 300);

    return () => {
      if (debounceRef.current) clearTimeout(debounceRef.current);
    };
  }, [isLoadingAddresses, selectedAddressId, cartItemIds, refreshQuote]);

  const selectAddress = useCallback((addressId) => {
    setSelectedAddressId(addressId);
  }, []);

  const selectPayment = useCallback((nextMethod) => {
    if (CHECKOUT_COD_ONLY_ENABLED && nextMethod !== "COD") return;
    setPaymentMethod(nextMethod);
  }, []);

  const submitOrder = useCallback(async () => {
    if (!cartItemIds?.length || !selectedAddressId || !quote) return null;
    if (submitLockRef.current || isSubmitting) return null;

    submitLockRef.current = true;
    setIsSubmitting(true);
    setSubmitError("");

    try {
      const idempotencyKey = await getOrCreateCheckoutIdempotencyKey();
      const effectivePaymentMethod = CHECKOUT_COD_ONLY_ENABLED ? "COD" : paymentMethod;
      const raw = await submitCheckout({
        cartItemIds,
        addressId: selectedAddressId,
        paymentMethod: effectivePaymentMethod,
        shipmentType: DEFAULT_SHIPMENT_TYPE,
        idempotencyKey,
        vnpayReturnUrls:
          effectivePaymentMethod === "VNPAY" ? buildVnpayCheckoutPayload() : undefined,
      });
      const result = mapCheckoutResponse(raw);
      await clearCheckoutIdempotencyKey();
      return result;
    } catch (error) {
      setSubmitError(error?.message || "Không thể đặt hàng. Vui lòng thử lại.");
      return null;
    } finally {
      setIsSubmitting(false);
      submitLockRef.current = false;
    }
  }, [cartItemIds, paymentMethod, quote, selectedAddressId, isSubmitting]);

  const isLoadingQuote = status === "loadingQuote";

  return {
    addresses,
    addressLabelVersion,
    selectedAddressId,
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
    isEmptyAddresses: !isLoadingAddresses && isEmptyAddresses,
    canSubmit:
      Boolean(selectedAddressId && quote && cartItemIds?.length) &&
      !isSubmitting &&
      !isLoadingQuote &&
      !quoteError,
    selectAddress,
    selectPayment,
    refreshQuote,
    submitOrder,
    reloadAddresses,
  };
}