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
      {/* Left Stepper Sidebar */}
      <aside className="hidden w-64 shrink-0 lg:block">
        <nav className="sticky top-24 rounded-2xl border border-outline-variant/80 bg-surface-container-lowest p-5 shadow-xs">
          <h2 className="mb-4 text-xs font-black uppercase tracking-wider text-on-surface flex items-center gap-2">
            <span className="material-symbols-outlined text-primary text-lg">format_list_bulleted</span>
            TIẾN TRÌNH THIẾT LẬP
          </h2>
          <ol className="space-y-1.5">
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
                      "flex w-full items-center gap-3 rounded-xl px-3.5 py-3 text-left text-xs font-bold transition-all cursor-pointer shadow-xs",
                      active
                        ? "bg-primary text-on-primary shadow-xs ring-2 ring-primary/20 scale-[1.02]"
                        : "text-on-surface-variant hover:bg-surface-container-high hover:text-on-surface",
                      !unlocked ? "cursor-not-allowed opacity-40" : "",
                    ].join(" ")}
                  >
                    <span
                      className={[
                        "material-symbols-outlined text-lg",
                        active ? "text-on-primary" : done ? "text-primary" : "text-slate-400",
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

          <div className="mt-6 border-t border-outline-variant/60 pt-4">
            {lastSavedAt ? (
              <p className="text-[11px] font-semibold text-on-surface-variant">
                Bản nháp · Vừa lưu ({formatProductUpdatedAt(lastSavedAt.toISOString())})
              </p>
            ) : (
              <p className="text-[11px] font-semibold text-on-surface-variant">Lưu từng bước để giữ tiến trình.</p>
            )}
            {canEdit ? (
              <button
                type="button"
                disabled={isSubmitting}
                onClick={onSaveDraft}
                className="mt-3 w-full rounded-xl border border-outline-variant bg-surface-container-lowest px-3 py-2 text-xs font-bold text-on-surface transition-all hover:bg-surface-container-low cursor-pointer disabled:opacity-50"
              >
                Lưu nháp
              </button>
            ) : null}
          </div>
        </nav>
      </aside>

      {/* Main Right Content Form Area */}
      <div className="min-w-0 flex-1">
        <div className="mb-3">{breadcrumb}</div>

        <div className="mb-5 flex flex-wrap items-center justify-between gap-3 border-b border-outline-variant/60 pb-3.5">
          <h1 className="text-xl font-black text-on-surface sm:text-2xl">{title}</h1>
          {status ? (
            <span
              className={[
                "rounded-xl px-3 py-1 text-xs font-bold border shadow-xs",
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
