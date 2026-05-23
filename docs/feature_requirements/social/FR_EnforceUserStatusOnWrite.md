# Functional Requirement (FR) - Enforce User Status On Write

## 1. Feature Overview

Quy tac **cross-cutting**: moi thao tac **ghi** (create/update/delete/toggle) tren Social phai kiem tra trang thai user trong `user_projections` truoc khi thuc thi. Dam bao user `SUSPENDED`/`DELETED` (va sau nay `RESTRICT`) khong tac dong len he thong social.

## 2. Actors

- **User:** Thuc hien hanh dong write.
- **System:** Doc projection; tra loi loi chuan.

## 3. Scope

- **In Scope:**
  - Guard tai application layer (use case hoac shared `UserWriteGuard`).
  - Map status → HTTP + business code.
  - Danh sach use case write bat buoc guard.
- **Out of Scope:**
  - Read-only APIs (feed, view post, list comments) — tru khi product chan doc suspended user.
  - Auth login block (Auth `FR_ApplyUserEnforcement`).
  - Admin moderation HTTP (role admin rieng).

## 4. Preconditions

- JWT xac thuc `user_id` (actor).
- Projection da dong bo (`FR_ConsumeAuthUserEvents`) — neu thieu projection: policy **fail-closed** (403) hoac **fail-open** cho read — **write khuyen nghi fail-closed**.

## 5. Enforcement Matrix (MVP)

| projection.status | Write actions | HTTP | code |
|-------------------|---------------|------|------|
| `ACTIVE` | Cho phep (tuy permission/ownership) | — | — |
| `SUSPENDED` | Chan tat ca write user | 403 | `SOCIAL-403-SUSPENDED` |
| `DELETED` | Chan tat ca write user | 403 | `SOCIAL-403` hoac `SOCIAL-404` |
| Missing projection | Chan write | 403 | `SOCIAL-403` |

Message mau: `"Tai khoan bi dinh chi, khong the thuc hien hanh dong nay."` (da co trong `ErrorCode.ACCOUNT_SUSPENDED`).

## 6. Write Operations Covered

| Nhom | Use case / FR |
|------|----------------|
| Post | `FR_CreatePost`, `FR_EditPost`, `FR_DeletePost`, `FR_LikePost`, `FR_UnlikePost`, `FR_SavePost`, `FR_UnsavePost` |
| Comment | `FR_CommentPost`, `FR_ReplyComment`, `FR_DeleteOwnComment`, like comment |
| Graph | `FR_FollowUser`, `FR_UnfollowUser` |
| Discovery | Ghi `SEARCH_HISTORY` (neu co) |

**Khong ap dung** cho:

- `FR_ViewGlobalFeed`, `FR_ViewPostDetail`, `FR_ListPostComments`, `FR_ViewUserPosts`, `FR_ViewSocialProfile` (read).

## 7. Business Rules

- `user_id` actor lay tu JWT, **khong** tu request body.
- Kiem tra **actor**, khong chi author target (vi du follow: chan follower suspended).
- Admin/Moderator role bypass guard user status **chi** khi goi API co role he thong (khong dung JWT user bi suspended).
- Sau `USER_SUSPENDED` event, write bi chan eventual (vai giay) — acceptable.
- **RESTRICT (future):** projection co the them `restricted_actions: ["CREATE_POST","COMMENT",...]`; guard chan subset write khi `status = ACTIVE` nhung restricted.

## 8. Implementation Pattern

```text
delivery (controller)
  → application use case
      → UserWriteGuard.assertCanWrite(actorUserId)  // doc projection
      → domain logic + repository
```

- Khong dat logic trong controller/DTO.
- Khong query Auth DB.

**Hien trang code:** mot so use case da goi `userProjectionRepository` + `isActionForbidden()` (vd `CreatePostUseCase`, `FollowUserUseCase`, `DeleteOwnCommentUseCase`). FR nay yeu cau **dong bo** cho tat ca write use case con thieu.

## 9. Database Impact

- Read `user_projections` by `user_id` (MongoDB).

## 10. Transaction

- Read trong cung transaction boundary use case (khong bat buoc transactional read).

## 11. Security

- Fail-closed cho write khi khong co projection (tranh suspended user tao content khi consumer cham).
- Khong log ly do enforcement chi tiet ra client (chi message chung).

## 12. Failure Cases

- Suspended → 403 `SOCIAL-403-SUSPENDED`.
- Deleted → 403.
- Projection missing → 403 (write).

## 13. Acceptance Criteria

- **AC1:** User `SUSPENDED` goi `POST /posts` → 403 `SOCIAL-403-SUSPENDED`.
- **AC2:** User `ACTIVE` tao post thanh cong.
- **AC3:** Sau consume `USER_SUSPENDED`, like/comment bi chan.
- **AC4:** Read feed van hoat dong voi user suspended (neu product cho phep login read-only).

## 14. Related

| FR / Tai lieu | Muc dich |
|---------------|----------|
| `FR_ConsumeAuthUserEvents` | Cap nhat status |
| `FR_CreatePost` | Da co validate author mau |
| `auth/FR_ApplyUserEnforcement` | Nguon suspend phia Auth |
| `docs/business-spec/social-service-spec.md` | Preconditions user hop le |

## 15. Implementation Notes (hien trang)

- `ErrorCode.ACCOUNT_SUSPENDED` **da ton tai**.
- Can audit danh sach controller write va them guard thong nhat.
