import { useCallback, useEffect, useRef, useState } from "react";
import { fetchShopReviews } from "../api/shopReviewsApi";
import { DEFAULT_SORT, PAGE_SIZE } from "../constants/productReviewsConstants";
import { mapShopReviewsResponse } from "../utils/shopReviewsMapper";

const NOT_FOUND_CODE = "COMMERCE-404-SHOP";

export function useShopReviews(shopId) {
  const [ratingFilter, setRatingFilter] = useState(null);
  const [sort, setSort] = useState(DEFAULT_SORT);
  const [page, setPage] = useState(1);
  const [shopName, setShopName] = useState("");
  const [shopMeta, setShopMeta] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [ratingSummary, setRatingSummary] = useState({ ratingAvg: 0, ratingCount: 0 });
  const [pagination, setPagination] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const requestIdRef = useRef(0);

  const loadPage = useCallback(
    async (
      targetPage,
      {
        append = false,
        sortValue = sort,
        ratingValue = ratingFilter,
        shopIdValue = shopId,
      } = {}
    ) => {
      if (!shopIdValue) return;

      const requestId = ++requestIdRef.current;
      setStatus(append ? "loadingMore" : "loading");
      setErrorMessage("");

      try {
        const raw = await fetchShopReviews({
          shopId: shopIdValue,
          page: targetPage,
          limit: PAGE_SIZE,
          sort: sortValue,
          rating: ratingValue ?? undefined,
        });
        if (requestId !== requestIdRef.current) return;

        const data = mapShopReviewsResponse(raw);
        setShopName(data.shopName || "");
        setShopMeta(
          data.shopId
            ? {
                shopId: data.shopId,
                shopName: data.shopName,
                avatarUrl: data.shopAvatarUrl,
                sellerId: data.sellerId,
              }
            : null
        );
        setReviews((prev) => (append ? [...prev, ...data.reviews] : data.reviews));
        setRatingSummary(data.ratingSummary);
        setPagination(data.pagination);
        setPage(targetPage);
        setStatus("ready");
      } catch (error) {
        if (requestId !== requestIdRef.current) return;

        if (error?.code === NOT_FOUND_CODE || error?.code === 404) {
          setStatus("notFound");
          setErrorMessage(error?.message || "Shop không tồn tại.");
          setReviews([]);
          setPagination(null);
          return;
        }

        setStatus("error");
        setErrorMessage(error?.message || "Không tải được đánh giá. Vui lòng thử lại.");
      }
    },
    [ratingFilter, shopId, sort]
  );

  const resetAndFetch = useCallback(
    (options = {}) => {
      const {
        sortValue = sort,
        ratingValue = ratingFilter,
        shopIdValue = shopId,
      } = options;

      setPage(1);
      setReviews([]);
      setPagination(null);
      setErrorMessage("");
      requestIdRef.current += 1;
      loadPage(1, {
        append: false,
        sortValue,
        ratingValue,
        shopIdValue,
      });
    },
    [loadPage, ratingFilter, shopId, sort]
  );

  useEffect(() => {
    if (!shopId) return;
    setSort(DEFAULT_SORT);
    setRatingFilter(null);
  }, [shopId]);

  useEffect(() => {
    if (!shopId) return;
    resetAndFetch({
      sortValue: sort,
      ratingValue: ratingFilter,
      shopIdValue: shopId,
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [shopId, ratingFilter, sort]);

  const changeSort = useCallback(
    (nextSort) => {
      if (nextSort === sort) return;
      setSort(nextSort);
      resetAndFetch({ sortValue: nextSort });
    },
    [resetAndFetch, sort]
  );

  const changeRatingFilter = useCallback(
    (nextRating) => {
      if (nextRating === ratingFilter) return;
      setRatingFilter(nextRating);
    },
    [ratingFilter]
  );

  const loadMore = useCallback(() => {
    if (status === "loadingMore" || !pagination?.hasNext) return;
    loadPage(page + 1, { append: true });
  }, [loadPage, page, pagination?.hasNext, status]);

  const retry = useCallback(() => {
    if (reviews.length > 0) {
      loadPage(page, { append: false });
      return;
    }
    resetAndFetch();
  }, [loadPage, page, resetAndFetch, reviews.length]);

  return {
    shopName,
    shop: shopMeta,
    reviews,
    ratingSummary,
    sort,
    ratingFilter,
    changeSort,
    changeRatingFilter,
    errorMessage,
    isNotFound: status === "notFound",
    isInitialLoading: status === "loading" && reviews.length === 0,
    isLoadingMore: status === "loadingMore",
    isEmpty: status === "ready" && reviews.length === 0,
    hasNext: Boolean(pagination?.hasNext),
    loadMore,
    retry,
  };
}
