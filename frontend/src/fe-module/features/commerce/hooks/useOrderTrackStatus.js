import { useCallback, useEffect, useRef, useState } from "react";
import { fetchOrderTrackStatus } from "../api/orderTrackApi";
import { ORDER_TRACK_POLL_INTERVAL_MS } from "../constants/orderDetailConstants";
import { buildOrderTimelineEvents } from "../utils/buildOrderTimelineEvents";
import { mapOrderTrackResponse } from "../utils/orderTrackMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

const NOT_FOUND_CODE = "COMMERCE-404-ORDER";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

export function useOrderTrackStatus(orderId, { enabled = true, pollWhileProcessing = true } = {}) {
  const { showSessionExpired } = useAuthSession();
  const [track, setTrack] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const pollRef = useRef(null);

  const refresh = useCallback(async () => {
    if (!orderId || !enabled) return null;

    setStatus((prev) => (prev === "ready" ? "refreshing" : "loading"));
    setErrorMessage("");

    try {
      const raw = await fetchOrderTrackStatus(orderId);
      const mapped = mapOrderTrackResponse(raw);
      setTrack(mapped);
      setStatus("ready");
      return mapped;
    } catch (error) {
      if (isUnauthorizedError(error)) {
        showSessionExpired(error?.message);
        return null;
      }

      if (error?.code === NOT_FOUND_CODE) {
        setStatus("notFound");
        setErrorMessage(error?.message || "Không tìm thấy đơn hàng.");
        return null;
      }

      setStatus("error");
      setErrorMessage(error?.message || "Không tải được trạng thái đơn hàng.");
      return null;
    }
  }, [enabled, orderId, showSessionExpired]);

  useEffect(() => {
    if (!enabled || !orderId) {
      setTrack(null);
      setStatus("idle");
      return;
    }
    refresh();
  }, [enabled, orderId, refresh]);

  useEffect(() => {
    if (pollRef.current) {
      clearInterval(pollRef.current);
      pollRef.current = null;
    }

    if (!pollWhileProcessing || !enabled || !orderId) return undefined;

    const shouldPoll = track?.orderStatus === "PROCESSING" && !track?.orderCompleted;
    if (!shouldPoll) return undefined;

    pollRef.current = setInterval(() => {
      refresh();
    }, ORDER_TRACK_POLL_INTERVAL_MS);

    return () => {
      if (pollRef.current) clearInterval(pollRef.current);
    };
  }, [
    enabled,
    orderId,
    pollWhileProcessing,
    refresh,
    track?.orderCompleted,
    track?.orderStatus,
  ]);

  const timelineEvents = buildOrderTimelineEvents(track);

  return {
    track,
    timelineEvents,
    orderStatus: track?.orderStatus ?? null,
    orderCompleted: Boolean(track?.orderCompleted),
    paymentPaid: Boolean(track?.paymentPaid),
    anyItemDelivered: Boolean(track?.anyItemDelivered),
    anyShipmentDelivered: Boolean(track?.anyShipmentDelivered),
    allItemsCompleted: Boolean(track?.allItemsCompleted),
    isLoading: status === "loading",
    isRefreshing: status === "refreshing",
    errorMessage,
    refresh,
  };
}
