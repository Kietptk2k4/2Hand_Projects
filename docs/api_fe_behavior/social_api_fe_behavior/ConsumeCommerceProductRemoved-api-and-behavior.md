# Consume Commerce Product Removed - Behavior Spec

## Scope

Social consumer cap nhat product_tags[].available = false khi nhan COMMERCE_PRODUCT_REMOVED tu topic commerce.product.removed.

## Rules

- Idempotent qua processed_domain_events
- Giu snapshot name, image_url, category, price
- FE: modal "San pham khong con kha dung", khong goi Commerce tu browser