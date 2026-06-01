import { useCallback, useEffect, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { fetchProductReviews } from "../api/productReviewsApi";
import {
  DEFAULT_SORT,
  PAGE_SIZE,
} from "../constants/productReviewsConstants";
import { mapProductReviewsResponse } from "../utils/productReviewsMapper";

const NOT_FOUND_CODE = "COMMERCE-404-PRODUCT";

function parseRatingParam(value) {
  if (value == null || value === "") return null;
  const parsed = Number(value);
  if (!Number.isInteger(parsed) || parsed < 1 || parsed > 5) return null;
  return parsed;
}

export function useProductReviews(productId) {
  const [searchParams, setSearchParams] = useSearchParams();
  const ratingFilter = parseRatingParam(searchParams.get("rating"));

  const [sort, setSort] = useState(DEFAULT_SORT);
  const [page, setPage] = useState(1);
  const [reviews, setReviews] = useState([]);
  const [ratingSummary, setRatingSummary] = useState({ ratingAvg: 0, ratingCount: 0 });
  const [pagination, setPagination] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [errorCode, setErrorCode] = useState(null);
  const requestIdRef = useRef(0);

  const loadPage = useCallback(
    async (
      targetPage,
      {
        append = false,
        sortValue = sort,
        ratingValue = ratingFilter,
        productIdValue = productId,
      } = {}
    ) => {
      if (!productIdValue) return;

      const requestId = ++requestIdRef.current;
      setStatus(append ? "loadingMore" : "loading");
      setErrorMessage("");
      setErrorCode(null);

      try {
        const raw = await fetchProductReviews({
          productId: productIdValue,
          page: targetPage,
          limit: PAGE_SIZE,
          sort: sortValue,
          rating: ratingValue ?? undefined,
        });
        if (requestId !== requestIdRef.current) return;

        const data = mapProductReviewsResponse(raw);
        setReviews((prev) => (append ? [...prev, ...data.reviews] : data.reviews));
        setRatingSummary(data.ratingSummary);
        setPagination(data.pagination);
        setPage(targetPage);
        setStatus("ready");
      } catch (error) {
        if (requestId !== requestIdRef.current) return;

        if (error?.code === NOT_FOUND_CODE || error?.code === 404) {
          setStatus("notFound");
          setErrorCode(NOT_FOUND_CODE);
          setErrorMessage(error?.message || "Sản phẩm không tồn tại.");
          setReviews([]);
          setPagination(null);
          return;
        }

        setStatus("error");
        setErrorMessage(error?.message || "Không tải được đánh giá. Vui lòng thử lại.");
      }
    },
    [productId, ratingFilter, sort]
  );

  const resetAndFetch = useCallback(
    (options = {}) => {
      const {
        sortValue = sort,
        ratingValue = ratingFilter,
        productIdValue = productId,
      } = options;

      setPage(1);
      setReviews([]);
      setPagination(null);
      setErrorMessage("");
      setErrorCode(null);
      requestIdRef.current += 1;
      loadPage(1, {
        append: false,
        sortValue,
        ratingValue,
        productIdValue,
      });
    },
    [loadPage, productId, ratingFilter, sort]
  );

  useEffect(() => {
    if (!productId) return;
    setSort(DEFAULT_SORT);
  }, [productId]);

  useEffect(() => {
    if (!productId) return;
    resetAndFetch({
      sortValue: sort,
      ratingValue: ratingFilter,
      productIdValue: productId,
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [productId, ratingFilter, sort]);

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
      setSearchParams((prev) => {
        const next = new URLSearchParams(prev);
        if (nextRating == null) {
          next.delete("rating");
        } else {
          next.set("rating", String(nextRating));
        }
        return next;
      });
    },
    [setSearchParams]
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
    reviews,
    ratingSummary,
    pagination,
    sort,
    ratingFilter,
    changeSort,
    changeRatingFilter,
    status,
    errorMessage,
    errorCode,
    isNotFound: status === "notFound",
    isInitialLoading: status === "loading" && reviews.length === 0,
    isLoadingMore: status === "loadingMore",
    isEmpty: status === "ready" && reviews.length === 0,
    hasNext: Boolean(pagination?.hasNext),
    loadMore,
    retry,
  };
}
