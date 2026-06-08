import { useEffect, useState } from "react";
import { AccountCard } from "../../../../../shared/ui/auth/authUi.jsx";
import { MOCK_SOCIAL_MODERATION_IDS } from "../constants/socialModerationConstants.js";
import { isValidObjectId } from "../utils/isValidObjectId.js";

const TARGET_CONFIG = {
  "post-moderation": {
    paramKey: "postId",
    label: "Bai viet can kiem duyet",
    placeholder: "Nhap MongoDB ObjectId (24 hex)...",
    hint: `Vi du mock: ${MOCK_SOCIAL_MODERATION_IDS.POST}`,
    idLabel: "postId",
  },
  "comment-moderation": {
    paramKey: "commentId",
    label: "Binh luan can kiem duyet",
    placeholder: "Nhap MongoDB ObjectId (24 hex)...",
    hint: `Vi du mock: ${MOCK_SOCIAL_MODERATION_IDS.COMMENT}`,
    idLabel: "commentId",
  },
};

export function ContentModerationTargetBar({ activeTab, targetIds, onTargetChange }) {
  const config = TARGET_CONFIG[activeTab];
  const [inputValue, setInputValue] = useState("");
  const [validationError, setValidationError] = useState("");

  useEffect(() => {
    if (!config) return;
    const current = targetIds[config.paramKey] || "";
    setInputValue(current);
    setValidationError("");
  }, [activeTab, config, targetIds]);

  if (!config) return null;

  const handleSubmit = (event) => {
    event.preventDefault();
    const trimmed = inputValue.trim();
    if (!trimmed) {
      onTargetChange({ [config.paramKey]: "" });
      setValidationError("");
      return;
    }
    if (!isValidObjectId(trimmed)) {
      setValidationError("ObjectId khong hop le (can 24 ky tu hex).");
      return;
    }
    setValidationError("");
    onTargetChange({ [config.paramKey]: trimmed });
  };

  const handleClear = () => {
    setInputValue("");
    setValidationError("");
    onTargetChange({ [config.paramKey]: "" });
  };

  const currentId = targetIds[config.paramKey];

  return (
    <AccountCard className="mb-6">
      <form onSubmit={handleSubmit}>
        <label htmlFor="content-moderation-target-input" className="mb-1.5 block text-xs font-semibold text-on-surface">
          {config.label}
        </label>
        <div className="flex flex-col gap-2 sm:flex-row">
          <input
            id="content-moderation-target-input"
            type="text"
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            placeholder={config.placeholder}
            className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2.5 font-mono text-sm outline-none focus:border-primary focus:ring-1 focus:ring-primary/30"
            autoComplete="off"
            spellCheck={false}
          />
          <div className="flex shrink-0 gap-2">
            <button type="submit" className="rounded-lg bg-primary px-4 py-2.5 text-sm font-semibold text-white hover:opacity-90">
              Chon
            </button>
            {currentId ? (
              <button
                type="button"
                onClick={handleClear}
                className="rounded-lg border border-outline-variant px-4 py-2.5 text-sm font-medium text-on-surface-variant hover:bg-surface-container-low"
              >
                Xoa
              </button>
            ) : null}
          </div>
        </div>
        {validationError ? (
          <p className="mt-2 text-xs text-error">{validationError}</p>
        ) : (
          <p className="mt-2 text-xs text-on-surface-variant">{config.hint}</p>
        )}
        {currentId ? (
          <p className="mt-2 break-all font-mono text-xs text-on-surface-variant">
            {config.idLabel}: {currentId}
          </p>
        ) : null}
      </form>
    </AccountCard>
  );
}