/**
 * User search API is not exposed on social-service for mobile clients yet.
 * This hook exists as a stable surface for SearchScreen when the API ships.
 */
export function useSearchUsers(_debouncedQuery) {
  return {
    isAvailable: false,
    items: [],
    errorMessage: "",
    isInitialLoading: false,
    isLoadingMore: false,
    hasNext: false,
    loadMore: () => {},
    retry: () => {},
    refetch: () => {},
  };
}
