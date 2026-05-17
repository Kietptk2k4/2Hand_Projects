import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import ModuleProviders from "./fe-module";
import "./index.css";

async function enableMocking() {
  if (import.meta.env.VITE_USE_MOCK !== "true") {
    return;
  }

  const { worker } = await import("./mocks/browser");
  await worker.start({
    onUnhandledRequest: "bypass",
  });
}

enableMocking().then(() => {
  createRoot(document.getElementById("root")).render(
    <StrictMode>
      <ModuleProviders />
    </StrictMode>
  );
});

