import { useCallback, useState } from "react";
import { AccountSettingsLayout } from "../account/components/AccountSettingsLayout.jsx";
import { AccountInfoTab } from "../account/components/AccountInfoTab.jsx";
import { DeleteAccountTab } from "../account/components/DeleteAccountTab.jsx";
import { EditProfileTab } from "../account/components/EditProfileTab.jsx";
import { PrivacyTab } from "../account/components/PrivacyTab.jsx";
import { SettingsTab } from "../account/components/SettingsTab.jsx";
import { UpdateAvatarTab } from "../account/components/UpdateAvatarTab.jsx";
import { useAccountProfile } from "../account/hooks/useAccountProfile.jsx";
import { ErrorState } from "../../../shared/ui/PageState.jsx";
import { AccountSkeleton, AuthAlert } from "../../../shared/ui/auth/authUi.jsx";

const TAB_COMPONENTS = {
  info: AccountInfoTab,
  edit: EditProfileTab,
  avatar: UpdateAvatarTab,
  privacy: PrivacyTab,
  settings: SettingsTab,
  delete: DeleteAccountTab,
};

export function AccountPage() {
  const [activeTab, setActiveTab] = useState("info");
  const [alert, setAlert] = useState(null);
  const { profile, status, errorMessage, isLoading, refetch } = useAccountProfile();

  const onNotify = useCallback((nextAlert) => {
    setAlert(nextAlert);
  }, []);

  const onTabChange = useCallback((tabId) => {
    setActiveTab(tabId);
    setAlert(null);
  }, []);

  if (isLoading) {
    return (
      <AccountSettingsLayout activeTab={activeTab} onTabChange={onTabChange} avatarUrl={null}>
        <AccountSkeleton />
      </AccountSettingsLayout>
    );
  }

  if (status === "not_found" || status === "error") {
    return (
      <AccountSettingsLayout activeTab={activeTab} onTabChange={onTabChange} avatarUrl={null}>
        <ErrorState
          message={
            status === "not_found"
              ? errorMessage || "Không tìm thay tài khoản."
              : errorMessage || "Không tải duoc thông tin tài khoản."
          }
        />
        <button
          type="button"
          onClick={refetch}
          className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white"
        >
          Thử lại
        </button>
      </AccountSettingsLayout>
    );
  }

  const TabComponent = TAB_COMPONENTS[activeTab] || AccountInfoTab;
  const tabProps =
    activeTab === "delete"
      ? { onNotify }
      : { profile, refetch, onNotify, onTabChange };

  return (
    <AccountSettingsLayout
      activeTab={activeTab}
      onTabChange={onTabChange}
      avatarUrl={profile?.profile?.avatar_url}
    >
      {alert ? (
        <div className="mb-6">
          <AuthAlert
            variant={alert.variant}
            message={alert.message}
            title={alert.variant === "success" ? "Thành công" : alert.variant === "error" ? "Lỗi" : undefined}
            onDismiss={() => setAlert(null)}
          />
        </div>
      ) : null}

      <TabComponent {...tabProps} />
    </AccountSettingsLayout>
  );
}
