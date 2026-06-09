import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useRef,
  useState,
} from "react";
const FLY_DURATION_MS = 620;
const FLY_SIZE_PX = 56;

const CartFlyAnimationContext = createContext(null);

function prefersReducedMotion() {
  return (
    typeof window !== "undefined" &&
    window.matchMedia("(prefers-reduced-motion: reduce)").matches
  );
}

function isElementVisible(element) {
  if (!element) return false;
  const rect = element.getBoundingClientRect();
  return rect.width > 0 && rect.height > 0 && rect.bottom > 0 && rect.right > 0;
}

function getRectCenter(rect) {
  return {
    x: rect.left + rect.width / 2,
    y: rect.top + rect.height / 2,
  };
}

function resolveFromRect(sourceElement, fromRect) {
  if (fromRect) return fromRect;
  if (sourceElement?.getBoundingClientRect) {
    return sourceElement.getBoundingClientRect();
  }
  return null;
}

function runFlyAnimation(imageUrl, fromRect, toRect) {
  return new Promise((resolve) => {
    const from = getRectCenter(fromRect);
    const to = getRectCenter(toRect);

    const layer = document.createElement("div");
    layer.setAttribute("aria-hidden", "true");
    layer.style.cssText = [
      "position:fixed",
      "left:0",
      "top:0",
      "width:0",
      "height:0",
      "z-index:9999",
      "pointer-events:none",
    ].join(";");

    const flyer = document.createElement("div");
    flyer.style.cssText = [
      "position:fixed",
      `left:${from.x}px`,
      `top:${from.y}px`,
      `width:${FLY_SIZE_PX}px`,
      `height:${FLY_SIZE_PX}px`,
      "border-radius:12px",
      "overflow:hidden",
      "box-shadow:0 8px 24px rgba(17,28,45,0.22)",
      "border:2px solid #fff",
      "transform:translate(-50%, -50%) scale(1)",
      "will-change:transform,opacity",
    ].join(";");

    if (imageUrl) {
      const img = document.createElement("img");
      img.src = imageUrl;
      img.alt = "";
      img.style.cssText = "width:100%;height:100%;object-fit:cover;display:block";
      flyer.appendChild(img);
    } else {
      flyer.style.background = "#dee8ff";
      flyer.style.display = "flex";
      flyer.style.alignItems = "center";
      flyer.style.justifyContent = "center";
      const icon = document.createElement("span");
      icon.textContent = "inventory_2";
      icon.className = "material-symbols-outlined";
      icon.style.color = "#0066ff";
      icon.style.fontSize = "28px";
      flyer.appendChild(icon);
    }

    layer.appendChild(flyer);
    document.body.appendChild(layer);

    const deltaX = to.x - from.x;
    const deltaY = to.y - from.y;
    const midX = deltaX * 0.45;
    const midY = deltaY * 0.45 - 48;

    const animation = flyer.animate(
      [
        {
          transform: "translate(-50%, -50%) scale(1)",
          opacity: 1,
        },
        {
          transform: `translate(calc(-50% + ${midX}px), calc(-50% + ${midY}px)) scale(0.82)`,
          opacity: 1,
          offset: 0.55,
        },
        {
          transform: `translate(calc(-50% + ${deltaX}px), calc(-50% + ${deltaY}px)) scale(0.15)`,
          opacity: 0.3,
        },
      ],
      {
        duration: FLY_DURATION_MS,
        easing: "cubic-bezier(0.22, 0.85, 0.28, 1)",
        fill: "forwards",
      }
    );

  animation.addEventListener("finish", () => {
      layer.remove();
      resolve();
    });
    animation.addEventListener("cancel", () => {
      layer.remove();
      resolve();
    });
  });
}

export function CartFlyAnimationProvider({ children }) {
  const targetsRef = useRef(new Map());
  const [pulseToken, setPulseToken] = useState(0);

  const registerTarget = useCallback((targetId, element) => {
    if (!targetId) return;
    if (element) {
      targetsRef.current.set(targetId, element);
    } else {
      targetsRef.current.delete(targetId);
    }
  }, []);

  const resolveCartTargetRect = useCallback(() => {
    const priority = ["mobile-cart", "sidebar-cart"];
    for (const targetId of priority) {
      const element = targetsRef.current.get(targetId);
      if (!isElementVisible(element)) continue;
      return element.getBoundingClientRect();
    }
    return null;
  }, []);

  const playFlyToCart = useCallback(
    async ({ imageUrl, sourceElement, fromRect } = {}) => {
      const resolvedFrom = resolveFromRect(sourceElement, fromRect);
      const toRect = resolveCartTargetRect();

      if (!resolvedFrom || !toRect) {
        setPulseToken((token) => token + 1);
        return;
      }

      if (prefersReducedMotion()) {
        setPulseToken((token) => token + 1);
        return;
      }

      await runFlyAnimation(imageUrl, resolvedFrom, toRect);
      setPulseToken((token) => token + 1);
    },
    [resolveCartTargetRect]
  );

  const value = useMemo(
    () => ({
      registerTarget,
      playFlyToCart,
      pulseToken,
    }),
    [playFlyToCart, pulseToken, registerTarget]
  );

  return (
    <CartFlyAnimationContext.Provider value={value}>{children}</CartFlyAnimationContext.Provider>
  );
}

export function useCartFlyAnimation() {
  const context = useContext(CartFlyAnimationContext);
  if (!context) {
    throw new Error("useCartFlyAnimation must be used inside CartFlyAnimationProvider");
  }
  return context;
}

export function useCartFlyTarget(targetId) {
  const { registerTarget } = useCartFlyAnimation();

  return useCallback(
    (element) => {
      registerTarget(targetId, element);
    },
    [registerTarget, targetId]
  );
}
