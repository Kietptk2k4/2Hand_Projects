# FR: View Suggested Users

## Mục tiêu
Gợi ý người dùng nên follow trên feed sidebar, tăng kết nối xã hội.

## Phạm vi
- **In scope:** API phân trang gợi ý user; FE sidebar (3 user) + modal mở rộng; follow/unfollow inline.
- **Out of scope:** ML recommendation; gợi ý theo interest graph phức tạp.

## User story
Là người dùng đã đăng nhập, tôi muốn thấy người có mutual follow để follow họ nhanh chóng.

## Acceptance criteria
1. `GET /api/v1/social/users/suggestions` yêu cầu JWT.
2. Hỗ trợ `page`, `size` (1–50); `limit` là alias của `size`.
3. Loại trừ bản thân và user đã follow.
4. Sắp xếp theo `mutualFollowCount` giảm dần.
5. Item có `userId`, `displayName`, `avatarUrl`, `followStatus`, `mutualFollowCount`.
6. FE sidebar `limit=3`; modal expanded `limit=20`.

## API doc
`docs/api_fe_behavior/social_api_fe_behavior/ViewSuggestedUsers-api-and-behavior.md`

## FE files
- `discoveryApi.js`, `useSuggestedUsers.js`, `FeedRightSidebar.jsx`, `SuggestedUsersModal.jsx`