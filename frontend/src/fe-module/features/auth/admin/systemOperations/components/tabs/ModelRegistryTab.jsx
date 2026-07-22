import { useState } from "react";
import { useRecommendationModelArtifacts } from "../../hooks/useRecommendationModelArtifacts.js";
import { ModelRegistryTabView } from "./ModelRegistryTabView.jsx";

export function ModelRegistryTab() {
  const { status, items, errorMessage, refetch } = useRecommendationModelArtifacts({ enabled: true });
  const [expandedVersion, setExpandedVersion] = useState(null);

  return (
    <ModelRegistryTabView
      title="Recommendation model registry"
      subtitle="Danh sách feed_ranker versions (active / rejected / inactive). Không kích hoạt export từ UI."
      status={status}
      errorMessage={errorMessage}
      items={items}
      expandedVersion={expandedVersion}
      onToggleExpand={(version) =>
        setExpandedVersion((prev) => (prev === version ? null : version))
      }
      onRetry={refetch}
      forbiddenMessage="Bạn cần role ADMIN hoặc MODERATOR để xem model registry."
    />
  );
}
