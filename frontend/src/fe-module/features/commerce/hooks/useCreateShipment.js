import { useCallback, useEffect, useMemo, useState } from "react";
import { fetchSellerOrderList } from "../api/sellerOrderApi";
import { createSellerShipment } from "../api/sellerShipmentApi";
import { mapSellerOrderApiError } from "../constants/sellerOrderConstants";
import { mapSellerShipmentApiError } from "../constants/sellerShipmentConstants";
import { mapSellerOrderListResponse } from "../utils/sellerOrderMapper";
import {
  mapCreateShipmentPayload,
  mapCreateShipmentResponse,
} from "../utils/sellerShipmentMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

function isEligibleItem(item) {
  if (item.itemStatus !== "PROCESSING") return false;
  if (item.shipmentSummary?.shipmentId) return false;
  if (item.orderStatus !== "PROCESSING") return false;

  if (item.orderPaymentMethod === "PAYOS") {
    return item.orderPaymentStatus === "PAID";
  }

  if (item.orderPaymentMethod === "COD") {
    return item.orderPaymentStatus === "PAID" || item.orderPaymentStatus === "PENDING";
  }

  return item.orderPaymentStatus === "PAID";
}

function sumSelectedLineWeight(items, selectedItemIds) {
  let total = 0;
  for (const item of items) {
    if (!selectedItemIds.has(item.orderItemId)) continue;
    total += item.lineWeightGram ?? 0;
  }
  return total;
}

export function useCreateShipment({ open, prefillOrderId, prefillOrderItemIds }) {
  const { showSessionExpired } = useAuthSession();
  const [eligibleItems, setEligibleItems] = useState([]);
  const [isLoadingOrders, setIsLoadingOrders] = useState(false);
  const [loadError, setLoadError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");

  const [orderId, setOrderId] = useState("");
  const [selectedItemIds, setSelectedItemIds] = useState(() => new Set());
  const [carrier, setCarrier] = useState("GHN");
  const [shipmentType, setShipmentType] = useState("STANDARD");
  const [useWeightOverride, setUseWeightOverride] = useState(false);
  const [weightOverrideGram, setWeightOverrideGram] = useState("");
  const [trackingNumber, setTrackingNumber] = useState("");

  const loadEligible = useCallback(async () => {
    setIsLoadingOrders(true);
    setLoadError("");

    try {
      const raw = await fetchSellerOrderList({
        page: 1,
        limit: 50,
        status: "PROCESSING",
      });
      const data = mapSellerOrderListResponse(raw);
      setEligibleItems(data.items.filter(isEligibleItem));
    } catch (error) {
      if (isUnauthorizedError(error)) {
        showSessionExpired(error?.message);
        return;
      }
      setLoadError(mapSellerOrderApiError(error));
    } finally {
      setIsLoadingOrders(false);
    }
  }, [showSessionExpired]);

  useEffect(() => {
    if (!open) return;
    loadEligible();
  }, [open, loadEligible]);

  useEffect(() => {
    if (!open) return;

    if (prefillOrderId) {
      setOrderId(prefillOrderId);
    }

    if (prefillOrderItemIds?.length) {
      setSelectedItemIds(new Set(prefillOrderItemIds));
    }
  }, [open, prefillOrderId, prefillOrderItemIds]);

  const ordersGrouped = useMemo(() => {
    const map = new Map();
    for (const item of eligibleItems) {
      if (!map.has(item.orderId)) map.set(item.orderId, []);
      map.get(item.orderId).push(item);
    }
    return map;
  }, [eligibleItems]);

  const orderOptions = useMemo(() => [...ordersGrouped.keys()], [ordersGrouped]);

  const itemsForSelectedOrder = useMemo(() => {
    if (!orderId) return [];
    return ordersGrouped.get(orderId) || [];
  }, [orderId, ordersGrouped]);

  const estimatedWeightGram = useMemo(
    () => sumSelectedLineWeight(eligibleItems, selectedItemIds),
    [eligibleItems, selectedItemIds],
  );

  const toggleItem = useCallback((itemId) => {
    setSelectedItemIds((prev) => {
      const next = new Set(prev);
      if (next.has(itemId)) next.delete(itemId);
      else next.add(itemId);
      return next;
    });
  }, []);

  const handleOrderChange = useCallback((nextOrderId) => {
    setOrderId(nextOrderId);
    setSelectedItemIds(new Set());
    setUseWeightOverride(false);
    setWeightOverrideGram("");
  }, []);

  const showTrackingField = carrier === "MANUAL" || carrier === "SELF_DELIVERY";

  const resetForm = useCallback(() => {
    setOrderId("");
    setSelectedItemIds(new Set());
    setCarrier("GHN");
    setShipmentType("STANDARD");
    setUseWeightOverride(false);
    setWeightOverrideGram("");
    setTrackingNumber("");
    setSubmitError("");
  }, []);

  const submit = useCallback(async () => {
    const orderItemIds = [...selectedItemIds];
    if (!orderId || orderItemIds.length === 0) {
      setSubmitError("Chọn đơn và ít nhất một mục hàng.");
      return null;
    }

    if (useWeightOverride) {
      const parsed = Number(weightOverrideGram);
      if (!Number.isFinite(parsed) || parsed < 1) {
        setSubmitError("Khối lượng ghi đè phải là số nguyên dương (gram).");
        return null;
      }
    }

    setIsSubmitting(true);
    setSubmitError("");

    try {
      const raw = await createSellerShipment(
        mapCreateShipmentPayload({
          orderId,
          orderItemIds,
          carrier,
          shipmentType,
          weightGram: useWeightOverride ? weightOverrideGram : "",
          trackingNumber: showTrackingField ? trackingNumber : "",
        }),
      );
      return mapCreateShipmentResponse(raw);
    } catch (error) {
      if (isUnauthorizedError(error)) {
        showSessionExpired(error?.message);
        throw error;
      }
      setSubmitError(mapSellerShipmentApiError(error));
      return null;
    } finally {
      setIsSubmitting(false);
    }
  }, [
    carrier,
    orderId,
    selectedItemIds,
    shipmentType,
    showTrackingField,
    showSessionExpired,
    trackingNumber,
    useWeightOverride,
    weightOverrideGram,
  ]);

  return {
    eligibleItems,
    orderOptions,
    ordersGrouped,
    orderId,
    setOrderId: handleOrderChange,
    itemsForSelectedOrder,
    selectedItemIds,
    toggleItem,
    carrier,
    setCarrier,
    shipmentType,
    setShipmentType,
    estimatedWeightGram,
    useWeightOverride,
    setUseWeightOverride,
    weightOverrideGram,
    setWeightOverrideGram,
    trackingNumber,
    setTrackingNumber,
    showTrackingField,
    isLoadingOrders,
    loadError,
    isSubmitting,
    submitError,
    submit,
    resetForm,
    reloadEligible: loadEligible,
  };
}
