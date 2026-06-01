import { STATUS_BADGE_CLASS, STATUS_LABELS, WIZARD_STEPS } from "../constants/sellerProductConstants";
import { formatProductUpdatedAt } from "../utils/sellerProductMapper";
import { SellerProductWizardStepper } from "./SellerProductWizardStepper";

export function SellerProductWizardLayout({
  title,
  breadcrumb,
  status,
  lastSavedAt,
  currentStep,
  maxUnlockedStep,
  canEdit,
  onStepClick,
  onSaveDraft,
  isSubmitting,
  children,
}) {
  return (
    <div className="mx-auto flex w-full max-w-6xl flex-col gap-6 px-4 py-6 lg:flex-row lg:py-8">
      <aside className="hidden w-64 shrink-0 lg:block">
        <nav className="sticky top-24 rounded-xl border border-outline-variant bg-surface-container-lowest p-4 shadow-sm">
          <p className="mb-4 text-label-md font-semibold text-on-surface">Tiến trình</p>
          <ol className="space-y-1">
            {WIZARD_STEPS.map((item) => {
              const unlocked = item.id <= maxUnlockedStep;
              const active = currentStep === item.id;
              const done = currentStep > item.id;

              return (
                <li key={item.id}>
                  <button
                    type="button"
                    disabled={!unlocked}
                    onClick={() => unlocked && onStepClick?.(item.id)}
                    className={[
                      "flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-left text-label-md transition-colors",
                      active
                        ? "bg-primary-container font-semibold text-on-primary-container"
                        : "text-on-surface-variant hover:bg-surface-container-low",
                      !unlocked ? "cursor-not-allowed opacity-40" : "",
                    ].join(" ")}
                  >
                    <span
                      className={[
                        "material-symbols-outlined text-[20px]",
                        active || done ? "text-primary" : "",
                      ].join(" ")}
                      aria-hidden="true"
                    >
                      {done ? "check_circle" : item.icon}
                    </span>
                    {item.label}
                  </button>
                </li>
              );
            })}
          </ol>

          <div className="mt-6 border-t border-outline-variant pt-4">
            {lastSavedAt ? (
              <p className="text-body-sm text-on-surface-variant">
                Bản nháp · vừa lưu ({formatProductUpdatedAt(lastSavedAt.toISOString())})
              </p>
            ) : (
              <p className="text-body-sm text-on-surface-variant">Lưu từng bước để giữ tiến trình.</p>
            )}
            {canEdit ? (
              <button
                type="button"
                disabled={isSubmitting}
                onClick={onSaveDraft}
                className="mt-3 w-full rounded-lg border border-outline-variant px-3 py-2 text-label-md font-medium text-on-surface hover:bg-surface-container-low disabled:opacity-50"
              >
                Lưu nháp
              </button>
            ) : null}
          </div>
        </nav>
      </aside>

      <div className="min-w-0 flex-1">
        <div className="mb-4">{breadcrumb}</div>

        <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
          <h1 className="text-headline-sm font-bold text-on-surface md:text-headline-md">{title}</h1>
          {status ? (
            <span
              className={[
                "rounded-full px-3 py-1 text-label-sm font-medium",
                STATUS_BADGE_CLASS[status] || STATUS_BADGE_CLASS.DRAFT,
              ].join(" ")}
            >
              {STATUS_LABELS[status] || status}
            </span>
          ) : null}
        </div>

        <SellerProductWizardStepper
          currentStep={currentStep}
          maxUnlockedStep={maxUnlockedStep}
          onStepClick={onStepClick}
        />

        {children}
      </div>
    </div>
  );
}
