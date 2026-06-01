import { VACATION_MESSAGE_MAX } from "../constants/shopSettingsConstants";

const textareaClass =
  "w-full rounded-lg border border-outline bg-surface-container-lowest px-4 py-2 text-body-md text-on-surface focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary resize-none";

const errorClass = "mt-1 text-sm text-error";

export function ShopSettingsVacationSection({
  form,
  fieldErrors,
  disabled,
  onFieldChange,
}) {
  return (
    <section className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm md:p-8">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h2 className="flex items-center gap-2 text-headline-sm font-semibold text-on-surface">
            <span className="material-symbols-outlined text-primary" aria-hidden="true">
              beach_access
            </span>
            Chế độ nghỉ lễ
          </h2>
          <p className="mt-1 text-body-sm text-on-surface-variant">
            Tạm dừng nhận đơn hàng mới trong thời gian bạn đi vắng.
          </p>
        </div>

        <label className="relative mt-1 inline-flex shrink-0 cursor-pointer items-center">
          <input
            type="checkbox"
            className="peer sr-only"
            checked={form.isVacation}
            disabled={disabled}
            onChange={(event) => onFieldChange("isVacation", event.target.checked)}
          />
          <span
            className={[
              "h-6 w-11 rounded-full transition-colors after:absolute after:left-[2px] after:top-[2px] after:h-5 after:w-5 after:rounded-full after:bg-white after:shadow-sm after:transition-transform peer-checked:bg-primary peer-checked:after:translate-x-5",
              disabled ? "cursor-not-allowed opacity-50" : "bg-outline-variant",
            ].join(" ")}
            aria-hidden="true"
          />
          <span className="sr-only">Bật chế độ nghỉ lễ</span>
        </label>
      </div>

      {form.isVacation ? (
        <div className="mt-6 border-t border-outline-variant pt-6">
          <label
            htmlFor="settings-vacation-message"
            className="mb-1 block text-label-md font-medium text-on-surface"
          >
            Thông báo nghỉ lễ (Tùy chọn)
          </label>
          <textarea
            id="settings-vacation-message"
            rows={3}
            className={textareaClass}
            placeholder="Ví dụ: Cửa hàng tạm nghỉ từ ngày... đến ngày..."
            value={form.vacationMessage}
            disabled={disabled}
            maxLength={VACATION_MESSAGE_MAX}
            onChange={(event) =>
              onFieldChange("vacationMessage", event.target.value.slice(0, VACATION_MESSAGE_MAX))
            }
          />
          <div className="mt-1 flex justify-between text-body-sm text-on-surface-variant">
            {fieldErrors.vacationMessage ? (
              <p className={errorClass}>{fieldErrors.vacationMessage}</p>
            ) : (
              <span />
            )}
            <span>
              {form.vacationMessage.length} / {VACATION_MESSAGE_MAX}
            </span>
          </div>
        </div>
      ) : null}
    </section>
  );
}
