import { useState } from "react";
import { InvestigationProfileCardView } from "./InvestigationProfileCardView.jsx";

export function InvestigationProfileCard({ profile }) {
  const [copied, setCopied] = useState(false);

  if (!profile) return null;

  const onCopyId = async () => {
    if (!profile?.user_id) return;
    try {
      await navigator.clipboard.writeText(profile.user_id);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      setCopied(false);
    }
  };

  return (
    <InvestigationProfileCardView profile={profile} copied={copied} onCopyId={onCopyId} />
  );
}
