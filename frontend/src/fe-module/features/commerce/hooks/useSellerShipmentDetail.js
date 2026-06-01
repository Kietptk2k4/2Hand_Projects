import { useCallback, useEffect, useRef, useState } from "react";
import {
  fetchSellerShipmentDetail,
  updateSellerShipment,
} from "../api/sellerShipmentApi";
import { mapSellerShipmentApiError } from "../constants/sellerShipmentConstants";
import {
  mapSellerShipmentDetail,
  mapUpdateShipmentPayload,
} from "../utils/sellerShipmentMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

function isNotFoundError(error) {
  return String(error?.code ?? "") === "COMMERCE-404-SHIPMENT";
}

export function useSellerShipmentDetail(shipmentId) {
  const { showSessionExpired } = useAuthSession();
  const [detail, setDetail] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isUpdating, setIsUpdating] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [updateError, setUpdateError] = useState("");
  const [isNotFound, setIsNotFound] = useState(false);
  const requestIdRef = useRef(0);

  const load = useCallback(async () => {
    if (!shipmentId) return;

    const requestId = ++requestIdRef.current;
    setIsLoading(true);
    setErrorMessage("");
    setIsNotFound(false);

    try {
      const raw = await fetchSellerShipmentDetail(shipmentId);
      if (requestId !== requestIdRef.current) return;
      setDetail(mapSellerShipmentDetail(raw));
    } catch (error) {
      if (requestId !== requestIdRef.current) return;

      if (isUnauthorizedError(error)) {
        showSessionExpired(error?.message);
        return;
      }

      if (isNotFoundError(error)) {
        setIsNotFound(true);
        setDetail(null);
        return;
      }

      setErrorMessage(mapSellerShipmentApiError(error));
    } finally {
      if (requestId === requestIdRef.current) {
        setIsLoading(false);
      }
    }
  }, [shipmentId, showSessionExpired]);

  useEffect(() => {
    load();
  }, [load]);

  const patchShipment = useCallback(
    async ({ status, trackingNumber }) => {
      if (!shipmentId) return null;

      setIsUpdating(true);
      setUpdateError("");

      try {
        const raw = await updateSellerShipment(
          shipmentId,
          mapUpdateShipmentPayload({ status, trackingNumber }),
        );
        const mapped = mapSellerShipmentDetail(raw);
        setDetail(mapped);
        return mapped;
      } catch (error) {
        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
          throw error;
        }
        setUpdateError(mapSellerShipmentApiError(error));
        return null;
      } finally {
        setIsUpdating(false);
      }
    },
    [shipmentId, showSessionExpired],
  );

  const clearUpdateError = useCallback(() => setUpdateError(""), []);

  return {
    detail,
    isLoading,
    isUpdating,
    errorMessage,
    updateError,
    isNotFound,
    retry: load,
    refresh: load,
    patchShipment,
    clearUpdateError,
  };
}
