# UC - Review Moderation

## 1. Overview

Use case nay mo ta admin moderation cho Commerce reviews: hide, restore va soft remove. Admin Service ghi moderation decision, Commerce Service own review status and rating recalculation.

## 2. Actors

- **Admin/Moderator:** Hide/restore/remove review.
- **Commerce Service:** Apply review state.

## 3. Related Data

- `content_moderation_logs`
- `admin_action_logs`
- `outbox_events`

## 4. Business Rules

- Hide review requires `REVIEW_HIDE`.
- Hidden review remains in DB and is not public-visible.
- Admin cannot edit buyer rating/comment.
- Commerce recalculates rating if needed.

## 5. Sub-Use Cases

### 5.1. Hide Review

**Main Flow:**

1. Admin requests hide review.
2. System checks permission.
3. System logs moderation and admin action.
4. System publishes `REVIEW_HIDDEN`.

### 5.2. Restore Review

**Main Flow:** Admin logs restore and publishes `REVIEW_RESTORED`.

### 5.3. Remove Review

**Main Flow:** Admin logs soft remove and publishes review moderation event according Commerce contract.

## 6. Acceptance Criteria

- Review moderation requires permission.
- Logs are written.
- Review event is published through outbox.
- Admin Service does not mutate Commerce review DB.

