# Checklist - Social product tag snapshot

## Backend (social-service)

- [ ] Mongo product_tags: name, image_url, category, available
- [ ] Create/Edit post: resolve snapshot tu Commerce API (server-side)
- [ ] Feed + post detail response: productId, price, name, imageUrl, category, available
- [ ] Kafka commerce.product.removed -> available: false tren moi post lien quan
- [ ] COMMERCE_SERVICE_BASE_URL cau hinh dung
- [ ] File Java UTF-8 (khong co \x00 trong 20 byte dau)
- [ ] Unit tests snapshot resolver + product removed consumer pass

## Frontend

- [ ] Khong goi Commerce API enrich tag tren /social
- [ ] mapProductTagsFromApi / useEnrichedProductTags dung data tu Social API
- [ ] Click Xem + available: false -> modal "San pham khong con kha dung"
- [ ] Click Xem + available -> navigate commerce product detail
- [ ] Console /social khong co 404 commerce khi load feed

## Manual

1. Post + san pham ACTIVE -> feed co ten/anh snapshot
2. Remove san pham (Commerce) -> feed tag available: false
3. Xem tag unavailable -> modal, khong 404 commerce page
4. Post cu thieu snapshot -> van hien thi, available mac dinh true