import { useAccountProfile } from "../account/hooks/useAccountProfile.jsx";
import { AccountPasswordLayout } from "../password/components/AccountPasswordLayout.jsx";
import { ChangePasswordTab } from "../password/components/ChangePasswordTab.jsx";

export function AccountPasswordPage() {
  const { profile } = useAccountProfile();

  return (
    <AccountPasswordLayout avatarUrl={profile?.profile?.avatar_url}>
      <ChangePasswordTab />
    </AccountPasswordLayout>
  );
}
