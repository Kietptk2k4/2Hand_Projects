import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { fetchSellerShopReviews } from "../api/sellerShopReviewsApi";
import {
  PAGE_SIZE,
  mapSellerShopReviewsApiError,
} from "../constants/sellerShopReviewsConstants";
import { mapSellerShopReviewsResponse } from "../utils/sellerShopReviewsMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

function parseRatingParam(value) {
  if (!value) return null;
  const n = Number(value);
  if (!Number.isInteger(n) || n < 1 || n > 5) return null;
  return n;
}

function parseReplyTab(value) {
  if (value === "unreplied" || value === "replied") return value;
  return "all";
}

export function useSellerShopReviews() {
  const { showSessionExpired } = useAuthSession();
  const [searchParams, setSearchParams] = useSearchParams();

  const ratingFilter = parseRatingParam(searchParams.get("rating"));
  const statusFilter = searchParams.get("status") === "HIDDEN" ? "HIDDEN" : "VISIBLE";
  const replyTab = parseReplyTab(searchParams.get("reply"));

  const [page, setPage] = useState(1);
  const [reviews, setReviews] = useState([]);
  const [ratingSummary, setRatingSummary] = useState({ ratingAvg: 0, ratingCount: 0 });
  const [pagination, setPagination] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");
  const [clientSearch, setClientSearch] = useState("");
  const requestIdRef = useRef(0);

  const loadPage = useCallback(
    async (
      targetPage,
      {
        ratingValue = ratingFilter,
        statusValue = statusFilter,
      } = {},
    ) => {
      const requestId = ++requestIdRef.current;
      setIsLoading(true);
      setErrorMessage("");

      try {
        const raw = await fetchSellerShopReviews({
          page: targetPage,
          limit: PAGE_SIZE,
          rating: ratingValue ?? undefined,
          status: statusValue,
        });

        if (requestId !== requestIdRef.current) return;

        const data = mapSellerShopReviewsResponse(raw);
        setReviews(data.reviews);
        setRatingSummary(data.ratingSummary);
        setPagination(data.pagination);
        setPage(targetPage);
      } catch (error) {
        if (requestId !== requestIdRef.current) return;

        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
          return;
        }

        setErrorMessage(mapSellerShopReviewsApiError(error));
      } finally {
        if (requestId === requestIdRef.current) {
          setIsLoading(false);
        }
      }
    },
    [ratingFilter, showSessionExpired, statusFilter],
  );

  useEffect(() => {
    setPage(1);
    loadPage(1);
  }, [ratingFilter, statusFilter, loadPage]);

  const changeRatingFilter = useCallback(
    (rating) => {
      const next = new URLSearchParams(searchParams);
      if (rating == null || rating === "") next.delete("rating");
      else next.set("rating", String(rating));
      setSearchParams(next, { replace: true });
    },
    [searchParams, setSearchParams],
  );

  const changeStatusFilter = useCallback(
    (status) => {
      const next = new URLSearchParams(searchParams);
      if (status === "VISIBLE") next.delete("status");
      else next.set("status", status);
      setSearchParams(next, { replace: true });
    },
    [searchParams, setSearchParams],
  );

  const changeReplyTab = useCallback(
    (tabId) => {
      const next = new URLSearchParams(searchParams);
      if (tabId === "all") next.delete("reply");
      else next.set("reply", tabId);
      setSearchParams(next, { replace: true });
    },
    [searchParams, setSearchParams],
  );

  const goToPage = useCallback(
    (targetPage) => {
      if (!pagination) return;
      if (targetPage < 1 || targetPage > pagination.totalPages) return;
      loadPage(targetPage);
    },
    [loadPage, pagination],
  );

  const retry = useCallback(() => loadPage(page), [loadPage, page]);

  const refresh = useCallback(() => loadPage(page), [loadPage, page]);

  const filteredReviews = useMemo(() => {
    let list = reviews;

    if (replyTab === "unreplied") {
      list = list.filter((r) => !r.sellerReply);
    } else if (replyTab === "replied") {
      list = list.filter((r) => Boolean(r.sellerReply));
    }

    const needle = clientSearch.trim().toLowerCase();
    if (!needle) return list;

    return list.filter(
      (r) =>
        r.productNameSnapshot?.toLowerCase().includes(needle) ||
        r.orderItemId?.toLowerCase().includes(needle),
    );
  }, [clientSearch, replyTab, reviews]);

  const replyTabCounts = useMemo(() => {
    const unreplied = reviews.filter((r) => !r.sellerReply).length;
    const replied = reviews.filter((r) => Boolean(r.sellerReply)).length;
    return {
      all: reviews.length,
      unreplied,
      replied,
    };
  }, [reviews]);

  const totalItems = pagination?.totalItems ?? 0;
  const totalPages = pagination?.totalPages ?? 1;
  const rangeStart = totalItems === 0 ? 0 : (page - 1) * PAGE_SIZE + 1;
  const rangeEnd = Math.min(page * PAGE_SIZE, totalItems);

  const isEmpty =
    !isLoading && !errorMessage && totalItems === 0 && !clientSearch && replyTab === "all";
  const isFilterEmpty =
    !isLoading &&
    !errorMessage &&
    filteredReviews.length === 0 &&
    (ratingFilter != null || replyTab !== "all" || statusFilter === "HIDDEN");
  const isSearchEmpty =
    !isLoading && !errorMessage && filteredReviews.length === 0 && Boolean(clientSearch);

  return {
    reviews: filteredReviews,
    rawReviews: reviews,
    ratingSummary,
    activeReplyTab: replyTab,
    changeReplyTab,
    replyTabCounts,
    ratingFilter,
    changeRatingFilter,
    statusFilter,
    changeStatusFilter,
    page,
    pagination,
    totalItems,
    totalPages,
    rangeStart,
    rangeEnd,
    isLoading,
    errorMessage,
    isEmpty,
    isFilterEmpty,
    isSearchEmpty,
    goToPage,
    retry,
    refresh,
    clientSearch,
    setClientSearch,
  };
}
