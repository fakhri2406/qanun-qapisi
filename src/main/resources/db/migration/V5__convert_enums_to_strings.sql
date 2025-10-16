-- Convert enum types to VARCHAR columns
-- This migration converts the custom enum types to simple VARCHAR columns

-- First, remove any default values that depend on the enum types
ALTER TABLE tests ALTER COLUMN status DROP DEFAULT;
ALTER TABLE questions ALTER COLUMN question_type DROP DEFAULT;

-- Update tests table
ALTER TABLE tests ALTER COLUMN status TYPE VARCHAR(20);

-- Update questions table  
ALTER TABLE questions ALTER COLUMN question_type TYPE VARCHAR(20);

-- Drop the custom enum types (they're no longer needed)
DROP TYPE IF EXISTS test_status CASCADE;
DROP TYPE IF EXISTS question_type CASCADE;
