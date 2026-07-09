import { useState } from "react";
import { useAdminFinanceOverview } from "../../hooks/useAdminFinanceOverview";
import { AdminFinanceOverviewView } from "../AdminFinanceOverviewView.jsx";
import { CommerceFinanceRetryPanel } from "../ui/CommerceFinanceRetryPanel.jsx";

export function AdminFinanceOverviewTab() {
  const [granularity, setGranularity] = useState("DAY");
  const { summary, trend, isLoading, errorMessage, retry } = useAdminFinanceOverview({ granularity });

  if (errorMessage) {
    return <CommerceFinanceRetryPanel message={errorMessage} onRetry={retry} />;
  }

  return (
    <AdminFinanceOverviewView
      summary={summary}
      trend={trend}
      isLoading={isLoading}
      granularity={granularity}
      onGranularityChange={setGranularity}
      onRetry={retry}
    />
  );
}
