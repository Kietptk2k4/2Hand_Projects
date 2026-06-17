import { useCallback, useEffect, useState } from "react";
import { fetchProductReviews } from "../api/productReviewsApi";
import {
  DEFAULT_SORT,
  DETAIL_PREVIEW_LIMIT,
} from "../constants/productReviewsConstants";
import { mapProductReviewsResponse } from "../utils/productReviewsMapper";

export function useProductReviewsPreview(productId) {
  const [shop, setShop] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [ratingSummary, setRatingSummary] = useState({ ratingAvg: 0, ratingCount: 0 });
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  const load = useCallback(async () => {
    if (!productId) return;

    setStatus("loading");
    setErrorMessage("");

    try {
      const raw = await fetchProductReviews({
        productId,
        page: 1,
        limit: DETAIL_PREVIEW_LIMIT,
        sort: DEFAULT_SORT,
      });
      const data = mapProductReviewsResponse(raw);
      setShop(data.shop);
      setReviews(data.reviews);
      setRatingSummary(data.ratingSummary);
      setStatus("ready");
    } catch (error) {
      setStatus("error");
      setErrorMessage(error?.message || "Không tải được đánh giá.");
      setReviews([]);
    }
  }, [productId]);

  useEffect(() => {
    load();
  }, [load]);

  return {
    shop,
    reviews,
    ratingSummary,
    isLoading: status === "loading",
    isEmpty: status === "ready" && ratingSummary.ratingCount === 0,
    hasMoreReviews: ratingSummary.ratingCount > DETAIL_PREVIEW_LIMIT,
    errorMessage,
    retry: load,
  };
}
