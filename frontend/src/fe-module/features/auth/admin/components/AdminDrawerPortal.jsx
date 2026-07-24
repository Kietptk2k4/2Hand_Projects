import { createPortal } from "react-dom";

export function AdminDrawerPortal({ children }) {
  if (typeof document === "undefined") return null;
  return createPortal(children, document.body);
}
