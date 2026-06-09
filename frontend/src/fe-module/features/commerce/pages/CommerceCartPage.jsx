import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { CartEmptyState } from "../components/CartEmptyState";
import { CartInvalidItemsBanner } from "../components/CartInvalidItemsBanner";
import { CartItemRow } from "../components/CartItemRow";
import { CartOrderSummary } from "../components/CartOrderSummary";
import { CartSkeleton } from "../components/CartSkeleton";
import { CartWarningsBanner } from "../components/CartWarningsBanner";
import { CommerceShell } from "../components/CommerceShell";
import { useCart } from "../hooks/useCart";
import { useValidateCartItems } from "../hooks/useValidateCartItems";
import {
  getCartItemCountLabel,
  getEligibleCartItems,
  getSelectedEligibleCartItems,
  isCartItemCheckoutEligible,
  isCartItemInvalid,
} from "../utils/cartDisplay";
import { APP_ROUTES } from "../../../shared/constants/routes";

const CHECKOUT_BLOCKED_TOAST = "Không thể thanh toán. Vui lòng kiểm tra lại giỏ hàng.";
const CHECKOUT_NO_SELECTION_TOAST = "Vui lòng chọn ít nhất một sản phẩm để thanh toán.";

export function CommerceCartPage() {
  const navigate = useNavigate();
  const [toastMessage, setToastMessage] = useState("");
  const [isCheckingOut, setIsCheckingOut] = useState(false);
  const [selectedIds, setSelectedIds] = useState(() => new Set());
  const selectAllRef = useRef(null);
  const { validate, isValidating } = useValidateCartItems();
  const {
    cart,
    isLoading,
    isEmpty,
    errorMessage,
    isMutating,
    mutatingItemId,
    updateQuantity,
    removeItem,
    retry,
    revalidate,
  } = useCart();

  const showComingSoon = useCallback(() => {
    setToastMessage("Tính năng đang được phát triển.");
  }, []);

  const eligibleCartItems = useMemo(
    () => (cart ? getEligibleCartItems(cart) : []),
    [cart]
  );

  const eligibleIds = useMemo(
    () => eligibleCartItems.map((item) => item.cartItemId),
    [eligibleCartItems]
  );

  const cartItemSignature = useMemo(
    () =>
      cart?.items
        ?.map((item) => `${item.cartItemId}:${item.quantity}:${item.status}:${item.unavailableReason ?? ""}`)
        .join("|") ?? "",
    [cart?.items]
  );

  useEffect(() => {
    if (!cart?.items?.length) {
      setSelectedIds(new Set());
      return;
    }

    setSelectedIds((prev) => {
      const eligibleSet = new Set(eligibleIds);
      const kept = [...prev].filter((id) => eligibleSet.has(id));
      if (kept.length > 0) return new Set(kept);
      return new Set(eligibleIds);
    });
  }, [cart?.items?.length, cartItemSignature, eligibleIds]);

  const selectedEligibleItems = useMemo(
    () => getSelectedEligibleCartItems(cart, selectedIds),
    [cart, selectedIds]
  );

  const allEligibleSelected =
    eligibleIds.length > 0 && eligibleIds.every((id) => selectedIds.has(id));

  const someEligibleSelected =
    eligibleIds.some((id) => selectedIds.has(id)) && !allEligibleSelected;

  useEffect(() => {
    if (selectAllRef.current) {
      selectAllRef.current.indeterminate = someEligibleSelected;
    }
  }, [someEligibleSelected]);

  const toggleSelectItem = useCallback((cartItemId) => {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(cartItemId)) {
        next.delete(cartItemId);
      } else {
        next.add(cartItemId);
      }
      return next;
    });
  }, []);

  const toggleSelectAllEligible = useCallback(() => {
    setSelectedIds((prev) => {
      if (allEligibleSelected) return new Set();
      return new Set(eligibleIds);
    });
  }, [allEligibleSelected, eligibleIds]);

  const goToCheckout = useCallback(async () => {
    if (!cart) return;

    if (!selectedEligibleItems.length) {
      setToastMessage(CHECKOUT_NO_SELECTION_TOAST);
      return;
    }

    setIsCheckingOut(true);
    try {
      const checkoutIds = selectedEligibleItems.map((item) => item.cartItemId);
      const result = await validate(checkoutIds);
      if (!result?.canCheckout) {
        await revalidate(checkoutIds);
        setToastMessage(CHECKOUT_BLOCKED_TOAST);
        return;
      }

      const validIds = result.validItems.map((entry) => entry.cartItemId);
      const validItems = cart.items.filter((item) => validIds.includes(item.cartItemId));

      navigate(APP_ROUTES.commerceCheckout, {
        state: {
          cartItemIds: validIds,
          cartItemsCache: validItems,
        },
      });
    } catch {
      setToastMessage(CHECKOUT_BLOCKED_TOAST);
    } finally {
      setIsCheckingOut(false);
    }
  }, [cart, navigate, revalidate, selectedEligibleItems, validate]);

  const dismissToast = useCallback(() => {
    setToastMessage("");
  }, []);

  const openProduct = useCallback(
    (productId) => {
      if (!productId) return;
      navigate(APP_ROUTES.commerceProductDetail.replace(":productId", productId));
    },
    [navigate]
  );

  const handleDecrease = useCallback(
    (item) => {
      if (isCartItemInvalid(item)) return;
      updateQuantity(item.cartItemId, item.quantity - 1);
    },
    [updateQuantity]
  );

  const handleIncrease = useCallback(
    (item) => {
      if (isCartItemInvalid(item)) return;
      if (item.quantity >= item.availableQuantity) return;
      updateQuantity(item.cartItemId, item.quantity + 1);
    },
    [updateQuantity]
  );

  const canCheckout = Boolean(selectedEligibleItems.length > 0 && !isValidating);

  return (
    <CommerceShell onComingSoon={showComingSoon}>
      <header className="mb-6">
        <h1 className="text-headline-lg-mobile font-semibold text-on-surface md:text-headline-lg">
          Giỏ hàng
        </h1>
        {cart ? (
          <p className="mt-1 text-sm text-on-surface-variant">{getCartItemCountLabel(cart)}</p>
        ) : null}
      </header>

      {isLoading ? <CartSkeleton /> : null}

      {!isLoading && errorMessage ? (
        <div className="rounded-xl border border-error/30 bg-error-container/40 p-6 text-center">
          <p className="text-sm text-on-error-container">{errorMessage}</p>
          <button
            type="button"
            onClick={retry}
            className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-medium text-on-primary hover:bg-[#0050cb]"
          >
            Thử lại
          </button>
        </div>
      ) : null}

      {!isLoading && !errorMessage && isEmpty ? <CartEmptyState /> : null}

      {!isLoading && !errorMessage && cart && !isEmpty ? (
        <>
          <CartWarningsBanner warnings={cart.summary.warnings} />
          <CartInvalidItemsBanner items={cart.items} />

          <div className="grid grid-cols-1 items-start gap-8 lg:grid-cols-12">
            <div className="flex flex-col gap-4 lg:col-span-8">
              <div className="flex items-center gap-3 rounded-lg border border-outline-variant bg-surface-container-lowest px-4 py-3">
                <input
                  ref={selectAllRef}
                  type="checkbox"
                  checked={allEligibleSelected}
                  disabled={eligibleIds.length === 0 || isMutating}
                  onChange={toggleSelectAllEligible}
                  className="h-4 w-4 rounded border-outline-variant text-primary disabled:opacity-40"
                  aria-label="Chọn tất cả sản phẩm có thể thanh toán"
                />
                <span className="text-sm text-on-surface">
                  Chọn tất cả ({eligibleIds.length} sản phẩm)
                </span>
              </div>

              {cart.items.map((item) => (
                <CartItemRow
                  key={item.cartItemId}
                  item={item}
                  selected={selectedIds.has(item.cartItemId)}
                  canSelect={isCartItemCheckoutEligible(item)}
                  isMutating={isMutating && mutatingItemId === item.cartItemId}
                  onToggleSelect={toggleSelectItem}
                  onOpenProduct={openProduct}
                  onRemove={removeItem}
                  onDecrease={() => handleDecrease(item)}
                  onIncrease={() => handleIncrease(item)}
                />
              ))}
            </div>

            <div className="lg:col-span-4">
              <CartOrderSummary
                cart={cart}
                selectedItems={selectedEligibleItems}
                isMutating={isMutating || isCheckingOut || isValidating}
                canCheckout={canCheckout}
                onCheckout={goToCheckout}
              />
            </div>
          </div>
        </>
      ) : null}

      <FeedToast message={toastMessage} onDismiss={dismissToast} />
    </CommerceShell>
  );
}
