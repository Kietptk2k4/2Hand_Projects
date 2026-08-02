import { useCallback, useEffect, useRef, useState } from "react";
import { fetchFlashSaleProducts } from "../api/flashSaleApi";
import { mapFlashSaleResponse } from "../utils/flashSaleMapper";

export function useFlashSaleProducts({ enabled = true, limit = 20 } = {}) {
  const [items, setItems] = useState([]);
  const [slotStart, setSlotStart] = useState(null);
  const [slotEnd, setSlotEnd] = useState(null);
  const [isLoading, setIsLoading] = useState(Boolean(enabled));
  const [errorMessage, setErrorMessage] = useState("");
  const requestSeqRef = useRef(0);

  const load = useCallback(async () => {
    if (!enabled) {
      setItems([]);
      setIsLoading(false);
      return;
    }

    const requestSeq = ++requestSeqRef.current;
    setIsLoading(true);
    setErrorMessage("");
    try {
      const data = await fetchFlashSaleProducts({ limit });
      if (requestSeq !== requestSeqRef.current) return;

      const mapped = mapFlashSaleResponse(data);
      setItems(mapped.items);
      setSlotStart(mapped.slotStart);
      setSlotEnd(mapped.slotEnd);
    } catch (error) {
      if (requestSeq !== requestSeqRef.current) return;
      setItems([]);
      setErrorMessage(error?.message || "Không tải được Flash Sale.");
    } finally {
      if (requestSeq === requestSeqRef.current) {
        setIsLoading(false);
      }
    }
  }, [enabled, limit]);

  useEffect(() => {
    load();
  }, [load]);

  return {
    items,
    slotStart,
    slotEnd,
    isLoading,
    errorMessage,
    retry: load,
  };
}
