import { useCallback, useEffect, useRef, useState } from "react";

/**
 * Keep drawer open state local so X/Esc closes immediately even if URL sync is slow.
 * While closing, ignore stale selectedId from URL until it clears.
 */
export function useSyncedDrawerId(selectedId, onClear) {
  const [openId, setOpenId] = useState(() => selectedId || "");
  const closingRef = useRef(false);

  useEffect(() => {
    if (closingRef.current) {
      if (!selectedId) {
        closingRef.current = false;
        setOpenId("");
      }
      return;
    }
    setOpenId(selectedId || "");
  }, [selectedId]);

  const closeDrawer = useCallback(() => {
    closingRef.current = true;
    setOpenId("");
    queueMicrotask(() => {
      onClear?.();
    });
  }, [onClear]);

  return { openId, closeDrawer };
}
