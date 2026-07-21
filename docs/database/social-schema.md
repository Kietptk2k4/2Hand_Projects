# Social Service Database Schema

Phân hệ mạng xã hội của 2Hands. Kết hợp **MongoDB** cho nội dung bài viết và **PostgreSQL** cho các quan hệ tương tác.

## A. MongoDB Collections

### 1. POSTS
- `_id`: ObjectId
- `author_id`: UUID (String)
- `caption`: Text
- `media`: Array<{url: String, type: String}>
- `productTags`: Array<{product_id: UUID, price: Decimal}>
- `status`: Enum (DRAFT, ACTIVE, DELETED)
- `visibility`: Enum (PUBLIC, FOLLOWERS)
- `like_count`: Integer
- `reply_count`: Integer
- `hashtags`: Array<String>
- `allow_comments`: BOOLEAN
- `created_at`: Timestamp
- `updated_at`: Timestamp
- `deleted_at`: Timestamp

### 2. COMMENTS
- `_id`: ObjectId
- `post_id`: ObjectId
- `author_id`: UUID (String)
- `parent_comment_id`: ObjectId (Nullable - cho reply comment)
- `content_text`: Text
- `media`: Array<{url, type}>
- `status`: Enum (ACTIVE, DELETED)
- `like_count`: Integer
- `created_at`: Timestamp
- `updated_at`: Timestamp

## B. PostgreSQL Tables

### 3. POST_LIKES
- `post_id`: String (Mapping MongoDB _id)
- `user_id`: UUID
- `created_at`: Timestamp
- PRIMARY KEY (post_id, user_id)

### 4. POST_SAVES
- `post_id`: String
- `user_id`: UUID
- `created_at`: Timestamp
- PRIMARY KEY (post_id, user_id)

### 5. FOLLOWS
- `follower_id`: UUID (Người theo dõi)
- `followee_id`: UUID (Người được theo dõi)
- `status`: Enum (PENDING, ACCEPTED) - Hỗ trợ tài khoản Private
- `created_at`: Timestamp
- PRIMARY KEY (follower_id, followee_id)

### 6. SEARCH_HISTORY
- `id`: UUID (PK)
- `user_id`: UUID
- `keyword`: String
- `created_at`: Timestamp
- `updated_at`: Timestamp

### 7. OUTBOX_EVENTS
- `id`: UUID (PK)
- `event_type`: String
- `aggregate_id`: String
- `payload`: JSONB
- `status`: Enum (PENDING, PROCESSING, PUBLISHED, FAILED)
- `retry_count`: Integer
- `created_at`: Timestamp
- `published_at`: Timestamp

### 8. MODEL_ARTIFACTS
Registry for offline-trained ranking models (LightGBM exported as ONNX, etc.).
- `id`: UUID (PK)
- `model_name`: String (e.g. `feed_ranker`)
- `version`: Integer (unique per `model_name`)
- `format`: String (e.g. `ONNX`)
- `artifact_path`: Text (filesystem path Java `ModelLoader` can read)
- `metrics`: JSONB (AUC, Precision@K, …)
- `is_active`: Boolean — at most one active row per `model_name` (partial unique index)
- `trained_at`: Timestamp

### 9. POST_IMPRESSION_LOG
Logged when Recommend Feed returns Top-K posts (source for training labels).
- `id`: UUID (PK)
- `user_id`: UUID
- `post_id`: String (Mongo post id)
- `shown_at`: Timestamp
- `rank_position`: Integer (nullable early)
- `model_version`: Integer **NULL** when ranking used rule-based fallback (no active ONNX)
- `model_name`: String (optional, e.g. `feed_ranker`)
- `request_id`: String (optional, groups one recommend page)

### 10. USER_SEEN_POSTS
Dedup for Candidate Pool — posts already shown to the user.
- `user_id`: UUID
- `post_id`: String
- `seen_at`: Timestamp
- PRIMARY KEY (`user_id`, `post_id`)