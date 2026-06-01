export function SellerProductWizardFooter({
  step,
  canEdit,
  isSubmitting,
  onBack,
  onSaveDraft,
  onNext,
  nextLabel = "Tiếp theo",
  showSaveDraft = true,
}) {
  return (
    <div className="sticky bottom-0 z-10 -mx-4 mt-6 border-t border-outline-variant bg-surface/95 px-4 py-4 backdrop-blur md:-mx-0 md:rounded-b-xl">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <button
          type="button"
          onClick={onBack}
          disabled={step <= 1 || isSubmitting}
          className="rounded-lg px-5 py-2.5 text-label-md text-on-surface-variant hover:bg-surface-container-low disabled:opacity-40"
        >
          Quay lại
        </button>

        <div className="flex flex-wrap gap-3">
          {showSaveDraft && canEdit ? (
            <button
              type="button"
              onClick={onSaveDraft}
              disabled={isSubmitting}
              className="rounded-lg border border-primary px-5 py-2.5 text-label-md font-medium text-primary hover:bg-surface-container-low disabled:opacity-50"
            >
              Lưu nháp
            </button>
          ) : null}
          <button
            type="button"
            onClick={onNext}
            disabled={isSubmitting}
            className="rounded-lg bg-primary px-6 py-2.5 text-label-md font-medium text-on-primary hover:bg-[#0050cb] disabled:opacity-50"
          >
            {isSubmitting ? "Đang lưu..." : nextLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
