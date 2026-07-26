## ADDED Requirements

### Requirement: Purchase profile respects train cutoff for final build
For the final dataset used to train after simulation, build-dataset SHALL consume a `user_purchase_profile` that excludes purchases after the time-split train cutoff `T_cut`, so test-window (and post-cut) purchases do not inflate `cross_domain_product_score`.

#### Scenario: Final build uses cutoff profile
- **WHEN** operators follow the documented final workflow after split
- **THEN** build-dataset is re-run using `user_purchase_profile.csv` exported with `as_of = T_cut`
- **AND** the resulting dataset is the input to the subsequent re-split and train jobs

#### Scenario: Cross-domain can be non-zero with tagged posts
- **WHEN** a user profile contains category C from a pre-cutoff purchase and an impressed post tags category C
- **THEN** `cross_domain_product_score` for that sample is at least 0.6
