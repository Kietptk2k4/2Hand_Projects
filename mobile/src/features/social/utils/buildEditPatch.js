function arraysEqualJson(a, b) {
  return JSON.stringify(a) === JSON.stringify(b);
}

export function buildPatchSnapshot(post) {
  return {
    caption: (post.caption || "").trim(),
    visibility: post.visibility || "PUBLIC",
    allowComments: post.allowComments !== false,
    hashtags: [...(post.hashtags || [])],
    media: (post.media || []).map((item) => ({
      url: item.url,
      type: item.type,
    })),
  };
}

export function buildEditPatchBody(initial, current) {
  const patch = {};
  const nextCaption = (current.caption || "").trim();

  if (nextCaption !== initial.caption) {
    patch.caption = nextCaption || null;
  }
  if (current.visibility !== initial.visibility) {
    patch.visibility = current.visibility;
  }
  if (current.allowComments !== initial.allowComments) {
    patch.allowComments = current.allowComments;
  }
  if (!arraysEqualJson(current.hashtags, initial.hashtags)) {
    patch.hashtags = current.hashtags;
  }
  if (!arraysEqualJson(current.media, initial.media)) {
    patch.media = current.media;
  }

  return patch;
}
