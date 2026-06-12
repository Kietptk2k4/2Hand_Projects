import { useCallback, useEffect, useRef, useState } from "react";
import { fetchProductList } from "../api/productListApi";
import { DEFAULT_SORT, PAGE_SIZE } from "../constants/productListConstants";
import { mapProductListResponse } from "../utils/productListMapper";

export function useProductList() {
  const [sort, setSort] = useState(DEFAULT_SORT);
  const [page, setPage] = useState(1);
  const [items, setItems] = useState([]);
  const [pagination, setPagination] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const requestIdRef = useRef(0);

  const loadPage = useCallback(
    async (targetPage, { append = false, sortValue = sort } = {}) => {
      const requestId = ++requestIdRef.current;
      setStatus(append ? "loadingMore" : "loading");
      setErrorMessage("");

      try {
        const raw = await fetchProductList({
          page: targetPage,
          limit: PAGE_SIZE,
          sort: sortValue,
        });
        if (requestId !== requestIdRef.current) return;

        const data = mapProductListResponse(raw);
        setItems((prev) => (append ? [...prev, ...data.items] : data.items));
        setPagination(data.pagination);
        setPage(targetPage);
        setStatus("ready");
      } catch (error) {
        if (requestId !== requestIdRef.current) return;
        setStatus("error");
        setErrorMessage(error?.message || "Không tải được danh sách sản phẩm. Vui lòng thử lại.");
      }
    },
    [sort]
  );

  const changeSort = useCallback(
    (nextSort) => {
      if (nextSort === sort) return;
      setSort(nextSort);
      setPage(1);
      setItems([]);
      setPagination(null);
      setErrorMessage("");
      requestIdRef.current += 1;
      loadPage(1, { append: false, sortValue: nextSort });
    },
    [loadPage, sort]
  );

  useEffect(() => {
    loadPage(1, { append: false, sortValue: DEFAULT_SORT });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadMore = useCallback(() => {
    if (status === "loadingMore" || !pagination?.hasNext) return;
    loadPage(page + 1, { append: true });
  }, [loadPage, page, pagination?.hasNext, status]);

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
    isInitialLoading: status === "loading" && items.length === 0,
    isLoadingMore: status === "loadingMore",
    hasNext: Boolean(pagination?.hasNext),
    loadMore,
    retry,
  };
}
