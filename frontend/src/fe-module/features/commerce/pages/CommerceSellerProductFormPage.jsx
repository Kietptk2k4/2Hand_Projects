import { useCallback, useState } from "react";
import { Link, useNavigate, useParams, useSearchParams } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { CommerceShell } from "../components/CommerceShell";
import { SellerProductInfoStep } from "../components/SellerProductInfoStep";
import { SellerProductMediaAttributesStep } from "../components/SellerProductMediaAttributesStep";
import { SellerProductPricingInventoryStep } from "../components/SellerProductPricingInventoryStep";
import { SellerProductReviewStep } from "../components/SellerProductReviewStep";
import { SellerProductWizardFooter } from "../components/SellerProductWizardFooter";
import { SellerProductWizardLayout } from "../components/SellerProductWizardLayout";
import { CommerceFooterTrustSection } from "../components/CommerceFooterTrustSection";
import { useSellerProductForm } from "../hooks/useSellerProductForm";
import { APP_ROUTES } from "../../../shared/constants/routes";

export function CommerceSellerProductFormPage({ mode: modeProp }) {
  const { productId: routeProductId } = useParams();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const mode = modeProp || (routeProductId ? "edit" : "create");
  const initialStep = searchParams.get("step");

  const [toastMessage, setToastMessage] = useState("");

  const wizard = useSellerProductForm({ mode, productId: routeProductId, initialStep });
  const {
    step,
    form,
    attributes,
    setAttributes,
    mediaUrls,
    setMediaUrls,
    status,
    fieldErrors,
    apiError,
    isLoading,
    isSubmitting,
    lastSavedAt,
    canEdit,
    maxUnlockedStep,
    categories,
    brands,
    reviewChecklist,
    canPublish,
    updateField,
    saveDraftShortcut,
    publishProductAction,
    goNext,
    goBack,
    goToStep,
    productId,
  } = wizard;

  const listPath = APP_ROUTES.commerceSellerProducts;
  const isEdit = mode === "edit";
  const pageTitle = isEdit ? "Sửa sản phẩm" : "Tạo sản phẩm";
  const disabled = !canEdit || isSubmitting;

  const navigateToList = useCallback(() => {
    navigate(listPath, { replace: true });
  }, [listPath, navigate]);

  const handleSaveDraft = useCallback(async () => {
    const result = await saveDraftShortcut();
    if (result === false) return;
    if (result === "no_changes") {
      setToastMessage("Không có thay đổi");
      return;
    }
    setToastMessage("Đã lưu nháp");
    if (step === 4 || !productId) {
      setTimeout(navigateToList, 600);
    }
  }, [navigateToList, productId, saveDraftShortcut, step]);

  const handleNext = useCallback(async () => {
    if (step === 4) {
      navigateToList();
      return;
    }
    const ok = await goNext();
    if (ok && step === 1 && !isEdit) {
      setToastMessage("Đã tạo bản nháp — tiếp tục thiết lập giá & kho");
    }
  }, [goNext, isEdit, navigateToList, step]);

  const handlePublish = useCallback(async () => {
    const result = await publishProductAction();
    if (!result?.ok) return;
    setToastMessage("Đăng bán sản phẩm thành công");
    setTimeout(navigateToList, 700);
  }, [navigateToList, publishProductAction]);

  const breadcrumb = (
    <nav className="flex flex-wrap items-center text-xs font-semibold text-on-surface-variant">
      <Link to={listPath} className="transition-colors hover:text-primary">
        Quản lý sản phẩm
      </Link>
      <span className="material-symbols-outlined mx-1 text-sm">chevron_right</span>
      <span className="font-bold text-on-surface">{pageTitle}</span>
    </nav>
  );

  if (isLoading) {
    return (
      <CommerceShell>
        <div className="mx-auto max-w-6xl px-4 py-16 text-center text-xs font-bold text-on-surface-variant">
          Đang tải thông tin sản phẩm...
        </div>
      </CommerceShell>
    );
  }

  return (
    <CommerceShell onComingSoon={() => setToastMessage("Tính năng đang được phát triển.")}>
      <div className="pb-24">
        {!canEdit ? (
          <div className="mx-auto max-w-6xl px-4 pt-6">
            <div className="rounded-2xl border border-outline-variant bg-surface-container-high p-4 text-xs font-bold text-on-surface shadow-xs">
              Sản phẩm đã lưu trữ — Chỉ xem thông tin, không thể chỉnh sửa.
            </div>
          </div>
        ) : null}

        <SellerProductWizardLayout
          title={pageTitle}
          breadcrumb={breadcrumb}
          status={status}
          lastSavedAt={lastSavedAt}
          currentStep={step}
          maxUnlockedStep={maxUnlockedStep}
          canEdit={canEdit}
          onStepClick={(n) => goToStep(n)}
          onSaveDraft={handleSaveDraft}
          isSubmitting={isSubmitting}
        >
          {apiError && step !== 4 ? (
            <div className="mb-4 rounded-2xl border border-error/30 bg-error-container/40 p-4 shadow-xs" role="alert">
              <p className="text-xs font-bold text-on-error-container">{apiError}</p>
            </div>
          ) : null}

          {step === 1 ? (
            <SellerProductInfoStep
              form={form}
              fieldErrors={fieldErrors}
              categories={categories}
              brands={brands}
              disabled={disabled}
              onFieldChange={updateField}
            />
          ) : null}

          {step === 2 ? (
            <SellerProductPricingInventoryStep
              form={form}
              fieldErrors={fieldErrors}
              disabled={disabled}
              onFieldChange={updateField}
            />
          ) : null}

          {step === 3 ? (
            <SellerProductMediaAttributesStep
              productId={productId}
              mediaUrls={mediaUrls}
              attributes={attributes}
              fieldErrors={fieldErrors}
              disabled={disabled}
              onMediaChange={setMediaUrls}
              onAttributesChange={setAttributes}
            />
          ) : null}

          {step === 4 ? (
            <>
              <SellerProductReviewStep
                form={form}
                categories={categories}
                brands={brands}
                mediaUrls={mediaUrls}
                attributes={attributes}
                status={status}
                reviewChecklist={reviewChecklist}
                canPublish={canPublish}
                apiError={apiError}
              />
              <div className="mt-6 flex flex-wrap justify-end gap-3">
                {(status === "DRAFT" || status === "PAUSED") && canEdit ? (
                  <button
                    type="button"
                    onClick={handlePublish}
                    disabled={!canPublish || isSubmitting}
                    className="rounded-xl bg-primary px-8 py-3 text-xs font-bold text-on-primary shadow-xs transition-all hover:bg-[#0050cb] active:scale-95 disabled:opacity-50 cursor-pointer"
                  >
                    {isSubmitting ? "Đang xử lý..." : "Đăng bán sản phẩm ngay"}
                  </button>
                ) : null}
                <button
                  type="button"
                  onClick={navigateToList}
                  className="rounded-xl border border-outline-variant px-6 py-3 text-xs font-bold text-on-surface hover:bg-surface-container-low cursor-pointer"
                >
                  {status === "ACTIVE" || status === "OUT_OF_STOCK" ? "Hoàn tất" : "Quay về danh sách"}
                </button>
              </div>
            </>
          ) : (
            <SellerProductWizardFooter
              step={step}
              canEdit={canEdit}
              isSubmitting={isSubmitting}
              onBack={goBack}
              onSaveDraft={handleSaveDraft}
              onNext={handleNext}
              nextLabel={step === 3 ? "Xem lại" : "Tiếp theo"}
            />
          )}
        </SellerProductWizardLayout>

        {/* E-Commerce Trust Badges Footer */}
        <div className="mx-auto max-w-6xl px-4 mt-8">
          <CommerceFooterTrustSection />
        </div>
      </div>

      <FeedToast message={toastMessage} onDismiss={() => setToastMessage("")} />
    </CommerceShell>
  );
}
