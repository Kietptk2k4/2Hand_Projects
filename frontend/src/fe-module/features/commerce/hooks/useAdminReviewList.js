import { useCallback, useEffect, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { fetchAdminReviewList } from "../api/adminReviewModerationApi";
import {
  PAGE_SIZE,
  REVIEW_STATUS_FILTER_TABS,
  mapAdminReviewModerationApiError,
} from "../constants/adminReviewModerationConstants";
import { mapAdminReviewListResponse } from "../utils/adminReviewModerationMapper";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";

function isUnauthorizedError(error) {
  const code = String(error?.code ?? "");
  return code === "401" || code.includes("401") || code.includes("COMMERCE-401");
}

function isForbiddenError(error) {
  return String(error?.code ?? "") === "COMMERCE-403";
}

function parseReviewStatusTab(searchParams) {
  const tab = searchParams.get("reviewStatus") || "all";
  const found = REVIEW_STATUS_FILTER_TABS.find((t) => t.id === tab);
  return found || REVIEW_STATUS_FILTER_TABS[0];
}

function parseRatingFilter(searchParams) {
  const raw = searchParams.get("rating");
  if (!raw) return null;
  const n = Number(raw);
  if (!Number.isInteger(n) || n < 1 || n > 5) return null;
  return n;
}

export function useAdminReviewList() {
  const { showSessionExpired } = useAuthSession();
  const [searchParams, setSearchParams] = useSearchParams();

  const activeStatusTab = parseReviewStatusTab(searchParams);
  const statusFilter = activeStatusTab.status;
  const ratingFilter = parseRatingFilter(searchParams);
  const serverSearch = searchParams.get("q") || "";

  const [page, setPage] = useState(1);
  const [items, setItems] = useState([]);
  const [pagination, setPagination] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");
  const [forbidden, setForbidden] = useState(false);
  const [searchInput, setSearchInput] = useState(serverSearch);
  const requestIdRef = useRef(0);

  const loadPage = useCallback(
    async (
      targetPage,
      {
        statusValue = statusFilter,
        ratingValue = ratingFilter,
        qValue = serverSearch,
      } = {},
    ) => {
      const requestId = ++requestIdRef.current;
      setIsLoading(true);
      setErrorMessage("");
      setForbidden(false);

      try {
        const raw = await fetchAdminReviewList({
          page: targetPage,
          limit: PAGE_SIZE,
          status: statusValue ?? undefined,
          rating: ratingValue ?? undefined,
          q: qValue || undefined,
        });

        if (requestId !== requestIdRef.current) return;

        const data = mapAdminReviewListResponse(raw);
        setItems(data.items);
        setPagination(data.pagination);
        setPage(targetPage);
      } catch (error) {
        if (requestId !== requestIdRef.current) return;

        if (isUnauthorizedError(error)) {
          showSessionExpired(error?.message);
          return;
        }

        if (isForbiddenError(error)) {
          setForbidden(true);
          setItems([]);
          setPagination(null);
          return;
        }

        setErrorMessage(mapAdminReviewModerationApiError(error));
      } finally {
        if (requestId === requestIdRef.current) {
          setIsLoading(false);
        }
      }
    },
    [ratingFilter, serverSearch, showSessionExpired, statusFilter],
  );

  useEffect(() => {
    setPage(1);
    loadPage(1);
  }, [statusFilter, ratingFilter, serverSearch, loadPage]);

  const changeStatusTab = useCallback(
    (tabId) => {
      const next = new URLSearchParams(searchParams);
      if (tabId === "all") next.delete("reviewStatus");
      else next.set("reviewStatus", tabId);
      setSearchParams(next, { replace: true });
    },
    [searchParams, setSearchParams],
  );

  const changeRatingFilter = useCallback(
    (rating) => {
      const next = new URLSearchParams(searchParams);
      if (rating == null || rating === "") next.delete("rating");
      else next.set("rating", String(rating));
      setSearchParams(next, { replace: true });
    },
    [searchParams, setSearchParams],
  );

  const applySearch = useCallback(() => {
    const next = new URLSearchParams(searchParams);
    const trimmed = searchInput.trim();
    if (trimmed) next.set("q", trimmed);
    else next.delete("q");
    setSearchParams(next, { replace: true });
  }, [searchInput, searchParams, setSearchParams]);

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

  const totalItems = pagination?.totalItems ?? 0;
  const totalPages = pagination?.totalPages ?? 1;
  const rangeStart = totalItems === 0 ? 0 : (page - 1) * PAGE_SIZE + 1;
  const rangeEnd = Math.min(page * PAGE_SIZE, totalItems);

  return {
    items,
    activeStatusTabId: activeStatusTab.id,
    changeStatusTab,
    ratingFilter,
    changeRatingFilter,
    page,
    pagination,
    totalItems,
    totalPages,
    rangeStart,
    rangeEnd,
    isLoading,
    errorMessage,
    forbidden,
    isEmpty: !isLoading && !errorMessage && !forbidden && totalItems === 0,
    goToPage,
    retry,
    refresh,
    searchInput,
    setSearchInput,
    applySearch,
  };
}
