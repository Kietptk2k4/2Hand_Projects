## ADDED Requirements

### Requirement: Commerce Home offline build jobs
The offline recsys ops package SHALL provide job or CLI entrypoints to (1) build entity co-occurrence tables for Commerce Home CF, (2) mine and publish Social interest_tag→Commerce category association mappings, and (3) build/train/evaluate/export-activate the Commerce Home LTR ranker, without exposing an online Commerce Home predict API for end-user requests.

#### Scenario: Entity CF job is triggerable
- **WHEN** an operator triggers the entity co-occurrence build job with valid Commerce DB configuration
- **THEN** the job writes or updates entity–entity scores for at least category and brand pairs
- **AND** reports completion status

#### Scenario: AR mapping job is triggerable
- **WHEN** an operator triggers the association-rule bridge job with access to Social interest inputs and Commerce categories
- **THEN** the job publishes interest_tag→category mappings with support/confidence fields

#### Scenario: Home LTR jobs remain offline-only
- **WHEN** a client inspects offline routes/CLIs for Commerce Home LTR
- **THEN** there is no public online endpoint that returns ranked Home products for a buyer session as a substitute for commerce-service serving
