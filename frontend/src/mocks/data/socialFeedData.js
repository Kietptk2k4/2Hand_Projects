/** Followee author IDs (ACCEPTED) for mock following feed */
export const MOCK_FOLLOWEE_IDS = [
  "a5af3d91-b53a-4b82-9eef-1dfa15e1fbb5",
  "d7548df7-8b14-4a35-86cc-3f3e6adcbaf3",
];

const CDN = "https://images.unsplash.com";

function post(overrides) {
  return {
    visibility: "PUBLIC",
    likeCount: 0,
    replyCount: 0,
    hashtags: [],
    allowComments: true,
    createdAt: "2026-05-18T10:15:30Z",
    updatedAt: "2026-05-18T10:20:30Z",
    media: [],
    ...overrides,
  };
}

/** Sorted newest first (createdAt DESC) */
export const mockGlobalFeedPosts = [
  post({
    postId: "674a100000000000000000001",
    authorId: MOCK_FOLLOWEE_IDS[0],
    caption:
      "Just wrapped up an incredible seminar on cross-border taxation. Key takeaways for e-commerce platforms are massive. #TaxLaw #Ecommerce #Consulting",
    hashtags: ["TaxLaw", "Ecommerce", "Consulting"],
    likeCount: 245,
    replyCount: 42,
    createdAt: "2026-05-29T08:00:00Z",
    updatedAt: "2026-05-29T08:05:00Z",
    media: [
      {
        url: `${CDN}/photo-1521737711867-e3b97375f902?w=800&q=80`,
        type: "IMAGE",
      },
    ],
  }),
  post({
    postId: "674a100000000000000000002",
    authorId: "b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1",
    caption:
      "Excited to announce my new portfolio site! Thanks to the 2Hands community for beta feedback. #WebDev #Portfolio",
    hashtags: ["WebDev", "Portfolio"],
    likeCount: 1200,
    replyCount: 89,
    createdAt: "2026-05-28T14:30:00Z",
    updatedAt: "2026-05-28T14:30:00Z",
  }),
  post({
    postId: "674a100000000000000000003",
    authorId: MOCK_FOLLOWEE_IDS[1],
    caption: "Spring microservices tip: keep feed reads separate from write paths. #spring #java",
    hashtags: ["spring", "java"],
    likeCount: 12,
    replyCount: 3,
    createdAt: "2026-05-27T10:15:30Z",
    media: [
      { url: `${CDN}/photo-1517694712202-14dd9538aa43?w=800&q=80`, type: "IMAGE" },
      { url: `${CDN}/photo-1498050108023-c5249f4df085?w=800&q=80`, type: "IMAGE" },
    ],
  }),
  post({
    postId: "674a100000000000000000004",
    authorId: "c0000000-0000-4000-8000-000000000099",
    caption: "Platform maintenance completed. All social APIs are green.",
    likeCount: 8,
    replyCount: 1,
    createdAt: "2026-05-26T09:00:00Z",
  }),
  post({
    postId: "674a100000000000000000005",
    authorId: MOCK_FOLLOWEE_IDS[0],
    caption: "Remote work checklist for 2024 — thread in comments.",
    hashtags: ["RemoteWork2024"],
    likeCount: 56,
    replyCount: 14,
    createdAt: "2026-05-25T16:00:00Z",
    media: [
      { url: `${CDN}/photo-1522071820081-009f0129c71c?w=800&q=80`, type: "IMAGE" },
      { url: `${CDN}/photo-1600880292203-757bb62b4baf?w=800&q=80`, type: "IMAGE" },
      { url: `${CDN}/photo-1552664730-d307ca884978?w=800&q=80`, type: "IMAGE" },
      { url: `${CDN}/photo-1556761175-b413da4baf72?w=800&q=80`, type: "IMAGE" },
    ],
  }),
  post({
    postId: "674a100000000000000000006",
    authorId: MOCK_FOLLOWEE_IDS[1],
    caption: "hello world",
    likeCount: 4,
    replyCount: 1,
    createdAt: "2026-05-24T11:00:00Z",
  }),
  post({
    postId: "674a100000000000000000007",
    authorId: "b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1",
    caption: "Long caption for testing Xem thêm in feed. ".repeat(12),
    likeCount: 2,
    replyCount: 0,
    createdAt: "2026-05-23T08:00:00Z",
  }),
  post({
    postId: "674a100000000000000000008",
    authorId: MOCK_FOLLOWEE_IDS[0],
    caption: "Freelance tips: invoice early, communicate often.",
    hashtags: ["FreelanceTips"],
    likeCount: 31,
    replyCount: 5,
    createdAt: "2026-05-22T12:00:00Z",
  }),
  post({
    postId: "674a100000000000000000009",
    authorId: MOCK_FOLLOWEE_IDS[1],
    caption: "AI in finance — what we are watching this quarter.",
    hashtags: ["AIinFinance"],
    likeCount: 99,
    replyCount: 22,
    createdAt: "2026-05-21T09:00:00Z",
    media: [{ url: `${CDN}/photo-1551288049-bebda4e38f71?w=800&q=80`, type: "IMAGE" }],
  }),
  post({
    postId: "674a10000000000000000000a",
    authorId: "c0000000-0000-4000-8000-000000000099",
    caption: "Welcome new providers to 2Hands!",
    likeCount: 150,
    replyCount: 10,
    createdAt: "2026-05-20T10:00:00Z",
  }),
  post({
    postId: "674a10000000000000000000b",
    authorId: MOCK_FOLLOWEE_IDS[0],
    caption: "Legal tech roundup #LegalTech",
    hashtags: ["LegalTech"],
    likeCount: 17,
    replyCount: 2,
    createdAt: "2026-05-19T15:00:00Z",
  }),
  post({
    postId: "674a10000000000000000000c",
    authorId: MOCK_FOLLOWEE_IDS[1],
    caption: "Following-only visibility demo post.",
    visibility: "FOLLOWERS",
    likeCount: 4,
    replyCount: 1,
    createdAt: "2026-05-18T10:16:30Z",
    updatedAt: "2026-05-18T10:20:40Z",
    hashtags: ["social"],
  }),
  post({
    postId: "674a10000000000000000000d",
    authorId: "b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1",
    caption: "Weekend project: social feed UI with React + Tailwind.",
    likeCount: 7,
    replyCount: 0,
    createdAt: "2026-05-17T18:00:00Z",
  }),
  post({
    postId: "674a10000000000000000000e",
    authorId: MOCK_FOLLOWEE_IDS[0],
    caption: "Pagination test post 14",
    likeCount: 1,
    replyCount: 0,
    createdAt: "2026-05-16T10:00:00Z",
  }),
  post({
    postId: "674a10000000000000000000f",
    authorId: MOCK_FOLLOWEE_IDS[1],
    caption: "Pagination test post 15",
    likeCount: 1,
    replyCount: 0,
    createdAt: "2026-05-15T10:00:00Z",
  }),
  post({
    postId: "674a100000000000000000010",
    authorId: MOCK_FOLLOWEE_IDS[0],
    caption: "Pagination test post 16",
    likeCount: 1,
    replyCount: 0,
    createdAt: "2026-05-14T10:00:00Z",
  }),
  post({
    postId: "674a100000000000000000011",
    authorId: MOCK_FOLLOWEE_IDS[1],
    caption: "Pagination test post 17",
    likeCount: 1,
    replyCount: 0,
    createdAt: "2026-05-13T10:00:00Z",
  }),
  post({
    postId: "674a100000000000000000012",
    authorId: "b8b9bf76-2ab2-4a01-8f16-fd0f5f9f95d1",
    caption: "Pagination test post 18",
    likeCount: 1,
    replyCount: 0,
    createdAt: "2026-05-12T10:00:00Z",
  }),
  post({
    postId: "674a100000000000000000013",
    authorId: MOCK_FOLLOWEE_IDS[0],
    caption: "Pagination test post 19",
    likeCount: 1,
    replyCount: 0,
    createdAt: "2026-05-11T10:00:00Z",
  }),
  post({
    postId: "674a100000000000000000014",
    authorId: MOCK_FOLLOWEE_IDS[1],
    caption: "Pagination test post 20 — last on page 0 when size=20",
    likeCount: 1,
    replyCount: 0,
    createdAt: "2026-05-10T10:00:00Z",
  }),
  post({
    postId: "674a100000000000000000015",
    authorId: MOCK_FOLLOWEE_IDS[0],
    caption: "Pagination test post 21 — appears on page 1",
    likeCount: 1,
    replyCount: 0,
    createdAt: "2026-05-09T10:00:00Z",
  }),
  post({
    postId: "674a100000000000000000016",
    authorId: MOCK_FOLLOWEE_IDS[1],
    caption: "Pagination test post 22",
    likeCount: 1,
    replyCount: 0,
    createdAt: "2026-05-08T10:00:00Z",
  }),
];

/** Posts from followees only; PUBLIC + FOLLOWERS */
export const mockFollowingFeedPosts = mockGlobalFeedPosts.filter(
  (item) =>
    MOCK_FOLLOWEE_IDS.includes(item.authorId) &&
    (item.visibility === "PUBLIC" || item.visibility === "FOLLOWERS")
);
