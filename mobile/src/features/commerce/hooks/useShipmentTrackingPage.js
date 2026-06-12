import { useCallback, useEffect, useMemo, useState } from "react";
import { fetchShipmentDetail, fetchShipmentTracking } from "../api/shipmentApi";
import { buildShipmentTimelineEvents } from "../utils/buildShipmentTimelineEvents";
import { mapShipmentDetailResponse } from "../utils/shipmentMapper";
import { mapShipmentTrackResponse } from "../utils/shipmentTrackMapper";

const NOT_FOUND_CODE = "COMMERCE-404-SHIPMENT";

export function useShipmentTrackingPage(orderId, shipmentId) {
  const [detail, setDetail] = useState(null);
  const [tracking, setTracking] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [orderMismatch, setOrderMismatch] = useState(false);

  const load = useCallback(
    async ({ isRefresh = false } = {}) => {
      if (!orderId || !shipmentId) {
        setDetail(null);
        setTracking(null);
        setStatus("idle");
        return;
      }

      setStatus(isRefresh ? "refreshing" : "loading");
      setErrorMessage("");
      setOrderMismatch(false);
      if (!isRefresh) {
        setDetail(null);
        setTracking(null);
      }

      try {
        const [rawDetail, rawTracking] = await Promise.all([
          fetchShipmentDetail(shipmentId),
          fetchShipmentTracking(shipmentId),
        ]);

        const mappedDetail = mapShipmentDetailResponse(rawDetail);
        const mappedTracking = mapShipmentTrackResponse(rawTracking);

        const detailOrderId = mappedDetail?.orderId;
        const trackOrderId = mappedTracking?.orderId;

        if (
          detailOrderId !== orderId ||
          trackOrderId !== orderId ||
          (detailOrderId && trackOrderId && detailOrderId !== trackOrderId)
        ) {
          setOrderMismatch(true);
          setStatus("error");
          setErrorMessage("Thông tin vận chuyển không khớp với đơn hàng.");
          return;
        }

        setDetail(mappedDetail);
        setTracking(mappedTracking);
        setStatus("ready");
      } catch (error) {
        if (error?.code === NOT_FOUND_CODE || error?.code === 404) {
          setStatus("notFound");
          setErrorMessage(error?.message || "Không tìm thấy thông tin vận chuyển.");
          return;
        }

        setStatus("error");
        setErrorMessage(error?.message || "Không tải được thông tin vận chuyển. Vui lòng thử lại.");
        throw error;
      }
    },
    [orderId, shipmentId],
  );

  useEffect(() => {
    load();
  }, [load]);

  const timelineEvents = useMemo(() => buildShipmentTimelineEvents(tracking), [tracking]);

  return {
    detail,
    tracking,
    timelineEvents,
    shipmentDelivered: Boolean(tracking?.shipmentDelivered),
    orderCompleted: Boolean(tracking?.orderCompleted),
    isLoading: status === "loading",
    isRefreshing: status === "refreshing",
    errorMessage,
    isNotFound: status === "notFound",
    isError: status === "error" || orderMismatch,
    refresh: () => load({ isRefresh: true }),
    retry: () => load({ isRefresh: false }),
  };
}
