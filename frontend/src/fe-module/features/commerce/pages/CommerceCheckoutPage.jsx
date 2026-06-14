import { useCallback, useEffect, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { CheckoutAddressSelector } from "../components/CheckoutAddressSelector";
import { CheckoutPaymentMethod } from "../components/CheckoutPaymentMethod";
import { CheckoutQuoteSummary } from "../components/CheckoutQuoteSummary";
import { CheckoutShipmentOptions } from "../components/CheckoutShipmentOptions";
import { CheckoutSkeleton } from "../components/CheckoutSkeleton";
import { UserAddressFormModal } from "../components/UserAddressFormModal";
import { CommerceShell } from "../components/CommerceShell";
import { QUOTE_DISCLAIMER } from "../constants/checkoutConstants";
import { useCheckout } from "../hooks/useCheckout";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { useCartBadge } from "../context/CartBadgeContext";

export function CommerceCheckoutPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { refetch: refetchCartBadge } = useCartBadge();
  const [toastMessage, setToastMessage] = useState("");
  const [isAddressModalOpen, setIsAddressModalOpen] = useState(false);

  const cartItemIds = location.state?.cartItemIds || [];
  const cartItemsCache = location.state?.cartItemsCache || [];

  useEffect(() => {
    if (!cartItemIds.length) {
      navigate(APP_ROUTES.commerceCart, { replace: true });
    }
  }, [cartItemIds.length, navigate]);

  const {
    addresses,
    addressLabelVersion,
    selectedAddressId,
    paymentMethod,
    quote,
    shippingFee,
    quoteError,
    submitError,
    isSubmitting,
    isLoadingAddresses,
    isLoadingQuote,
    isEmptyAddresses,
    canSubmit,
    selectAddress,
    selectPayment,
    refreshQuote,
    submitOrder,
    createAddress,
    isCreatingAddress,
  } = useCheckout(cartItemIds);

  const openAddressModal = useCallback(() => {
    setIsAddressModalOpen(true);
  }, []);

  const closeAddressModal = useCallback(() => {
    setIsAddressModalOpen(false);
  }, []);

  const handleCreateAddress = useCallback(
    async (formValues) => {
      await createAddress(formValues);
      setToastMessage("Đã thêm địa chỉ giao hàng.");
    },
    [createAddress]
  );

  const dismissToast = useCallback(() => {
    setToastMessage("");
  }, []);

  const handlePlaceOrder = useCallback(async () => {
    const result = await submitOrder();
    if (!result) return;

    if (result.redirect) {
      refetchCartBadge();
      window.location.assign(result.redirect);
      return;
    }

    refetchCartBadge();
    navigate(APP_ROUTES.commerceCheckoutSuccess, {
      replace: true,
      state: {
        orderId: result.orderId,
        paymentId: result.paymentId,
        finalAmount: result.finalAmount,
        paymentMethod: result.paymentMethod,
        orderStatus: result.orderStatus,
        paymentStatus: result.paymentStatus,
      },
    });
  }, [navigate, refetchCartBadge, submitOrder]);

  if (!cartItemIds.length) {
    return null;
  }

  const showSkeleton = isLoadingAddresses;
  const showCheckoutForm = !showSkeleton && !isEmptyAddresses;

  return (
    <CommerceShell showHomeSidebar={false}>
      <div className="mx-auto w-full max-w-[1280px]">
        <header className="mb-6 flex flex-wrap items-center justify-between gap-4">
          <div>
            <h1 className="text-headline-lg-mobile font-bold text-on-surface md:text-headline-lg">
              Thanh toán an toàn
            </h1>
            <p className="mt-1 flex items-center gap-1 text-sm text-on-surface-variant">
              <span className="material-symbols-outlined text-base text-primary" aria-hidden="true">
                lock
              </span>
              Thông tin được bảo mật
            </p>
          </div>
          <Link
            to={APP_ROUTES.commerceCart}
            className="text-sm font-medium text-primary hover:underline"
          >
            Quay lại giỏ hàng
          </Link>
        </header>

        <div
          className="mb-6 rounded-lg border border-surface-tint/30 bg-surface-container-low p-3 text-sm text-on-surface"
          role="note"
        >
          {QUOTE_DISCLAIMER}
        </div>

        {showSkeleton ? <CheckoutSkeleton /> : null}

        {!showSkeleton && isEmptyAddresses ? (
          <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-10 text-center">
            <span className="material-symbols-outlined mb-2 text-4xl text-outline" aria-hidden="true">
              location_off
            </span>
            <p className="text-sm text-on-surface-variant">
              Bạn chưa có địa chỉ giao hàng.
            </p>
            <button
              type="button"
              onClick={openAddressModal}
              className="mt-6 rounded-lg bg-primary px-6 py-2.5 text-sm font-medium text-on-primary hover:bg-[#0050cb]"
            >
              Thêm địa chỉ
            </button>
            <Link
              to={APP_ROUTES.commerceCart}
              className="mt-4 block text-sm text-primary hover:underline"
            >
              Quay lại giỏ hàng
            </Link>
          </div>
        ) : null}

        {showCheckoutForm ? (
          <div className="grid grid-cols-1 items-start gap-8 lg:grid-cols-12">
            <div className="flex flex-col gap-6 lg:col-span-7">
              <CheckoutAddressSelector
                key={addressLabelVersion}
                addresses={addresses}
                selectedAddressId={selectedAddressId}
                onSelect={selectAddress}
                onAddNew={openAddressModal}
              />

              <CheckoutShipmentOptions
                quote={quote}
                shippingFee={shippingFee}
                isLoading={isLoadingQuote || !selectedAddressId}
              />

              <CheckoutPaymentMethod
                paymentMethod={paymentMethod}
                disabled={isSubmitting}
                onSelect={selectPayment}
              />

              {quoteError ? (
                <div className="rounded-lg border border-error/30 bg-error-container/40 p-4">
                  <p className="text-sm text-on-error-container">{quoteError}</p>
                  <button
                    type="button"
                    onClick={refreshQuote}
                    className="mt-2 text-sm font-medium text-primary hover:underline"
                  >
                    Thử lại
                  </button>
                </div>
              ) : null}

              {submitError ? (
                <div className="rounded-lg border border-error/30 bg-error-container/40 p-4">
                  <p className="text-sm text-on-error-container">{submitError}</p>
                </div>
              ) : null}
            </div>

            <div className="lg:col-span-5">
              <CheckoutQuoteSummary
                quote={quote}
                cartItemsCache={cartItemsCache}
                isLoading={isLoadingQuote}
                canSubmit={canSubmit}
                isSubmitting={isSubmitting}
                onPlaceOrder={handlePlaceOrder}
              />
            </div>
          </div>
        ) : null}
      </div>

      <UserAddressFormModal
        mode="create"
        open={isAddressModalOpen}
        onClose={closeAddressModal}
        onSubmit={handleCreateAddress}
        isSubmitting={isCreatingAddress}
      />

      <FeedToast message={toastMessage} onDismiss={dismissToast} />
    </CommerceShell>
  );
}
