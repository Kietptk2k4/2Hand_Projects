import { useCallback, useEffect, useState } from "react";
import { fetchOrderDetail } from "../api/orderDetailApi";
import { mapOrderDetailResponse } from "../utils/orderDetailMapper";

const NOT_FOUND_CODE = "COMMERCE-404-ORDER";

export function useOrderDetail(orderId) {
  const [order, setOrder] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [errorCode, setErrorCode] = useState(null);

  const load = useCallback(async () => {
    if (!orderId) {
      setOrder(null);
      setStatus("idle");
      return;
    }

    setStatus("loading");
    setErrorMessage("");
    setErrorCode(null);
    setOrder(null);

    try {
      const raw = await fetchOrderDetail(orderId);
      setOrder(mapOrderDetailResponse(raw));
      setStatus("ready");
    } catch (error) {
      if (error?.code === NOT_FOUND_CODE || error?.code === 404) {
        setStatus("notFound");
        setErrorCode(NOT_FOUND_CODE);
        setErrorMessage(error?.message || "Không tìm thấy đơn hàng.");
        return;
      }

      setStatus("error");
      setErrorCode(error?.code || 500);
      setErrorMessage(error?.message || "Không tải được chi tiết đơn hàng. Vui lòng thử lại.");
    }
  }, [orderId]);

  useEffect(() => {
    load();
  }, [load]);

  return {
    order,
    status,
    errorMessage,
    errorCode,
    isLoading: status === "loading",
    isNotFound: status === "notFound",
    isError: status === "error",
    retry: load,
  };
}
