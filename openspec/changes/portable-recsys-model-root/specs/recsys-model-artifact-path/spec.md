## ADDED Requirements

### Requirement: Portable relative artifact reference
The system SHALL treat `model_artifacts.artifact_path` as a portable relative reference (Phase 1: a single path segment / basename such as `feed_ranker_v1.onnx`) that does not encode host-specific absolute filesystem locations.

#### Scenario: Basename stored after export
- **WHEN** export-activate successfully registers a new ONNX version
- **THEN** the inserted `artifact_path` equals the ONNX filename only (for example `feed_ranker_v3.onnx`)
- **AND** it MUST NOT contain drive letters, leading `/`, or Windows absolute prefixes

### Requirement: Resolve path via configurable model root
Social Service SHALL resolve the on-disk ONNX file by joining a configured model root directory with the relative `artifact_path` from the active registry row.

#### Scenario: Docker Linux load
- **WHEN** `SOCIAL_RECOMMENDATION_MODEL_ROOT` (or equivalent) is `/models/recsys` and the active relative path is `feed_ranker_v1.onnx` and that file exists under the root
- **THEN** Social loads ONNX from `/models/recsys/feed_ranker_v1.onnx`

#### Scenario: Windows host load without DB change
- **WHEN** the same active relative path is `feed_ranker_v1.onnx` and model root is set to the host artifacts directory
- **THEN** Social loads the file under that root without updating `model_artifacts`

### Requirement: Reject unsafe artifact path segments
When resolving an active artifact, Social SHALL reject relative values that attempt path traversal or absolute override (including `..`, nested separators, or absolute/URI forms) and MUST NOT load a file outside the configured model root.

#### Scenario: Traversal rejected
- **WHEN** `artifact_path` is `../other/model.onnx` or an absolute path string
- **THEN** Social does not load that path as a successful active artifact
- **AND** ranking falls back according to existing fallback rules with a non-success load outcome

### Requirement: Shared artifacts mount for container Social
Dev/container deployments of Social that consume offline-exported ONNX SHALL mount the offline artifacts directory at the configured model root (for example host `Services/recsys-offline/data/artifacts` → container `/models/recsys`) so the resolved relative path is readable inside the container.

#### Scenario: Mount makes file visible
- **WHEN** Social runs in Docker with model root `/models/recsys` and the artifacts directory is mounted there read-only
- **THEN** `feed_ranker_v1.onnx` written by offline export is visible at `/models/recsys/feed_ranker_v1.onnx` without copying into the Social source tree
