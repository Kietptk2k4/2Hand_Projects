import { useCallback, useState } from "react";
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
  isCartItemInvalid,
} from "../utils/cartDisplay";
import { APP_ROUTES } from "../../../shared/constants/routes";

const CHECKOUT_BLOCKED_TOAST = "Không thể thanh toán. Vui lòng kiểm tra lại giỏ hàng.";

export function CommerceCartPage() {
  const navigate = useNavigate();
  const [toastMessage, setToastMessage] = useState("");
  const [isCheckingOut, setIsCheckingOut] = useState(false);
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

  const goToCheckout = useCallback(async () => {
    if (!cart) return;

    const eligible = getEligibleCartItems(cart);
    if (!eligible.length) {
      setToastMessage(CHECKOUT_BLOCKED_TOAST);
      return;
    }

    setIsCheckingOut(true);
    try {
      const eligibleIds = eligible.map((item) => item.cartItemId);
      const result = await validate(eligibleIds);
      if (!result?.canCheckout) {
        await revalidate(eligibleIds);
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
  }, [cart, navigate, revalidate, validate]);

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

  const eligibleCartItems = cart ? getEligibleCartItems(cart) : [];
  const canCheckout = Boolean(
    cart?.summary?.canCheckout && eligibleCartItems.length > 0 && !isValidating
  );

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
              {cart.items.map((item) => (
                <CartItemRow
                  key={item.cartItemId}
                  item={item}
                  isMutating={isMutating && mutatingItemId === item.cartItemId}
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
