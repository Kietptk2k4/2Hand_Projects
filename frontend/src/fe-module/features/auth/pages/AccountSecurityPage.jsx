import { useCallback, useState } from "react";
import { useAccountProfile } from "../account/hooks/useAccountProfile.jsx";
import { AccountSecurityLayout } from "../security/components/AccountSecurityLayout.jsx";
import { LoginHistoryTab } from "../security/components/LoginHistoryTab.jsx";
import { LoginSessionsTab } from "../security/components/LoginSessionsTab.jsx";

export function AccountSecurityPage() {
  const [activeTab, setActiveTab] = useState("sessions");
  const { profile } = useAccountProfile();

  const onTabChange = useCallback((tabId) => {
    setActiveTab(tabId);
  }, []);

  return (
    <AccountSecurityLayout
      activeTab={activeTab}
      onTabChange={onTabChange}
      avatarUrl={profile?.profile?.avatar_url}
    >
      {activeTab === "sessions" ? <LoginSessionsTab /> : <LoginHistoryTab />}
    </AccountSecurityLayout>
  );
}
