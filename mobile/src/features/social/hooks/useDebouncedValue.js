import { useEffect, useState } from "react";
import { SEARCH_DEBOUNCE_MS } from "../constants/discoveryConstants";

export function useDebouncedValue(value, delayMs = SEARCH_DEBOUNCE_MS) {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedValue(value), delayMs);
    return () => clearTimeout(timer);
  }, [value, delayMs]);

  return debouncedValue;
}
