import { useCallback } from "react";
import { Link, useNavigate } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { CommerceShell } from "../components/CommerceShell";
import { CreateShopBrandStep } from "../components/CreateShopBrandStep";
import { CreateShopPickupStep } from "../components/CreateShopPickupStep";
import { CreateShopStepper } from "../components/CreateShopStepper";
import { useCreateShop } from "../hooks/useCreateShop";
import { useRouteToastMessage } from "../hooks/useRouteToastMessage";
import { useSellerShop } from "../context/SellerShopContext";
import { APP_ROUTES } from "../../../shared/constants/routes";

const COMING_SOON_MESSAGE = "Tính năng đang được phát triển.";

export function CommerceCreateShopPage() {
  const navigate = useNavigate();
  const { toastMessage, setToastMessage, dismissToast } = useRouteToastMessage();

  const {
    step,
    form,
    fieldErrors,
    apiError,
    existingShopId,
    isSubmitting,
    updateField,
    nextStep,
    prevStep,
    submit,
  } = useCreateShop();
  const { reload: reloadSellerShop } = useSellerShop();

  const handleCancel = useCallback(() => {
    if (window.history.length > 1) {
      navigate(-1);
      return;
    }
    navigate(APP_ROUTES.commerceHome);
  }, [navigate]);

  const handleNext = useCallback(() => {
    nextStep();
  }, [nextStep]);

  const handleSubmit = useCallback(async () => {
    const result = await submit();
    if (!result?.shopId) return;

    await reloadSellerShop();
    setToastMessage("Tạo shop thành công");

    setTimeout(() => {
      navigate(APP_ROUTES.commerceSellerProducts, { replace: true });
    }, 800);
  }, [navigate, reloadSellerShop, submit]);

  const showComingSoon = useCallback(() => {
    setToastMessage(COMING_SOON_MESSAGE);
  }, []);

  const existingShopPath = existingShopId
    ? APP_ROUTES.commerceShopProducts.replace(":shopId", existingShopId)
    : null;

  return (
    <CommerceShell onComingSoon={showComingSoon}>
      <div className="mx-auto flex w-full max-w-3xl flex-col items-center px-4 py-8 md:px-8">
        <header className="mb-8 text-center">
          <h1 className="text-headline-lg-mobile font-bold text-primary md:text-headline-lg">
            Tạo shop 2Hands
          </h1>
          <p className="mt-2 text-body-md text-on-surface-variant">
            Thiết lập thương hiệu và địa chỉ lấy hàng để bắt đầu bán trên nền tảng.
          </p>
        </header>

        <CreateShopStepper currentStep={step} />

        {apiError ? (
          <div
            className="mb-6 w-full rounded-lg border border-error/30 bg-error-container/40 p-4"
            role="alert"
          >
            <p className="text-sm text-on-error-container">{apiError}</p>
            {existingShopPath ? (
              <Link to={existingShopPath} className="mt-2 inline-block text-sm font-medium text-primary hover:underline">
                Xem shop của bạn
              </Link>
            ) : null}
          </div>
        ) : null}

        {step === 1 ? (
          <CreateShopBrandStep
            form={form}
            fieldErrors={fieldErrors}
            disabled={isSubmitting}
            onFieldChange={updateField}
            onCancel={handleCancel}
            onNext={handleNext}
          />
        ) : (
          <CreateShopPickupStep
            form={form}
            fieldErrors={fieldErrors}
            disabled={isSubmitting}
            onFieldChange={updateField}
            onBack={prevStep}
            onSubmit={handleSubmit}
          />
        )}

        {step === 1 ? (
          <p className="mt-4 text-center text-body-sm text-on-surface-variant">
            Tiếp theo: Địa chỉ lấy hàng (kho &amp; liên hệ)
          </p>
        ) : null}
      </div>

      <FeedToast message={toastMessage} onDismiss={dismissToast} />
    </CommerceShell>
  );
}
