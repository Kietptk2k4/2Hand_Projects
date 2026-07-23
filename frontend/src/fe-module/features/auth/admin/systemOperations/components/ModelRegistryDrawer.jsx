import { MODEL_REGISTRY_VIEW_MODES } from "../constants/modelRegistryConstants.js";
import { ModelRegistryDrawerView } from "./ModelRegistryDrawerView.jsx";

export function ModelRegistryDrawer({ artifact, viewMode, onClose, onViewChange }) {
  return (
    <ModelRegistryDrawerView
      open={Boolean(artifact)}
      artifact={artifact}
      viewMode={viewMode || MODEL_REGISTRY_VIEW_MODES.DETAIL}
      onClose={onClose}
      onViewChange={onViewChange}
    />
  );
}
