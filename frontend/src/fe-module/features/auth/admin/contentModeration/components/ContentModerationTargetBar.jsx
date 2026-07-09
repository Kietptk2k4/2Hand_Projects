import { useEffect, useState } from "react";
import { MOCK_SOCIAL_MODERATION_IDS } from "../constants/socialModerationConstants.js";
import { isValidObjectId } from "../utils/isValidObjectId.js";
import { ContentModerationTargetBarView } from "./ContentModerationTargetBarView.jsx";

const TARGET_CONFIG = {
  "post-moderation": {
    paramKey: "postId",
    label: "Bài viết cần kiểm duyệt",
    placeholder: "Nhập MongoDB ObjectId (24 hex)…",
    hint: `Ví dụ mock: ${MOCK_SOCIAL_MODERATION_IDS.POST}`,
    idLabel: "postId",
  },
  "comment-moderation": {
    paramKey: "commentId",
    label: "Bình luận cần kiểm duyệt",
    placeholder: "Nhập MongoDB ObjectId (24 hex)…",
    hint: `Ví dụ mock: ${MOCK_SOCIAL_MODERATION_IDS.COMMENT}`,
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
      setValidationError("ObjectId không hợp lệ (cần 24 ký tự hex).");
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
    <ContentModerationTargetBarView
      label={config.label}
      placeholder={config.placeholder}
      hint={config.hint}
      idLabel={config.idLabel}
      inputValue={inputValue}
      validationError={validationError}
      currentId={currentId}
      onInputChange={(event) => setInputValue(event.target.value)}
      onSubmit={handleSubmit}
      onClear={handleClear}
    />
  );
}
