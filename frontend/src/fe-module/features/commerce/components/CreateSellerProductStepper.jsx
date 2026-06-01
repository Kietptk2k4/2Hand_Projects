export function CreateSellerProductStepper({ currentStep }) {
  const steps = [
    { id: 1, label: "Thông tin sản phẩm" },
    { id: 2, label: "Giá & tồn kho" },
  ];

  return (
    <ol className="mb-8 flex items-center justify-center gap-4">
      {steps.map((step, index) => {
        const active = currentStep === step.id;
        const done = currentStep > step.id;
        return (
          <li key={step.id} className="flex items-center gap-2">
            <span
              className={[
                "flex h-8 w-8 items-center justify-center rounded-full text-label-sm font-semibold",
                active || done
                  ? "bg-primary text-on-primary"
                  : "bg-surface-container-high text-on-surface-variant",
              ].join(" ")}
            >
              {step.id}
            </span>
            <span
              className={[
                "hidden text-label-md sm:inline",
                active ? "font-semibold text-primary" : "text-on-surface-variant",
              ].join(" ")}
            >
              {step.label}
            </span>
            {index < steps.length - 1 ? (
              <span className="mx-2 hidden h-px w-8 bg-outline-variant sm:block" aria-hidden="true" />
            ) : null}
          </li>
        );
      })}
    </ol>
  );
}
