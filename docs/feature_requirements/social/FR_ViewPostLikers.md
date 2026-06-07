# FR: View Post Likers

## Mục tiêu
Cho phép xem ai đã thích bài viết, mở từ số like trên feed / post detail.

## Phạm vi
- **In scope:** API phân trang likers; modal danh sách + search client-side; load more.
- **Out of scope:** Export danh sách; thông báo realtime khi có like mới trong modal.

## User story
Là người dùng, khi bấm vào số like của post, tôi muốn xem danh sách người đã thích.

## Acceptance criteria
1. `GET /api/v1/social/posts/{postId}/likes` yêu cầu JWT.
2. Viewer phải có quyền xem post (cùng rule visibility như xem post).
3. Response `items[]`: `userId`, `displayName`, `avatarUrl`, `likedAt`; kèm `meta` phân trang.
4. FE: `LikeCountButton` → `LikesListModal` (`targetType: post`).
5. Like count = 0 → không mở modal / nút disabled.

## API doc
`docs/api_fe_behavior/social_api_fe_behavior/ViewPostLikers-api-and-behavior.md`

## FE files
- `likesApi.js`, `useLikeUsersList.js`, `useLikesListModal.js`, `LikesListModal.jsx`, `LikeCountButton.jsx`