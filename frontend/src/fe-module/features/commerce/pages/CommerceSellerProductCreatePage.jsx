import { useCallback, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { CommerceShell } from "../components/CommerceShell";
import { CreateSellerProductInfoStep } from "../components/CreateSellerProductInfoStep";
import { CreateSellerProductPricingStep } from "../components/CreateSellerProductPricingStep";
import { CreateSellerProductStepper } from "../components/CreateSellerProductStepper";
import { useCreateSellerProduct } from "../hooks/useCreateSellerProduct";
import { APP_ROUTES } from "../../../shared/constants/routes";

export function CommerceSellerProductCreatePage() {
  const navigate = useNavigate();
  const [toastMessage, setToastMessage] = useState("");

  const {
    step,
    form,
    fieldErrors,
    apiError,
    isSubmitting,
    categories,
    updateField,
    nextStep,
    prevStep,
    saveDraft,
    saveAndPublish,
  } = useCreateSellerProduct();

  const listPath = APP_ROUTES.commerceSellerProducts;

  const handleCancel = useCallback(() => {
    if (window.history.length > 1) navigate(-1);
    else navigate(listPath);
  }, [listPath, navigate]);

  const handleDraft = useCallback(async () => {
    const result = await saveDraft();
    if (!result?.productId) return;

    setToastMessage("Tạo sản phẩm nháp thành công");
    if (result.publishError) {
      setToastMessage(`Tạo nháp thành công. ${result.publishError}`);
    }
    setTimeout(() => navigate(listPath, { replace: true }), 700);
  }, [listPath, navigate, saveDraft]);

  const handlePublish = useCallback(async () => {
    const result = await saveAndPublish();
    if (!result?.productId) return;

    if (result.publishError) {
      setToastMessage(`Đã lưu nháp. ${result.publishError}`);
    } else {
      setToastMessage("Đăng bán sản phẩm thành công");
    }
    setTimeout(() => navigate(listPath, { replace: true }), 700);
  }, [listPath, navigate, saveAndPublish]);

  return (
    <CommerceShell onComingSoon={() => setToastMessage("Tính năng đang được phát triển.")}>
      <div className="mx-auto w-full max-w-3xl px-4 py-8">
        <nav className="mb-4 text-body-sm text-on-surface-variant">
          <Link to={listPath} className="hover:text-primary">
            Quản lý sản phẩm
          </Link>
          <span className="mx-1">›</span>
          <span className="text-on-surface">Tạo sản phẩm</span>
        </nav>

        <header className="mb-6 text-center">
          <h1 className="text-headline-lg-mobile font-bold text-on-surface md:text-headline-lg">
            Thêm sản phẩm mới
          </h1>
        </header>

        <CreateSellerProductStepper currentStep={step} />

        {apiError ? (
          <div className="mb-6 rounded-lg border border-error/30 bg-error-container/40 p-4" role="alert">
            <p className="text-sm text-on-error-container">{apiError}</p>
          </div>
        ) : null}

        {step === 1 ? (
          <CreateSellerProductInfoStep
            form={form}
            fieldErrors={fieldErrors}
            categories={categories}
            disabled={isSubmitting}
            onFieldChange={updateField}
            onCancel={handleCancel}
            onNext={nextStep}
          />
        ) : (
          <CreateSellerProductPricingStep
            form={form}
            fieldErrors={fieldErrors}
            disabled={isSubmitting}
            onFieldChange={updateField}
            onBack={prevStep}
            onSaveDraft={handleDraft}
            onPublish={handlePublish}
          />
        )}
      </div>

      <FeedToast message={toastMessage} onDismiss={() => setToastMessage("")} />
    </CommerceShell>
  );
}
