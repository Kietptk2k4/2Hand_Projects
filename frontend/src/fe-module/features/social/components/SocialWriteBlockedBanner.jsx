import { useState } from "react";
import { useSocialWriteBlock } from "../context/SocialWriteBlockContext";

export function SocialWriteBlockedBanner({ className = "" }) {
  const { isWriteBlocked, suspendMessage } = useSocialWriteBlock();
  const [dismissed, setDismissed] = useState(false);

  if (!isWriteBlocked || dismissed) {
    return null;
  }

  return (
    <div
      className={`flex items-start gap-3 rounded-lg border border-tertiary/40 bg-tertiary-container/50 px-4 py-3 text-sm text-on-surface ${className}`}
      role="status"
    >
      <span className="material-symbols-outlined shrink-0 text-[20px] text-tertiary" aria-hidden="true">
        info
      </span>
      <p className="flex-1">
        {suspendMessage || "Tài khoản bị đình chỉ. Bạn chỉ có thể xem nội dung."}
      </p>
      <button
        type="button"
        onClick={() => setDismissed(true)}
        className="shrink-0 rounded p-1 text-on-surface-variant hover:bg-surface-container-high"
        aria-label="Đóng thông báo"
      >
        <span className="material-symbols-outlined text-[18px]" aria-hidden="true">
          close
        </span>
      </button>
    </div>
  );
}
