import { useMemo, useState } from "react";

const HASHTAG_REGEX = /#([\w\u00C0-\u024F\u1E00-\u1EFF]+)/g;

function renderCaptionWithHashtags(text, extraHashtags = []) {
  if (!text && extraHashtags.length === 0) return null;

  const tagSet = new Set(
    (extraHashtags || []).map((tag) => (tag.startsWith("#") ? tag.slice(1) : tag).toLowerCase())
  );

  let body = text || "";
  if (!body && tagSet.size > 0) {
    body = [...tagSet].map((t) => `#${t}`).join(" ");
  }

  const parts = [];
  let lastIndex = 0;
  let match;

  const regex = new RegExp(HASHTAG_REGEX.source, HASHTAG_REGEX.flags);
  while ((match = regex.exec(body)) !== null) {
    if (match.index > lastIndex) {
      parts.push({ type: "text", value: body.slice(lastIndex, match.index) });
    }
    parts.push({ type: "tag", value: match[1] });
    tagSet.delete(match[1].toLowerCase());
    lastIndex = regex.lastIndex;
  }

  if (lastIndex < body.length) {
    parts.push({ type: "text", value: body.slice(lastIndex) });
  }

  tagSet.forEach((tag) => {
    parts.push({ type: "tag", value: tag });
  });

  return parts.map((part, index) => {
    if (part.type === "text") {
      return <span key={`t-${index}`}>{part.value}</span>;
    }
    return (
      <a
        key={`h-${index}-${part.value}`}
        href="#"
        className="text-primary hover:underline"
        onClick={(event) => event.preventDefault()}
      >
        #{part.value}
      </a>
    );
  });
}

export function PostCaption({ caption, hashtags = [] }) {
  const [expanded, setExpanded] = useState(false);
  const isLong = useMemo(() => (caption || "").length > 180, [caption]);

  if (!caption && (!hashtags || hashtags.length === 0)) {
    return null;
  }

  return (
    <div className="text-base text-on-surface">
      <p className={!expanded && isLong ? "line-clamp-3" : ""}>
        {renderCaptionWithHashtags(caption, hashtags)}
      </p>
      {isLong ? (
        <button
          type="button"
          onClick={() => setExpanded((prev) => !prev)}
          className="mt-1 text-sm font-medium text-primary hover:underline"
        >
          {expanded ? "Thu gọn" : "Xem thêm"}
        </button>
      ) : null}
    </div>
  );
}
