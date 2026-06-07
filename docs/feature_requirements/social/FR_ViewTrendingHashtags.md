# FR: View Trending Hashtags

## Mục tiêu
Hiển thị hashtag thịnh hành trên sidebar feed để người dùng khám phá nội dung theo chủ đề.

## Phạm vi
- **In scope:** API read-only trả top hashtag theo engagement 7 ngày; FE sidebar + link sang trang hashtag.
- **Out of scope:** Tùy chỉnh cửa sổ thời gian từ FE; realtime push khi hashtag thay đổi.

## User story
Là người dùng đã đăng nhập, tôi muốn thấy hashtag đang hot để bấm vào xem các bài viết liên quan.

## Acceptance criteria
1. `GET /api/v1/social/search/trending-hashtags` yêu cầu JWT.
2. Query `limit` mặc định 5, tối đa 20.
3. Mỗi item có `tag`, `postCount`, `totalLikes`, `totalReplies`, `engagementCount`, `score`.
4. Chỉ tính post `ACTIVE` trong 7 ngày gần nhất.
5. FE `FeedRightSidebar` gọi API qua `useTrendingHashtags`; click tag → `/social/hashtag/{tag}`.

## API doc
`docs/api_fe_behavior/social_api_fe_behavior/ViewTrendingHashtags-api-and-behavior.md`

## FE files
- `discoveryApi.js`, `useTrendingHashtags.js`, `FeedRightSidebar.jsx`