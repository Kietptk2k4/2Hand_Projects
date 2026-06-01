import { useCallback, useEffect, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { searchProducts } from "../api/productSearchApi";
import {
  DEFAULT_SORT,
  MIN_KEYWORD_LENGTH,
  PAGE_SIZE,
} from "../constants/productSearchConstants";
import { addCommerceSearchHistory } from "../utils/commerceSearchHistoryStorage";
import { normalizeSearchKeyword } from "../utils/normalizeSearchKeyword";
import { mapProductSearchResponse } from "../utils/productSearchMapper";

const SEARCH_KEYWORD_ERROR = "COMMERCE-400-SEARCH-KEYWORD";

export function useProductSearch() {
  const [searchParams] = useSearchParams();
  const rawQ = searchParams.get("q") ?? "";
  const q = normalizeSearchKeyword(rawQ);
  const isQueryTooShort = q.length > 0 && q.length < MIN_KEYWORD_LENGTH;

  const [sort, setSort] = useState(DEFAULT_SORT);
  const [page, setPage] = useState(1);
  const [items, setItems] = useState([]);
  const [keyword, setKeyword] = useState("");
  const [pagination, setPagination] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const requestIdRef = useRef(0);

  const loadPage = useCallback(
    async (targetPage, { append = false, searchQ = q, sortValue = sort } = {}) => {
      const normalized = normalizeSearchKeyword(searchQ);

      if (!normalized || normalized.length < MIN_KEYWORD_LENGTH) {
        setItems([]);
        setPagination(null);
        setKeyword("");
        setStatus("idle");
        setErrorMessage("");
        return;
      }

      const requestId = ++requestIdRef.current;
      setStatus(append ? "loadingMore" : "loading");
      setErrorMessage("");

      try {
        const raw = await searchProducts({
          q: normalized,
          page: targetPage,
          limit: PAGE_SIZE,
          sort: sortValue,
        });
        if (requestId !== requestIdRef.current) return;

        const data = mapProductSearchResponse(raw);
        setItems((prev) => (append ? [...prev, ...data.items] : data.items));
        setPagination(data.pagination);
        setKeyword(data.keyword || normalized);
        setPage(targetPage);
        setStatus("ready");

        if (!append) {
          addCommerceSearchHistory(data.keyword || normalized);
        }
      } catch (error) {
        if (requestId !== requestIdRef.current) return;

        setStatus("error");
        setErrorMessage(error?.message || "Không tìm được sản phẩm. Vui lòng thử lại.");
        if (error?.code === SEARCH_KEYWORD_ERROR) {
          setItems([]);
          setPagination(null);
        }
      }
    },
    [q, sort]
  );

  useEffect(() => {
    if (!q) {
      setItems([]);
      setPagination(null);
      setKeyword("");
      setStatus("idle");
      setErrorMessage("");
      return;
    }

    if (isQueryTooShort) {
      setItems([]);
      setPagination(null);
      setKeyword("");
      setStatus("idle");
      setErrorMessage("");
      return;
    }

    setSort(DEFAULT_SORT);
    loadPage(1, { append: false, searchQ: q, sortValue: DEFAULT_SORT });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [q]);

  const changeSort = useCallback(
    (nextSort) => {
      if (nextSort === sort || !q || isQueryTooShort) return;
      setSort(nextSort);
      setPage(1);
      setItems([]);
      setPagination(null);
      requestIdRef.current += 1;
      loadPage(1, { append: false, searchQ: q, sortValue: nextSort });
    },
    [isQueryTooShort, loadPage, q, sort]
  );

  const loadMore = useCallback(() => {
    if (status === "loadingMore" || !pagination?.hasNext || !q || isQueryTooShort) return;
    loadPage(page + 1, { append: true });
  }, [isQueryTooShort, loadPage, page, pagination?.hasNext, q, status]);

  const retry = useCallback(() => {
    if (!q || isQueryTooShort) return;
    loadPage(items.length > 0 ? page : 1, { append: false });
  }, [isQueryTooShort, items.length, loadPage, page, q]);

  return {
    q,
    keyword,
    items,
    pagination,
    sort,
    changeSort,
    status,
    errorMessage,
    isQueryTooShort,
    isInitialLoading: status === "loading" && items.length === 0,
    isLoadingMore: status === "loadingMore",
    hasNext: Boolean(pagination?.hasNext),
    totalItems: pagination?.totalItems ?? 0,
    loadMore,
    retry,
  };
}
