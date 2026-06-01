import { CREATE_SHOP_STEPS } from "../constants/createShopConstants";

export function CreateShopStepper({ currentStep }) {
  return (
    <div className="mb-10 flex items-center justify-center">
      {CREATE_SHOP_STEPS.map((item, index) => {
        const isActive = currentStep === item.id;
        const isCompleted = currentStep > item.id;
        const showConnector = index < CREATE_SHOP_STEPS.length - 1;

        return (
          <div key={item.id} className="flex items-center">
            <div className="flex flex-col items-center">
              <div
                className={[
                  "flex h-10 w-10 items-center justify-center rounded-full text-label-md font-medium shadow-sm",
                  isActive || isCompleted
                    ? "bg-primary text-on-primary"
                    : "bg-surface-container-high text-on-surface-variant",
                ].join(" ")}
              >
                {item.id}
              </div>
              <span
                className={[
                  "mt-1 text-label-sm font-semibold",
                  isActive ? "text-primary" : "text-on-surface-variant",
                ].join(" ")}
              >
                {item.label}
              </span>
            </div>

            {showConnector ? (
              <div
                className={[
                  "mx-3 mb-6 h-0.5 w-16 md:w-24",
                  isCompleted ? "bg-primary" : "bg-outline-variant",
                ].join(" ")}
                aria-hidden="true"
              />
            ) : null}
          </div>
        );
      })}
    </div>
  );
}
