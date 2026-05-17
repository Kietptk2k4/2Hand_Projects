import { EmptyState } from "../../../shared/ui/PageState";

export function AccountPage() {
  return (
    <section className="space-y-4">
      <header>
        <h1 className="text-2xl font-semibold text-on-surface">Tai khoan cua toi</h1>
        <p className="mt-1 text-sm text-on-surface-variant">
          Skeleton page cho ProfileAccount flow (6 tabs) theo spec.
        </p>
      </header>
      <EmptyState message="Module Account tabs se duoc implement tiep theo task screen-by-screen." />
    </section>
  );
}

