import { AdminFilterButton } from "../../../components/ui";

export function SupportCrossLinkButton({ children, onClick, className = "" }) {
  return (
    <AdminFilterButton
      type="button"
      variant="secondary"
      onClick={onClick}
      className={["w-full sm:w-auto", className].filter(Boolean).join(" ")}
    >
      {children}
    </AdminFilterButton>
  );
}
