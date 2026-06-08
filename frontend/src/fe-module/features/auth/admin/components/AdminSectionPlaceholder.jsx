import { TabPanelHeader } from "../../../../shared/ui/auth/authUi.jsx";

export function AdminSectionPlaceholder({ title, subtitle, description }) {
  return (
    <div className="space-y-6">
      <TabPanelHeader title={title} subtitle={subtitle} />
      <div className="rounded-lg border border-dashed border-account-surface-dim bg-account-surface-low px-6 py-10 text-center">
        <p className="text-sm text-on-surface-variant">{description}</p>
      </div>
    </div>
  );
}