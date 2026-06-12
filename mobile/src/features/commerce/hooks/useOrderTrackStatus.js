import { useCallback, useEffect, useRef, useState } from "react";
import { fetchOrderTrackStatus } from "../api/orderTrackApi";
import { ORDER_TRACK_POLL_INTERVAL_MS } from "../constants/orderDetailConstants";
import { buildOrderTimelineEvents } from "../utils/buildOrderTimelineEvents";
import { mapOrderTrackResponse } from "../utils/orderTrackMapper";

const NOT_FOUND_CODE = "COMMERCE-404-ORDER";

export function useOrderTrackStatus(orderId, { enabled = true, pollWhileProcessing = true } = {}) {
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
      if (error?.code === NOT_FOUND_CODE) {
        setStatus("notFound");
        setErrorMessage(error?.message || "Không tìm thấy đơn hàng.");
        return null;
      }

      setStatus("error");
      setErrorMessage(error?.message || "Không tải được trạng thái đơn hàng.");
      throw error;
    }
  }, [enabled, orderId]);

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
    isNotFound: status === "notFound",
    isError: status === "error",
    errorMessage,
    refresh,
  };
}
