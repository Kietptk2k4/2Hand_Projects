# recsys-cross-domain-affinity

## Purpose

Persist product-tag commerce identifiers on Social posts and compute `cross_domain_product_score` (0.6 category + 0.4 shop) for train/serve feature parity.

## Requirements

### Requirement: Product tag snapshot includes commerce identifiers
When a post product tag is created or updated, the Social Mongo `productTags` snapshot SHALL store `productId`, optional `categoryId`, optional `shopId`, optional `category` display name, and existing display fields (`name`, `imageUrl`, `price`, `available`) without requiring migration of historical documents.

#### Scenario: New tag persists categoryId and shopId
- **WHEN** a user creates or edits a post with a product tag resolved from Commerce
- **THEN** the stored `productTags` entry includes `categoryId` and `shopId` when Commerce provides them

#### Scenario: Legacy documents remain readable
- **WHEN** a post document lacks `categoryId` or `shopId` on product tags
- **THEN** Social read/recommend paths MUST still load the post without error

### Requirement: Real cross-domain product score
`PostFeatureBuilder` and the offline build-dataset feature step SHALL compute `cross_domain_product_score` as `0.6 * category_overlap + 0.4 * shop_overlap` where overlap is 1 if the user's purchase/cart category or shop set intersects the post's productTags identifiers, else 0; missing tags or missing user profile yields 0.0.

#### Scenario: Category overlap scores
- **WHEN** the user previously purchased a product in category C and the candidate post tags a product with `categoryId = C`
- **THEN** `cross_domain_product_score` is at least 0.6

#### Scenario: No commerce profile
- **WHEN** no purchase/cart profile is available for the user
- **THEN** `cross_domain_product_score` is 0.0

#### Scenario: Train serve same formula
- **WHEN** the same user history and post tags are evaluated in Java serving and Python build-dataset at the same point-in-time cutoff
- **THEN** both produce the same `cross_domain_product_score` value
