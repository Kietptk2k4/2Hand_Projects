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
import { CommerceFooterTrustSection } from "../components/CommerceFooterTrustSection";
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
        {/* Header & Cart Navigation */}
        <header className="mb-6 flex flex-wrap items-center justify-between gap-4 border-b border-outline-variant/60 pb-4">
          <div>
            <h1 className="text-xl font-black text-on-surface sm:text-2xl">
              XÁC NHẬN & THANH TOÁN ĐƠN HÀNG
            </h1>
            <p className="mt-1 flex items-center gap-1.5 text-xs font-semibold text-on-surface-variant">
              <span className="material-symbols-outlined text-base text-primary" aria-hidden="true">
                lock
              </span>
              Bảo mật 100% bằng mã hóa SSL/TLS 256-bit
            </p>
          </div>
          <Link
            to={APP_ROUTES.commerceCart}
            className="inline-flex items-center gap-1 text-xs font-bold text-primary hover:underline cursor-pointer"
          >
            <span className="material-symbols-outlined text-sm">arrow_back</span>
            Quay lại Giỏ hàng
          </Link>
        </header>

        {/* Disclaimer Alert Box */}
        <div
          className="mb-6 rounded-2xl border border-blue-100 bg-blue-50/60 p-4 text-xs font-semibold text-blue-900 shadow-xs flex items-center gap-2"
          role="note"
        >
          <span className="material-symbols-outlined text-primary text-lg shrink-0">info</span>
          <span>{QUOTE_DISCLAIMER}</span>
        </div>

        {showSkeleton ? <CheckoutSkeleton /> : null}

        {!showSkeleton && isEmptyAddresses ? (
          <div className="rounded-2xl border border-outline-variant bg-surface-container-lowest p-12 text-center shadow-xs">
            <span className="material-symbols-outlined mb-3 text-5xl text-outline" aria-hidden="true">
              location_off
            </span>
            <p className="text-sm font-semibold text-on-surface-variant">
              Bạn chưa có địa chỉ giao hàng. Vui lòng thêm địa chỉ để tiến hành đặt hàng.
            </p>
            <button
              type="button"
              onClick={openAddressModal}
              className="mt-6 rounded-xl bg-primary px-8 py-3 text-xs font-bold text-on-primary hover:bg-[#0050cb] cursor-pointer shadow-xs active:scale-95"
            >
              + Thêm địa chỉ mới
            </button>
            <Link
              to={APP_ROUTES.commerceCart}
              className="mt-4 block text-xs font-bold text-primary hover:underline"
            >
              Quay lại giỏ hàng
            </Link>
          </div>
        ) : null}

        {showCheckoutForm ? (
          <>
            <div className="grid grid-cols-1 items-start gap-8 lg:grid-cols-12 mb-8">
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
                  <div className="rounded-2xl border border-error/30 bg-error-container/40 p-4 shadow-xs">
                    <p className="text-xs font-bold text-on-error-container">{quoteError}</p>
                    <button
                      type="button"
                      onClick={refreshQuote}
                      className="mt-2 text-xs font-bold text-primary hover:underline cursor-pointer"
                    >
                      Thử lại
                    </button>
                  </div>
                ) : null}

                {submitError ? (
                  <div className="rounded-2xl border border-error/30 bg-error-container/40 p-4 shadow-xs">
                    <p className="text-xs font-bold text-on-error-container">{submitError}</p>
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

            {/* E-Commerce Trust Badges Footer */}
            <CommerceFooterTrustSection />
          </>
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
