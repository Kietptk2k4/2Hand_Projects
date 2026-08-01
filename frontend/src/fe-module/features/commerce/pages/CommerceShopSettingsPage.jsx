import { useCallback, useState } from "react";
import { Link } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { CommerceShell } from "../components/CommerceShell";
import { ShopSettingsActionBar } from "../components/ShopSettingsActionBar";
import { ShopSettingsProfileSection } from "../components/ShopSettingsProfileSection";
import { ShopSettingsVacationSection } from "../components/ShopSettingsVacationSection";
import { CommerceFooterTrustSection } from "../components/CommerceFooterTrustSection";
import { useShopSettings } from "../hooks/useShopSettings";
import { APP_ROUTES } from "../../../shared/constants/routes";

function ShopSettingsSkeleton() {
  return (
    <div className="grid animate-pulse grid-cols-1 gap-8 lg:grid-cols-3" aria-hidden="true">
      <div className="h-96 rounded-2xl border border-outline-variant bg-surface-container-low lg:col-span-2" />
      <div className="h-64 rounded-2xl border border-outline-variant bg-surface-container-low" />
    </div>
  );
}

const COMING_SOON_MESSAGE = "Tính năng đang được phát triển.";

export function CommerceShopSettingsPage() {
  const [toastMessage, setToastMessage] = useState("");

  const {
    shop,
    form,
    fieldErrors,
    apiError,
    isLoading,
    isError,
    loadErrorMessage,
    isSubmitting,
    isDirty,
    updateField,
    resetForm,
    save,
    retry,
  } = useShopSettings();

  const storefrontPath = shop?.shopId
    ? APP_ROUTES.commerceShopProducts.replace(":shopId", shop.shopId)
    : null;

  const handleSave = useCallback(async () => {
    const ok = await save();
    if (ok) {
      setToastMessage("Lưu thay đổi thành công");
    }
  }, [save]);

  const dismissToast = useCallback(() => {
    setToastMessage("");
  }, []);

  const showComingSoon = useCallback(() => {
    setToastMessage(COMING_SOON_MESSAGE);
  }, []);

  return (
    <CommerceShell onComingSoon={showComingSoon}>
      <div className="mx-auto w-full max-w-[1280px] pb-28">
        {/* Header & Breadcrumb */}
        <header className="mb-6 border-b border-outline-variant/60 pb-4">
          <nav
            className="mb-2 flex flex-wrap items-center text-xs font-semibold text-on-surface-variant"
            aria-label="Breadcrumb"
          >
            <Link to={APP_ROUTES.commerceHome} className="transition-colors hover:text-primary">
              Trang quản trị
            </Link>
            <span className="material-symbols-outlined mx-1 text-sm" aria-hidden="true">
              chevron_right
            </span>
            <span className="font-bold text-on-surface">Cài đặt cửa hàng</span>
          </nav>

          <div className="flex flex-wrap items-start justify-between gap-4">
            <div>
              <h1 className="text-xl font-black text-on-surface sm:text-2xl">
                CÀI ĐẶT CỬA HÀNG (SELLER)
              </h1>
              <p className="mt-1 text-xs font-semibold text-on-surface-variant">
                Cập nhật hồ sơ thông tin shop, ảnh đại diện, ảnh bìa và chế độ nghỉ lễ
              </p>
            </div>

            {storefrontPath ? (
              <Link
                to={storefrontPath}
                className="inline-flex items-center gap-1.5 rounded-xl border border-primary px-4 py-2.5 text-xs font-bold text-primary shadow-xs transition-all hover:bg-primary hover:text-on-primary active:scale-95 cursor-pointer"
              >
                Xem shop trực tiếp
                <span className="material-symbols-outlined text-base" aria-hidden="true">
                  open_in_new
                </span>
              </Link>
            ) : null}
          </div>
        </header>

        {isLoading ? <ShopSettingsSkeleton /> : null}

        {isError ? (
          <div className="rounded-2xl border border-error/30 bg-error-container/40 p-8 text-center shadow-xs">
            <p className="text-sm font-semibold text-on-error-container">{loadErrorMessage}</p>
            <button
              type="button"
              onClick={retry}
              className="mt-4 rounded-xl bg-primary px-6 py-2.5 text-xs font-bold text-on-primary hover:bg-[#0050cb]"
            >
              Thử lại ngay
            </button>
          </div>
        ) : null}

        {!isLoading && !isError && shop ? (
          <>
            {apiError ? (
              <div
                className="mb-6 rounded-2xl border border-error/30 bg-error-container/40 p-4 shadow-xs"
                role="alert"
              >
                <p className="text-xs font-bold text-on-error-container">{apiError}</p>
              </div>
            ) : null}

            <div className="grid grid-cols-1 items-start gap-8 lg:grid-cols-3 mb-8">
              <div className="lg:col-span-2">
                <ShopSettingsProfileSection
                  form={form}
                  fieldErrors={fieldErrors}
                  shop={shop}
                  disabled={isSubmitting}
                  onFieldChange={updateField}
                />
              </div>
              <div>
                <ShopSettingsVacationSection
                  form={form}
                  fieldErrors={fieldErrors}
                  disabled={isSubmitting}
                  onFieldChange={updateField}
                />
              </div>
            </div>

            {/* E-Commerce Trust Badges Footer */}
            <CommerceFooterTrustSection />

            <ShopSettingsActionBar
              isDirty={isDirty}
              isSubmitting={isSubmitting}
              onCancel={resetForm}
              onSave={handleSave}
            />
          </>
        ) : null}
      </div>

      <FeedToast message={toastMessage} onDismiss={dismissToast} />
    </CommerceShell>
  );
}
