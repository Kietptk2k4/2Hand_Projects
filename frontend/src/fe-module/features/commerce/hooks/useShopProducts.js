import { useCallback, useEffect, useRef, useState } from "react";
import { fetchShopProducts } from "../api/shopProductsApi";
import { DEFAULT_SORT, PAGE_SIZE } from "../constants/shopProductsConstants";
import { mapShopProductsResponse } from "../utils/shopProductsMapper";

const NOT_FOUND_CODE = "COMMERCE-404-SHOP";

export function useShopProducts(shopId) {
  const [sort, setSort] = useState(DEFAULT_SORT);
  const [page, setPage] = useState(1);
  const [items, setItems] = useState([]);
  const [shop, setShop] = useState(null);
  const [pagination, setPagination] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [errorCode, setErrorCode] = useState(null);
  const requestIdRef = useRef(0);

  const loadPage = useCallback(
    async (targetPage, { append = false, sortValue = sort, shopIdValue = shopId } = {}) => {
      if (!shopIdValue) return;

      const requestId = ++requestIdRef.current;
      setStatus(append ? "loadingMore" : "loading");
      setErrorMessage("");
      setErrorCode(null);

      try {
        const raw = await fetchShopProducts({
          shopId: shopIdValue,
          page: targetPage,
          limit: PAGE_SIZE,
          sort: sortValue,
        });
        if (requestId !== requestIdRef.current) return;

        const data = mapShopProductsResponse(raw);
        setItems((prev) => (append ? [...prev, ...data.items] : data.items));
        setShop(data.shop);
        setPagination(data.pagination);
        setPage(targetPage);
        setStatus("ready");
      } catch (error) {
        if (requestId !== requestIdRef.current) return;

        if (error?.code === NOT_FOUND_CODE || error?.code === 404) {
          setStatus("notFound");
          setErrorCode(NOT_FOUND_CODE);
          setErrorMessage(error?.message || "Shop không tồn tại hoặc không khả dụng.");
          setItems([]);
          setShop(null);
          setPagination(null);
          return;
        }

        setStatus("error");
        setErrorMessage(error?.message || "Không tải được sản phẩm của shop. Vui lòng thử lại.");
      }
    },
    [shopId, sort]
  );

  const resetAndFetch = useCallback(
    (options = {}) => {
      const { sortValue = sort, shopIdValue = shopId } = options;

      setPage(1);
      setItems([]);
      setPagination(null);
      setErrorMessage("");
      setErrorCode(null);
      requestIdRef.current += 1;
      loadPage(1, { append: false, sortValue, shopIdValue });
    },
    [loadPage, shopId, sort]
  );

  useEffect(() => {
    if (!shopId) return;
    setSort(DEFAULT_SORT);
    resetAndFetch({ sortValue: DEFAULT_SORT, shopIdValue: shopId });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [shopId]);

  const changeSort = useCallback(
    (nextSort) => {
      if (nextSort === sort) return;
      setSort(nextSort);
      resetAndFetch({ sortValue: nextSort });
    },
    [resetAndFetch, sort]
  );

  const loadMore = useCallback(() => {
    if (status === "loadingMore" || !pagination?.hasNext) return;
    loadPage(page + 1, { append: true });
  }, [loadPage, page, pagination?.hasNext, status]);

  const retry = useCallback(() => {
    if (items.length > 0) {
      loadPage(page, { append: false });
      return;
    }
    resetAndFetch();
  }, [items.length, loadPage, page, resetAndFetch]);

  return {
    items,
    shop,
    pagination,
    sort,
    changeSort,
    status,
    errorMessage,
    errorCode,
    isNotFound: status === "notFound",
    isInitialLoading: status === "loading" && items.length === 0 && !shop,
    isLoadingMore: status === "loadingMore",
    hasNext: Boolean(pagination?.hasNext),
    loadMore,
    retry,
  };
}
