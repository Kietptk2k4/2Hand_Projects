export function ShopSettingsActionBar({ isDirty, isSubmitting, onCancel, onSave }) {
  return (
    <div className="fixed bottom-0 left-0 right-0 z-30 border-t border-outline-variant/80 bg-surface/90 backdrop-blur-md shadow-lg">
      <div className="mx-auto flex max-w-[1280px] items-center justify-end gap-3 px-4 py-3.5 md:px-8">
        <button
          type="button"
          onClick={onCancel}
          disabled={isSubmitting}
          className="rounded-xl px-5 py-2.5 text-xs font-bold text-on-surface-variant transition-all hover:bg-surface-container-high hover:text-on-surface disabled:opacity-50 cursor-pointer"
        >
          Hủy
        </button>
        <button
          type="button"
          onClick={onSave}
          disabled={!isDirty || isSubmitting}
          className="min-w-[140px] rounded-xl bg-primary px-6 py-2.5 text-xs font-bold text-on-primary shadow-xs transition-all hover:bg-[#0050cb] active:scale-95 disabled:opacity-50 cursor-pointer"
        >
          {isSubmitting ? "Đang lưu..." : "Lưu thay đổi"}
        </button>
      </div>
    </div>
  );
}
