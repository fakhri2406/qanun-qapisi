-- Remove profile picture URLs from users table
ALTER TABLE users DROP COLUMN IF EXISTS profile_picture_url;

-- Remove image URLs from questions table
ALTER TABLE questions DROP COLUMN IF EXISTS image_url;
