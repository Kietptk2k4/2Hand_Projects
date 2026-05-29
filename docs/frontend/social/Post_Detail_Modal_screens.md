# Post Detail Modal — Frontend Spec

Version: 1.0  
Scope: Modal chi tiết bài viết trên `/social` (overlay, không full-page route).

---

## Route & state

- Feed: `/social`
- Deep link modal: `/social?postId={id}` (optional `&focusComments=1`)
- Đóng modal: xóa query → giữ scroll feed

---

## API

| Action | Endpoint |
|--------|----------|
| Chi tiết post | `GET /api/v1/social/posts/{postId}` |
| Top-level comments | `GET /api/v1/social/posts/{postId}/comments` |
| Replies | `GET .../comments?parent_comment_id={commentId}` |

API layer: `postApi.js` — unwrap envelope tại `socialApiResponse.js`.

---

## MSW (`VITE_USE_MOCK=true`)

Handlers: `socialPostHandlers.js`  
Data: `socialPostDetailData.js`, `socialCommentsData.js`

---

## QA postIds

| postId | Kỳ vọng |
|--------|---------|
| `674a10000000000000000001` | 200 — media, productTags, ~30 comments, `hasNext` page 0 |
| `674a10000000000000000006` | 200 — comments rỗng |
| `674a10000000000000000403` | 403 FOLLOWERS |
| `000000000000000000000000` | 404 |
| `invalid-id` | 400 |
| Comments `?size=99` | 400 `SOCIAL-400-PAGINATION` |
| Không Authorization | 401 |

---

## Entry points (feed card)

| Vùng | Hành vi |
|------|---------|
| Ảnh / caption | Mở modal |
| Comment / replyCount | Mở modal + focus ô comment |
| Xem thêm caption | Expand in-place, **không** modal |
| Like / Share / hashtag | Coming soon / không modal |

---

## Manual test

1. `npm run dev`, `VITE_USE_MOCK=true`
2. Login `active@2hands.vn` / `Password123!`
3. `/social` → tap bài đầu → modal + comments
4. 「Xem thêm」 trên card → không mở modal
5. Tap ảnh trong modal → gallery
6. 「Xem N phản hồi」→ load replies
7. `?postId=000000000000000000000000` → lỗi trong modal
8. Đóng modal → scroll feed giữ nguyên
