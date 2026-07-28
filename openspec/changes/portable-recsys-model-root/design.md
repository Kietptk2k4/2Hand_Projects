## Context

Recommended Feed activation already inserts ONNX into Social `model_artifacts` and Social `ModelLoader` prefers the active row over `social.recommendation.model-path`. In practice `export_activate` persists `Path.resolve()` absolute paths from the offline host. Dev Social runs in Docker (`docker-compose.dev.yml`) with only `Services/social-service:/app` mounted, so Windows absolute paths never resolve inside the container and ranking silently falls back to rule-based — while Admin can still list an “active” v1 row.

Operators agreed: DB stores portable relative names; filesystem root comes from config (`MODEL_ROOT` / Social equivalent); Compose mounts the offline artifacts directory at that root.

## Goals / Non-Goals

**Goals:**

- Persist only portable relative artifact references (Phase 1: basename `feed_ranker_v{N}.onnx`).
- Resolve load path as `MODEL_ROOT` + relative name in Social.
- Mount shared artifacts into `social-service` for Docker `dev` (and `apps` when that profile is used for Social).
- Keep column name `artifact_path` (semantics change only).
- Migrate existing absolute rows to basename.
- Fail closed with clear `file_not_found` / reject unsafe relative paths (`..`, absolute, drive letters).

**Non-Goals:**

- Renaming the DB column or Flyway schema rename.
- Object storage / MinIO for model binaries.
- Changing metrics JSON absolute paths for `train_meta` / `evaluate_report`.
- Subdirectory layout (`feed_ranker/v1/model.onnx`) in Phase 1.
- Changing metric gate / ONNX parity logic beyond the path string written to DB.
- Making Admin activate models from UI.

## Decisions

### D1 — Basename in DB, root in config
- **Choice:** Store `feed_ranker_v{version}.onnx` only; Social config `social.recommendation.model-root` ← `SOCIAL_RECOMMENDATION_MODEL_ROOT` (document alias `MODEL_ROOT` in ops docs).
- **Why:** Matches current filename convention; one env change per environment; no DB edits when moving hosts.
- **Alternatives:** Absolute path + path rewrite map (fragile); store only version and invent filename in Java (hides file naming from registry); subdir keys (deferred).

### D2 — Keep column name `artifact_path`
- **Choice:** No schema rename; document as portable relative reference.
- **Why:** Avoid migration churn; Admin/API already expose `artifactPath`.
- **Alternatives:** Rename to `artifact_name` (clearer, more work).

### D3 — Producer vs consumer roots
- **Choice:** Offline keeps `RECSYS_ARTIFACT_DIR` for **writing** files; Social uses `SOCIAL_RECOMMENDATION_MODEL_ROOT` for **reading**. Same host directory is mounted/shared in Docker.
- **Why:** Services already have separate configs; forcing one env name across Python/Java is unnecessary.
- **Alternatives:** Single shared env name in compose only (optional sugar later).

### D4 — Path safety
- **Choice:** Relative name MUST be a single path segment (basename): no `/`, `\`, `..`, or absolute/URI forms. Reject otherwise and treat as load failure (do not join blindly).
- **Why:** Prevents path traversal out of `MODEL_ROOT`.
- **Alternatives:** Allow nested relative paths with normalization (Phase 2 if needed).

### D5 — Legacy absolute rows
- **Choice:** One-time SQL/ops update to basename for existing rows. Optional short bridge in loader: if value is absolute **and** file exists, load it (dev-only convenience) — prefer migrate + strict relative after cutover.
- **Recommended implementation:** Migrate first; loader accepts relative only (simpler tests). If needed, bridge for one release behind a flag — default off.

### D6 — Fallback `model-path`
- **Choice:** Keep `social.recommendation.model-path` as fallback when no active row or relative resolve fails existence check (current behavior), but prefer documenting that production/dev Docker should rely on active row + `MODEL_ROOT`.
- **Why:** Minimal behavior break for local experiments without registry rows.

### D7 — Compose mount
- **Choice:** Bind-mount `../Services/recsys-offline/data/artifacts` → `/models/recsys:ro` on `social-service` in `docker-compose.dev.yml`; set `SOCIAL_RECOMMENDATION_MODEL_ROOT=/models/recsys` in `.env.docker` / `.env.docker.local` example. Mirror volume on `docker-compose.apps.yml` if Social image profile is used for the same workflow.
- **Why:** Matches operator-chosen container path; read-only reduces accidental mutation from Social.

### D8 — Resolved path visibility
- **Choice:** Log resolved absolute path at INFO on successful load; Admin list continues to show DB relative value; status endpoint does not require a new field for Phase 1.
- **Why:** Enough to debug without expanding API surface.

## Risks / Trade-offs

- **[Risk] Operators forget mount or wrong `MODEL_ROOT`** → Mitigation: README checklist; status remains `file_not_found`; compose ships default mount for `dev`.
- **[Risk] Offline and Social roots diverge (different folders)** → Mitigation: Document that Docker mount must point at the same directory offline writes; smoke test after export-activate.
- **[Risk] Absolute rows left in DB after deploy** → Mitigation: Migration task + startup warn if `artifact_path` looks absolute.
- **[Risk] Windows host bootRun without Docker** → Mitigation: Set `SOCIAL_RECOMMENDATION_MODEL_ROOT` to host artifacts dir (same relative DB value works).
- **[Trade-off] Basename-only limits multi-file layouts** → Accept for Phase 1; extend relative segments later if needed.

## Migration Plan

1. Ship Social loader that joins `MODEL_ROOT` + relative (and rejects unsafe strings).
2. Ship export-activate that inserts basename only.
3. Update Compose mount + env examples; recreate `social-service`.
4. Run: `UPDATE model_artifacts SET artifact_path = regexp_replace(artifact_path, '.*[\\\\/]', '') WHERE artifact_path ~ '[\\\\/]' OR artifact_path ~ '^[A-Za-z]:'` (or equivalent basename extraction).
5. Restart Social; confirm logs `loaded successfully version=N` and Admin status `lightgbm`.
6. Rollback: revert code; optionally restore absolute paths from backup (not preferred). Mount can remain harmlessly.

## Open Questions

- None blocking Phase 1 (basename + `SOCIAL_RECOMMENDATION_MODEL_ROOT` + compose mount locked).
- Optional later: whether status API should return `resolvedArtifactPath` for support tooling.
