--DROP TABLE IF EXISTS "reviews";
--CREATE TABLE IF NOT EXISTS "users" ("id" VARCHAR(128) PRIMARY KEY, "header" VARCHAR(256) NOT NULL, "message" VARCHAR(256) NOT NULL, "score" INTEGER NOT NULL, "user_id" VARCHAR(256) NOT NULL, "property_id" VARCHAR(256) NOT NULL);
INSERT INTO "reviews" ("id", "header", "message", "score", "user_id", "property_id") VALUES ('0', 'AWESOME', 'The stay was grate.', 10, '1', '2');
INSERT INTO "reviews" ("id", "header", "message", "score", "user_id", "property_id") VALUES ('1', 'srsly?.....', 'I hosted u last tim but now u do dis? y? y u bad host???!?!', 2, '2', '1');
