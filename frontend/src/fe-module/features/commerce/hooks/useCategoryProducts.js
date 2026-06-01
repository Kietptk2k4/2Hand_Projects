import { useCallback, useEffect, useRef, useState } from "react";
import { fetchCategoryProducts } from "../api/categoryProductsApi";
import {
  DEFAULT_INCLUDE_CHILDREN,
  DEFAULT_SORT,
  PAGE_SIZE,
} from "../constants/categoryProductsConstants";
import { mapCategoryProductsResponse } from "../utils/categoryProductsMapper";

const NOT_FOUND_CODE = "COMMERCE-404-CATEGORY";

export function useCategoryProducts(categoryId) {
  const [sort, setSort] = useState(DEFAULT_SORT);
  const [includeChildren, setIncludeChildren] = useState(DEFAULT_INCLUDE_CHILDREN);
  const [page, setPage] = useState(1);
  const [items, setItems] = useState([]);
  const [category, setCategory] = useState(null);
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
        includeChildrenValue = includeChildren,
        categoryIdValue = categoryId,
      } = {}
    ) => {
      if (!categoryIdValue) return;

      const requestId = ++requestIdRef.current;
      setStatus(append ? "loadingMore" : "loading");
      setErrorMessage("");
      setErrorCode(null);

      try {
        const raw = await fetchCategoryProducts({
          categoryId: categoryIdValue,
          page: targetPage,
          limit: PAGE_SIZE,
          sort: sortValue,
          includeChildren: includeChildrenValue,
        });
        if (requestId !== requestIdRef.current) return;

        const data = mapCategoryProductsResponse(raw);
        setItems((prev) => (append ? [...prev, ...data.items] : data.items));
        setCategory(data.category);
        setPagination(data.pagination);
        setPage(targetPage);
        setStatus("ready");
      } catch (error) {
        if (requestId !== requestIdRef.current) return;

        if (error?.code === NOT_FOUND_CODE || error?.code === 404) {
          setStatus("notFound");
          setErrorCode(NOT_FOUND_CODE);
          setErrorMessage(error?.message || "Danh mục không tồn tại.");
          setItems([]);
          setCategory(null);
          setPagination(null);
          return;
        }

        setStatus("error");
        setErrorMessage(error?.message || "Không tải được sản phẩm theo danh mục. Vui lòng thử lại.");
      }
    },
    [categoryId, includeChildren, sort]
  );

  const resetAndFetch = useCallback(
    (options = {}) => {
      const {
        sortValue = sort,
        includeChildrenValue = includeChildren,
        categoryIdValue = categoryId,
      } = options;

      setPage(1);
      setItems([]);
      setPagination(null);
      setErrorMessage("");
      setErrorCode(null);
      requestIdRef.current += 1;
      loadPage(1, {
        append: false,
        sortValue,
        includeChildrenValue,
        categoryIdValue,
      });
    },
    [categoryId, includeChildren, loadPage, sort]
  );

  useEffect(() => {
    if (!categoryId) return;
    setSort(DEFAULT_SORT);
    setIncludeChildren(DEFAULT_INCLUDE_CHILDREN);
    resetAndFetch({
      sortValue: DEFAULT_SORT,
      includeChildrenValue: DEFAULT_INCLUDE_CHILDREN,
      categoryIdValue: categoryId,
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [categoryId]);

  const changeSort = useCallback(
    (nextSort) => {
      if (nextSort === sort) return;
      setSort(nextSort);
      resetAndFetch({ sortValue: nextSort });
    },
    [resetAndFetch, sort]
  );

  const changeIncludeChildren = useCallback(
    (nextValue) => {
      if (nextValue === includeChildren) return;
      setIncludeChildren(nextValue);
      resetAndFetch({ includeChildrenValue: nextValue });
    },
    [includeChildren, resetAndFetch]
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
    category,
    pagination,
    sort,
    includeChildren,
    changeSort,
    changeIncludeChildren,
    status,
    errorMessage,
    errorCode,
    isNotFound: status === "notFound",
    isInitialLoading: status === "loading" && items.length === 0,
    isLoadingMore: status === "loadingMore",
    hasNext: Boolean(pagination?.hasNext),
    loadMore,
    retry,
  };
}
