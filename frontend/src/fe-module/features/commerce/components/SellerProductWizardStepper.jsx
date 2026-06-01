import { WIZARD_STEPS } from "../constants/sellerProductConstants";

export function SellerProductWizardStepper({ currentStep, maxUnlockedStep, onStepClick }) {
  return (
    <ol className="mb-6 flex flex-wrap items-center justify-center gap-2 md:hidden">
      {WIZARD_STEPS.map((item, index) => {
        const unlocked = item.id <= maxUnlockedStep;
        const active = currentStep === item.id;
        const done = currentStep > item.id;

        return (
          <li key={item.id} className="flex items-center">
            <button
              type="button"
              disabled={!unlocked}
              onClick={() => unlocked && onStepClick?.(item.id)}
              className="flex flex-col items-center gap-1 disabled:cursor-not-allowed"
            >
              <span
                className={[
                  "flex h-8 w-8 items-center justify-center rounded-full text-label-sm font-semibold",
                  active || done
                    ? "bg-primary text-on-primary"
                    : "bg-surface-container-high text-on-surface-variant",
                  !unlocked ? "opacity-40" : "",
                ].join(" ")}
              >
                {done ? (
                  <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
                    check
                  </span>
                ) : (
                  item.id
                )}
              </span>
              <span
                className={[
                  "max-w-[4.5rem] text-center text-[10px] leading-tight sm:max-w-none sm:text-label-sm",
                  active ? "font-semibold text-primary" : "text-on-surface-variant",
                ].join(" ")}
              >
                {item.label}
              </span>
            </button>
            {index < WIZARD_STEPS.length - 1 ? (
              <span
                className={[
                  "mx-1 h-px w-4 sm:w-8",
                  done ? "bg-primary" : "bg-outline-variant",
                ].join(" ")}
                aria-hidden="true"
              />
            ) : null}
          </li>
        );
      })}
    </ol>
  );
}
