export function ShopSettingsActionBar({ isDirty, isSubmitting, onCancel, onSave }) {
  return (
    <div className="fixed bottom-0 left-0 right-0 z-30 border-t border-outline-variant bg-surface shadow-[0_-4px_6px_-1px_rgba(0,0,0,0.05)]">
      <div className="mx-auto flex max-w-[1280px] items-center justify-end gap-4 px-4 py-4 md:px-8">
        <button
          type="button"
          onClick={onCancel}
          disabled={isSubmitting}
          className="rounded-lg px-4 py-2.5 text-label-md text-on-surface-variant transition-colors hover:text-primary disabled:opacity-50"
        >
          Hủy
        </button>
        <button
          type="button"
          onClick={onSave}
          disabled={!isDirty || isSubmitting}
          className="min-w-[140px] rounded-lg bg-primary px-6 py-2.5 text-label-md font-medium text-on-primary shadow-sm hover:bg-[#0050cb] disabled:opacity-50"
        >
          {isSubmitting ? "Đang lưu..." : "Lưu thay đổi"}
        </button>
      </div>
    </div>
  );
}
