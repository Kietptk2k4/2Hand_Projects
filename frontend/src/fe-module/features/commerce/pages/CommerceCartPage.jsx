import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { CartEmptyState } from "../components/CartEmptyState";
import { CartInvalidItemsBanner } from "../components/CartInvalidItemsBanner";
import { CartItemRow } from "../components/CartItemRow";
import { CartOrderSummary } from "../components/CartOrderSummary";
import { CartSkeleton } from "../components/CartSkeleton";
import { CartTableHeader } from "../components/CartTableHeader";
import { CartShopGroupHeader } from "../components/CartShopGroupHeader";
import { CommerceFooterTrustSection } from "../components/CommerceFooterTrustSection";
import { CommerceShell } from "../components/CommerceShell";
import { useCart } from "../hooks/useCart";
import { useValidateCartItems } from "../hooks/useValidateCartItems";
import { fetchShopProducts } from "../api/shopProductsApi";
import { mapShopProductsResponse } from "../utils/shopProductsMapper";
import {
  getCartItemCountLabel,
  getDisplayShopName,
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
  const [shopNamesMap, setShopNamesMap] = useState({});
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

  // Dynamically fetch actual shop names for any shopId in cart
  useEffect(() => {
    if (!cart?.items?.length) return;

    const shopIds = Array.from(
      new Set(cart.items.map((item) => item.shopId || item.sellerId).filter(Boolean))
    );

    shopIds.forEach((shopId) => {
      const existing = cart.items.find(
        (i) => (i.shopId === shopId || i.sellerId === shopId) && i.shopName && !i.shopName.startsWith("Shop ")
      );
      if (existing?.shopName) {
        setShopNamesMap((prev) => ({ ...prev, [shopId]: existing.shopName }));
        return;
      }

      fetchShopProducts({ shopId, limit: 1 })
        .then((raw) => {
          const mapped = mapShopProductsResponse(raw);
          if (mapped?.shop?.shopName) {
            setShopNamesMap((prev) => ({
              ...prev,
              [shopId]: mapped.shop.shopName,
            }));
          }
        })
        .catch(() => {});
    });
  }, [cart?.items]);

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

  // Group cart items by shop using dynamically resolved shop names
  const shopGroups = useMemo(() => {
    if (!cart?.items) return [];
    const map = new Map();
    cart.items.forEach((item) => {
      const shopId = item.shopId || item.sellerId || "shop-2hand";
      const shopName = getDisplayShopName(item, shopNamesMap);
      if (!map.has(shopId)) {
        map.set(shopId, { shopId, shopName, items: [] });
      }
      map.get(shopId).items.push(item);
    });
    return Array.from(map.values());
  }, [cart?.items, shopNamesMap]);

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

  const handleRemoveAllInvalid = useCallback(() => {
    if (!cart?.items) return;
    const invalidItems = cart.items.filter((item) => item.validateMessage || item.unavailableReason);
    invalidItems.forEach((item) => removeItem(item.cartItemId));
  }, [cart?.items, removeItem]);

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
      {/* Header */}
      <header className="mb-6 flex flex-wrap items-baseline justify-between gap-2 border-b border-outline-variant/60 pb-4">
        <div>
          <h1 className="text-xl font-black text-on-surface sm:text-2xl">
            GIỎ HÀNG CỦA BẠN
          </h1>
          {cart ? (
            <p className="mt-1 text-xs font-semibold text-on-surface-variant">
              {getCartItemCountLabel(cart)}
            </p>
          ) : null}
        </div>
      </header>

      {isLoading ? <CartSkeleton /> : null}

      {!isLoading && errorMessage ? (
        <div className="rounded-2xl border border-error/30 bg-error-container/40 p-8 text-center shadow-xs">
          <p className="text-sm font-semibold text-on-error-container">{errorMessage}</p>
          <button
            type="button"
            onClick={retry}
            className="mt-4 rounded-xl bg-primary px-6 py-2.5 text-xs font-bold text-on-primary hover:bg-[#0050cb]"
          >
            Thử lại ngay
          </button>
        </div>
      ) : null}

      {!isLoading && !errorMessage && isEmpty ? <CartEmptyState /> : null}

      {!isLoading && !errorMessage && cart && !isEmpty ? (
        <>
          {/* Invalid Items Banner */}
          <CartInvalidItemsBanner
            items={cart.items}
            onRemoveInvalidItems={handleRemoveAllInvalid}
          />

          <div className="grid grid-cols-1 items-start gap-6 lg:grid-cols-12">
            {/* Main Cart Items Table (Column 8) */}
            <div className="flex flex-col gap-6 lg:col-span-8">
              {/* Desktop Table Header */}
              <CartTableHeader
                allEligibleSelected={allEligibleSelected}
                eligibleCount={eligibleIds.length}
                isMutating={isMutating}
                onToggleSelectAll={toggleSelectAllEligible}
                selectAllRef={selectAllRef}
              />

              {/* Items Grouped by Shop */}
              <div className="space-y-6">
                {shopGroups.map((group) => (
                  <div key={group.shopId} className="rounded-2xl shadow-xs overflow-hidden">
                    <CartShopGroupHeader shopId={group.shopId} shopName={group.shopName} />
                    {group.items.map((item, idx) => (
                      <CartItemRow
                        key={item.cartItemId}
                        item={item}
                        selected={selectedIds.has(item.cartItemId)}
                        canSelect={isCartItemCheckoutEligible(item)}
                        isMutating={isMutating && mutatingItemId === item.cartItemId}
                        isLastInGroup={idx === group.items.length - 1}
                        onToggleSelect={toggleSelectItem}
                        onOpenProduct={openProduct}
                        onRemove={removeItem}
                        onDecrease={() => handleDecrease(item)}
                        onIncrease={() => handleIncrease(item)}
                      />
                    ))}
                  </div>
                ))}
              </div>
            </div>

            {/* Order Summary Sidebar (Column 4) */}
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

          {/* Trust Guarantees */}
          <CommerceFooterTrustSection />
        </>
      ) : null}

      <FeedToast message={toastMessage} onDismiss={dismissToast} />
    </CommerceShell>
  );
}
