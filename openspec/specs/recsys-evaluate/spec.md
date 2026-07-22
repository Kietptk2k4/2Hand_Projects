# recsys-evaluate

## Purpose

Offline evaluation of LightGBM vs rule-based baseline on the held-out test split (ROC-AUC and ranking@10 metrics) with a JSON-only report.

## Requirements

### Requirement: Pointwise ROC-AUC on test set
The evaluate pipeline SHALL score each usable `dataset_test.parquet` row with the trained LightGBM model and with the rule-based baseline, and SHALL compute ROC-AUC independently for each scorer using labels and scores (no grouping required). If AUC is undefined (e.g. only one class), the report SHALL set AUC to null and include a warning.

#### Scenario: AUC computed for both models
- **WHEN** the test set contains both positive and negative labels
- **THEN** `evaluate_report.json` includes ROC-AUC for lightgbm and for baseline

### Requirement: Ranking metrics at K equals 10
The evaluate pipeline SHALL compute Precision@10, Recall@10, and HitRate@10 by grouping rows (prefer `request_id`, else `user_id` with a warning), ranking candidates by score descending within each group, and using top-`min(10, n)` where `n` is the group size. Precision denominator SHALL be `min(10, n)`.

#### Scenario: Precision with fewer than ten candidates
- **WHEN** a group has 7 candidates and 2 relevant items appear in the top 7
- **THEN** Precision@10 for that group is 2/7

#### Scenario: HitRate detects any relevant in topk
- **WHEN** a group has at least one relevant item in its top-`min(10,n)`
- **THEN** HitRate@10 for that group is 1

### Requirement: Baseline uses locked six-feature weights
The rule-based baseline used in evaluate (and Social `RuleBasedRankingModel` serving) SHALL score  
`0.12*recency + 0.28*engagement + 0.22*hashtag + 0.13*author_affinity + 0.13*mutual_follow + 0.12*cross_domain`.

#### Scenario: Cross domain participates in baseline
- **WHEN** only `cross_domain_product_score` is non-zero among features
- **THEN** the baseline score equals 0.12 times that feature value

### Requirement: Compare models and write JSON report
The evaluate job SHALL write `evaluate_report.json` under the artifact directory containing lightgbm metrics, baseline metrics, deltas, `k`, `group_by`, row/group counts, and warnings. The job MUST fail closed when `dataset_test.parquet` or the trained model file is missing.

#### Scenario: Successful evaluate report
- **WHEN** test parquet and model.txt exist with usable rows
- **THEN** evaluate completes and writes `evaluate_report.json`

#### Scenario: Missing inputs fail closed
- **WHEN** `dataset_test.parquet` or `model.txt` is missing
- **THEN** the job fails with an explicit error and does not claim success
