import { useCallback, useEffect, useState } from "react";
import { fetchSellerOrderDetail } from "../api/sellerOrderApi";
import { mapSellerOrderDetail } from "../utils/sellerOrderMapper";

const NOT_FOUND_CODE = "COMMERCE-404-ORDER";

export function useSellerOrderDetail(orderId) {
  const [detail, setDetail] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const load = useCallback(async (targetOrderId = orderId) => {
    if (!targetOrderId) return;

    setStatus("loading");
    setErrorMessage("");

    try {
      const raw = await fetchSellerOrderDetail(targetOrderId);
      setDetail(mapSellerOrderDetail(raw));
      setStatus("ready");
    } catch (error) {
      if (error?.code === NOT_FOUND_CODE || error?.code === 404) {
        setStatus("notFound");
        setErrorMessage(error?.message || "Đơn hàng không tồn tại.");
        setDetail(null);
        return;
      }

      setStatus("error");
      setErrorMessage(error?.message || "Không tải được chi tiết đơn hàng.");
      setDetail(null);
    }
  }, [orderId]);

  useEffect(() => {
    load(orderId);
  }, [load, orderId]);

  const retry = useCallback(() => {
    load(orderId);
  }, [load, orderId]);

  return {
    detail,
    status,
    errorMessage,
    isLoading: status === "loading",
    isNotFound: status === "notFound",
    isError: status === "error",
    retry,
    reload: retry,
  };
}
