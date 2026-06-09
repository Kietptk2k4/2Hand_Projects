import { useLocation } from "react-router-dom";
import { CommerceBuyerSidebar } from "./CommerceBuyerSidebar";
import { CommerceSellerSidebar } from "./CommerceSellerSidebar";
import { CommerceMobileCartButton } from "./CommerceMobileCartButton";
import { useCartFlyAnimation } from "../context/CartFlyAnimationContext";
import { isCommerceSellerShellPath } from "../utils/commerceRoutes";

/** Offset for AppHeader (`h-16` = 4rem) */
const STICKY_TOP = "top-16";
const STICKY_MAX_HEIGHT = "max-h-[calc(100vh-4rem)]";

export function CommerceShell({ children, onComingSoon, showHomeSidebar = true }) {
  const { pathname } = useLocation();
  const { pulseToken } = useCartFlyAnimation();
  const useSellerSidebar = isCommerceSellerShellPath(pathname);

  return (
    <div className="flex w-full items-start bg-surface-container-lowest">
      {showHomeSidebar ? (
        <div
          className={[
            "hidden shrink-0 lg:sticky lg:z-30 lg:block lg:self-start",
            STICKY_TOP,
            STICKY_MAX_HEIGHT,
          ].join(" ")}
        >
          {useSellerSidebar ? (
            <CommerceSellerSidebar onComingSoon={onComingSoon} />
          ) : (
            <CommerceBuyerSidebar onComingSoon={onComingSoon} pulseToken={pulseToken} />
          )}
        </div>
      ) : null}
      <CommerceMobileCartButton pulseToken={pulseToken} />
      <div className="min-w-0 flex-1 px-4 py-8 md:px-8">{children}</div>
    </div>
  );
}
