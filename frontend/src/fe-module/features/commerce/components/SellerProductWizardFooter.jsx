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
    <div className="fixed bottom-0 left-0 right-0 z-30 border-t border-outline-variant/80 bg-surface/90 backdrop-blur-md shadow-lg">
      <div className="mx-auto flex max-w-[1280px] items-center justify-between gap-3 px-4 py-3.5 md:px-8">
        <button
          type="button"
          onClick={onBack}
          disabled={step <= 1 || isSubmitting}
          className="rounded-xl px-5 py-2.5 text-xs font-bold text-on-surface-variant transition-all hover:bg-surface-container-high hover:text-on-surface disabled:opacity-40 cursor-pointer"
        >
          Quay lại
        </button>

        <div className="flex flex-wrap items-center gap-3">
          {showSaveDraft && canEdit ? (
            <button
              type="button"
              onClick={onSaveDraft}
              disabled={isSubmitting}
              className="rounded-xl border border-primary px-5 py-2.5 text-xs font-bold text-primary transition-all hover:bg-primary/5 active:scale-95 disabled:opacity-50 cursor-pointer"
            >
              Lưu nháp
            </button>
          ) : null}
          <button
            type="button"
            onClick={onNext}
            disabled={isSubmitting}
            className="min-w-[140px] rounded-xl bg-primary px-6 py-2.5 text-xs font-bold text-on-primary shadow-xs transition-all hover:bg-[#0050cb] active:scale-95 disabled:opacity-50 cursor-pointer"
          >
            {isSubmitting ? "Đang lưu..." : nextLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
