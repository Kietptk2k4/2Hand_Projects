import { useCallback, useRef } from "react";

const DRAG_THRESHOLD_PX = 12;

/**
 * Mouse drag for horizontal scroll. Pointer capture starts only after the user
 * moves past the threshold so taps/clicks on child tiles still fire normally.
 */
export function useHorizontalScrollDrag() {
  const scrollRef = useRef(null);
  const dragRef = useRef({
    active: false,
    pointerId: null,
    startX: 0,
    scrollLeft: 0,
    moved: false,
    capturing: false,
  });

  const resetDrag = useCallback(() => {
    dragRef.current.moved = false;
    dragRef.current.capturing = false;
  }, []);

  const wasDragged = useCallback(() => dragRef.current.moved, []);

  const onPointerDown = useCallback((event) => {
    const element = scrollRef.current;
    if (!element || event.button !== 0) return;

    dragRef.current = {
      active: true,
      pointerId: event.pointerId,
      startX: event.clientX,
      scrollLeft: element.scrollLeft,
      moved: false,
      capturing: false,
    };
  }, []);

  const onPointerMove = useCallback((event) => {
    const element = scrollRef.current;
    const drag = dragRef.current;
    if (!element || !drag.active || drag.pointerId !== event.pointerId) return;

    const delta = event.clientX - drag.startX;
    if (!drag.capturing && Math.abs(delta) > DRAG_THRESHOLD_PX) {
      drag.capturing = true;
      drag.moved = true;
      element.setPointerCapture(event.pointerId);
    }

    if (!drag.moved) return;

    element.scrollLeft = drag.scrollLeft - delta;
  }, []);

  const endDrag = useCallback((event) => {
    const element = scrollRef.current;
    const drag = dragRef.current;
    if (!element || !drag.active || drag.pointerId !== event.pointerId) return;

    if (drag.capturing && element.hasPointerCapture(event.pointerId)) {
      element.releasePointerCapture(event.pointerId);
    }

    drag.active = false;
    drag.pointerId = null;
    drag.capturing = false;

    if (!drag.moved) {
      drag.moved = false;
    }
  }, []);

  const onPointerUp = useCallback(
    (event) => {
      endDrag(event);
    },
    [endDrag],
  );

  return {
    scrollRef,
    onPointerDown,
    onPointerMove,
    onPointerUp,
    onPointerCancel: onPointerUp,
    wasDragged,
    resetDrag,
  };
}
