# FR: View Comment Likers

## Mục tiêu
Cho phép xem ai đã thích bình luận, mở từ số like trong thread comment.

## Phạm vi
- **In scope:** API phân trang likers comment; modal dùng chung với post likers.
- **Out of scope:** Likers cho reaction khác ngoài LIKE.

## User story
Là người dùng, khi bấm vào số like của comment, tôi muốn xem danh sách người đã thích.

## Acceptance criteria
1. `GET /api/v1/social/comments/{commentId}/likes` yêu cầu JWT.
2. Comment phải `ACTIVE`; đã xóa → 404.
3. Response `items[]`: `userId`, `displayName`, `avatarUrl`, `likedAt`; kèm `meta` phân trang.
4. FE: `CommentItem` + `LikeCountButton` → `LikesListModal` (`targetType: comment`).
5. Top-level comment và reply đều hỗ trợ mở modal likers.

## API doc
`docs/api_fe_behavior/social_api_fe_behavior/ViewCommentLikers-api-and-behavior.md`

## FE files
- `likesApi.js`, `useLikeUsersList.js`, `useLikesListModal.js`, `LikesListModal.jsx`, `CommentItem.jsx`