## ADDED Requirements

### Requirement: Commerce Home offline build jobs
The offline recsys ops package (`recsys-offline`) SHALL provide job or CLI entrypoints for Commerce Home without exposing an online Commerce Home predict API for end-user requests. Required entrypoints:

1. **Home sim (file/RAM)** — personas + in-memory niche catalog → Home impression/engage/CF/AR/export **files** (no shared Auth/Social/Commerce DB seed); impressions include provenance columns.
2. **Entity co-occurrence** — per train-data mode; write files and/or `entity_cooccur`.
3. **Social interest export** — per mode; write files and/or `user_social_interest_export`.
4. **Association-rule mine** — Apriori or FP-Growth; tag→category; write files and/or `social_tag_category_ar`.
5. **load-artifact** — upsert CF/AR/export files into Commerce tables without fake users/posts/orders/impressions.
6. **Home LTR** — build-dataset (provenance + labels), split, train, evaluate (AUC + P@10 by `request_id`), export-activate.
7. **Train/retrain orchestrator** — ordered: resolve mode → (sim) → CF → social-export → AR → load-artifact → dataset → split → train → evaluate → export-activate.

Mode resolution SHALL read Admin `commerce.home.ltr.train_data_mode` at orchestration start.

#### Scenario: Retrain orchestration order is enforced
- **WHEN** an operator triggers Home retrain
- **THEN** CF, social export, and AR run before build-dataset
- **AND** evaluate runs before export-activate

#### Scenario: Entity CF job is triggerable
- **WHEN** an operator triggers the entity co-occurrence build job under a resolved mode
- **THEN** the job writes entity–entity scores for at least category and brand pairs using log1p(weight_sum)
- **AND** retains at most top-M neighbors per entity
- **AND** reports completion status and the resolved mode

#### Scenario: Social interest export job is triggerable
- **WHEN** an operator triggers the social interest export job
- **THEN** the job produces decayed weighted scores to files and/or `user_social_interest_export`
- **AND** does not require Social HTTP on the Commerce Home online path to rebuild those scores

#### Scenario: AR mapping job is triggerable
- **WHEN** an operator triggers the association-rule bridge job
- **THEN** the job publishes interest_tag→category mappings with support/confidence fields using Apriori or FP-Growth
- **AND** drops rules below configured min_support / min_confidence

#### Scenario: load-artifact is triggerable
- **WHEN** an operator triggers Home load-artifact with CF/AR/export files
- **THEN** Commerce consumer tables are upserted
- **AND** Auth/Social event tables are not written by that job

#### Scenario: Home LTR pipeline is triggerable offline
- **WHEN** an operator runs Home build-dataset → train → evaluate → export-activate
- **THEN** evaluate compares LightGBM to baseline 0.7*popularity_score+0.3*recency_score on AUC and Precision@10 grouped by request_id
- **AND** export-activate activates only when both metrics pass; otherwise soft-rejects
- **AND** train_meta records the resolved train_data_mode

#### Scenario: Home LTR jobs remain offline-only
- **WHEN** a client inspects offline routes/CLIs for Commerce Home LTR
- **THEN** there is no public online endpoint that returns ranked Home products for a buyer session as a substitute for commerce-service serving

### Requirement: Job ordering documentation
Ops docs or job README SHALL document the D15 retrain order and D19 inventory (read-only extracts vs Commerce writes vs forbidden shared-DB seeds).

#### Scenario: Operator can find build order
- **WHEN** an operator reads the Home offline ops documentation
- **THEN** the recommended job order, mode enum, and table/collection inventory are listed without requiring inventing schemas at implement time
