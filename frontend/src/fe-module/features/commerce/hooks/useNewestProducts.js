import { useCallback, useEffect, useRef, useState } from "react";
import { fetchNewestProducts } from "../api/newestProductsApi";
import { DEFAULT_SORT, PAGE_SIZE } from "../constants/productListConstants";
import { mapProductListResponse } from "../utils/productListMapper";

export function useNewestProducts({
  enabled = true,
  limit = PAGE_SIZE,
  paginated = false,
} = {}) {
  const [sort, setSort] = useState(DEFAULT_SORT);
  const [page, setPage] = useState(1);
  const [items, setItems] = useState([]);
  const [pagination, setPagination] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const requestSeqRef = useRef(0);

  const loadPage = useCallback(
    async (targetPage, { append = false, sortValue = sort } = {}) => {
      if (!enabled) {
        setItems([]);
        setStatus("idle");
        return;
      }

      const requestSeq = ++requestSeqRef.current;
      setStatus(append ? "loadingMore" : "loading");
      setErrorMessage("");

      try {
        const raw = await fetchNewestProducts({
          page: targetPage,
          limit,
          sort: sortValue,
        });
        if (requestSeq !== requestSeqRef.current) return;

        const mapped = mapProductListResponse(raw);
        setItems((prev) => (append ? [...prev, ...mapped.items] : mapped.items));
        setPagination(mapped.pagination);
        setPage(targetPage);
        setStatus("ready");
      } catch (error) {
        if (requestSeq !== requestSeqRef.current) return;
        if (!append) {
          setItems([]);
          setPagination(null);
        }
        setStatus("error");
        setErrorMessage(error?.message || "Không tải được hàng mới về.");
      }
    },
    [enabled, limit, sort]
  );

  const changeSort = useCallback(
    (nextSort) => {
      if (!paginated || nextSort === sort) return;
      setSort(nextSort);
      setPage(1);
      setItems([]);
      setPagination(null);
      setErrorMessage("");
      requestSeqRef.current += 1;
      loadPage(1, { append: false, sortValue: nextSort });
    },
    [loadPage, paginated, sort]
  );

  useEffect(() => {
    loadPage(1, { append: false, sortValue: DEFAULT_SORT });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [enabled, limit]);

  const loadMore = useCallback(() => {
    if (!paginated || status === "loadingMore" || !pagination?.hasNext) return;
    loadPage(page + 1, { append: true });
  }, [loadPage, page, paginated, pagination?.hasNext, status]);

  const retry = useCallback(() => {
    if (items.length > 0) {
      loadPage(page, { append: false });
      return;
    }
    loadPage(1, { append: false });
  }, [items.length, loadPage, page]);

  return {
    items,
    pagination,
    sort,
    changeSort,
    status,
    errorMessage,
    isLoading: status === "loading" && items.length === 0,
    isInitialLoading: status === "loading" && items.length === 0,
    isLoadingMore: status === "loadingMore",
    hasNext: Boolean(paginated && pagination?.hasNext),
    loadMore,
    retry,
  };
}
