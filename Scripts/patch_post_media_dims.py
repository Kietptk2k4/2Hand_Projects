# -*- coding: utf-8 -*-
import re
from pathlib import Path

ROOT = Path(r"d:/Projects/2Hand_Projects/Services/social-service/src/main/java/com/twohands/social_service")

def patch(path, old, new):
    p = ROOT / path
    text = p.read_text(encoding="utf-8")
    if old not in text:
        raise SystemExit(f"MISSING in {path}: {old[:80]}...")
    p.write_text(text.replace(old, new, 1), encoding="utf-8", newline="\n")
    print("patched", path)

# Commands
patch(
    "application/post/createpost/CreatePostCommand.java",
    "    public record MediaItemCommand(String url, String type) {\n    }",
    "    public record MediaItemCommand(String url, String type, Integer width, Integer height) {\n    }",
)
patch(
    "application/post/editpost/EditPostCommand.java",
    "    public record MediaItemCommand(String url, String type) {\n    }",
    "    public record MediaItemCommand(String url, String type, Integer width, Integer height) {\n    }",
)

# Result MediaItemData records
media_data_record = """    public record MediaItemData(String url, String type, Integer width, Integer height) {
    }"""
for rel in [
    "application/post/createpost/CreatePostResult.java",
    "application/post/editpost/EditPostResult.java",
    "application/post/viewpostdetail/ViewPostDetailResult.java",
    "application/post/viewsavedposts/ViewSavedPostsResult.java",
    "application/user/viewuserposts/ViewUserPostsResult.java",
    "application/feed/viewglobalfeed/ViewGlobalFeedResult.java",
    "application/search/searchpost/SearchPostResult.java",
    "application/search/searchhashtag/SearchHashtagResult.java",
]:
    patch(rel, "    public record MediaItemData(String url, String type) {\n    }", media_data_record)

# HTTP nested MediaItemResponse in feed
patch(
    "delivery/http/feed/response/ViewGlobalFeedResponse.java",
    """    public record MediaItemResponse(
            String url,
            String type
    ) {
    }""",
    """    public record MediaItemResponse(
            String url,
            String type,
            Integer width,
            Integer height
    ) {
    }""",
)

# Create/Edit/ViewPostDetail HTTP responses - replace inner MediaItemResponse
inner = """    public record MediaItemResponse(
            String url,
            String type,
            Integer width,
            Integer height
    ) {
    }"""
for rel, old in [
    ("delivery/http/post/response/CreatePostResponse.java", "    public record MediaItemResponse(String url, String type) {\n    }"),
    ("delivery/http/post/response/EditPostResponse.java", "    public record MediaItemResponse(String url, String type) {\n    }"),
    ("delivery/http/post/response/ViewSavedPostsResponse.java", "    public record MediaItemResponse(\n            String url,\n            String type\n    ) {\n    }"),
]:
    patch(rel, old, inner)

patch(
    "delivery/http/post/response/ViewPostDetailResponse.java",
    """    public record MediaItemResponse(
            String url,
            String type
    ) {
    }""",
    inner,
)

patch(
    "delivery/http/search/response/SearchPostResponse.java",
    """    public record MediaItemResponse(
            String url,
            String type
    ) {
    }""",
    inner,
)

print("done patches part 1")
