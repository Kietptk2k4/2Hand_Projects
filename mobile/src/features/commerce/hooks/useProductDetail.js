import { useCallback, useEffect, useState } from "react";
import { fetchProductDetail } from "../api/productDetailApi";
import { mapProductDetailResponse } from "../utils/productDetailMapper";

const NOT_FOUND_CODE = "COMMERCE-404-PRODUCT";

export function useProductDetail(productId) {
  const [product, setProduct] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [errorCode, setErrorCode] = useState(null);

  const load = useCallback(async () => {
    if (!productId) {
      setProduct(null);
      setStatus("idle");
      return;
    }

    setStatus("loading");
    setErrorMessage("");
    setErrorCode(null);
    setProduct(null);

    try {
      const raw = await fetchProductDetail(productId);
      setProduct(mapProductDetailResponse(raw));
      setStatus("ready");
    } catch (error) {
      if (error?.code === NOT_FOUND_CODE || error?.code === 404) {
        setStatus("notFound");
        setErrorCode(NOT_FOUND_CODE);
        setErrorMessage(error?.message || "Sản phẩm không tồn tại.");
        return;
      }

      setStatus("error");
      setErrorCode(error?.code || 500);
      setErrorMessage(error?.message || "Không tải được chi tiết sản phẩm. Vui lòng thử lại.");
    }
  }, [productId]);

  useEffect(() => {
    load();
  }, [load]);

  return {
    product,
    status,
    errorMessage,
    errorCode,
    isLoading: status === "loading",
    isNotFound: status === "notFound",
    isError: status === "error",
    retry: load,
  };
}
