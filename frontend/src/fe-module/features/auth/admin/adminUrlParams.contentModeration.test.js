import { describe, expect, it } from "vitest";
import { buildAdminSearchParams } from "./adminUrlParams.js";

describe("buildAdminSearchParams contentModeration", () => {
  it("only keeps filter params for the active tab", () => {
    const preserve = new URLSearchParams(
      "section=contentModeration&tab=post-moderation&post_mod_page=2&cmt_mod_page=3&shop_mod_page=1",
    );

    const next = buildAdminSearchParams({
      section: "contentModeration",
      tab: "comment-moderation",
      preserve,
    });

    expect(next.get("tab")).toBe("comment-moderation");
    expect(next.get("post_mod_page")).toBeNull();
    expect(next.get("cmt_mod_page")).toBe("3");
    expect(next.get("shop_mod_page")).toBeNull();
  });

  it("clears selection ids when switching away from their tab", () => {
    const preserve = new URLSearchParams(
      "section=contentModeration&tab=post-moderation&postId=abc123&commentId=def456",
    );

    const next = buildAdminSearchParams({
      section: "contentModeration",
      tab: "comment-moderation",
      preserve,
    });

    expect(next.get("postId")).toBeNull();
    expect(next.get("commentId")).toBe("def456");
  });

  it("clears postId when clearPostSelection is set", () => {
    const preserve = new URLSearchParams(
      "section=contentModeration&tab=post-moderation&postId=abc123&post_mod_page=1",
    );

    const next = buildAdminSearchParams({
      section: "contentModeration",
      tab: "post-moderation",
      clearPostSelection: true,
      preserve,
    });

    expect(next.get("postId")).toBeNull();
    expect(next.get("post_mod_page")).toBe("1");
  });
});
