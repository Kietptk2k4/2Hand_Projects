/*
 * SOCIAL SERVICE MONGODB INIT SCRIPT
 * Collections: posts, comments
 * Note: This script is for mongosh/manual init (not Flyway).
 */

const dbName = process.env.MONGO_INITDB_DATABASE || "social_db";
const socialDb = db.getSiblingDB(dbName);

// POSTS collection
socialDb.createCollection("posts");

// COMMENTS collection
socialDb.createCollection("comments");

// USER_PROJECTIONS collection (synced from Auth Service)
socialDb.createCollection("user_projections");

// ---------------------------------------------------------------------
// POSTS indexes (from docs/business-spec/social-service-spec.md)
// ---------------------------------------------------------------------
socialDb.posts.createIndex(
  { status: 1, visibility: 1, created_at: -1 },
  { name: "idx_posts_status_visibility_created_desc" }
);

socialDb.posts.createIndex(
  { hashtags: 1 },
  { name: "idx_posts_hashtags" }
);

socialDb.posts.createIndex(
  { author_id: 1, created_at: -1 },
  { name: "idx_posts_author_created_desc" }
);

socialDb.posts.createIndex(
  { author_id: 1, status: 1, created_at: -1 },
  { name: "idx_posts_author_status_created_desc" }
);

// ---------------------------------------------------------------------
// COMMENTS indexes (from docs/business-spec/social-service-spec.md)
// ---------------------------------------------------------------------
socialDb.comments.createIndex(
  { post_id: 1, created_at: 1 },
  { name: "idx_comments_post_created_asc" }
);

socialDb.comments.createIndex(
  { post_id: 1, status: 1, created_at: 1 },
  { name: "idx_comments_post_status_created_asc" }
);

socialDb.comments.createIndex(
  { parent_comment_id: 1 },
  { name: "idx_comments_parent_comment" }
);

print(`Mongo init completed for database: ${dbName}`);
