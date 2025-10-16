-- Test Status Enum
CREATE TYPE test_status AS ENUM ('DRAFT', 'PUBLISHED');

-- Question Type Enum
CREATE TYPE question_type AS ENUM ('CLOSED_SINGLE', 'CLOSED_MULTIPLE', 'OPEN_TEXT');

-- Tests
CREATE TABLE IF NOT EXISTS tests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_by UUID NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT NOT NULL,
    is_premium BOOLEAN NOT NULL DEFAULT FALSE,
    status test_status NOT NULL DEFAULT 'DRAFT',
    question_count INTEGER NOT NULL DEFAULT 0,
    total_possible_score INTEGER NOT NULL DEFAULT 0,
    published_at TIMESTAMP WITHOUT TIME ZONE NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT fk_tests_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_tests_status ON tests (status);
CREATE INDEX IF NOT EXISTS idx_tests_is_premium ON tests (is_premium);
CREATE INDEX IF NOT EXISTS idx_tests_created_by ON tests (created_by);
CREATE INDEX IF NOT EXISTS idx_tests_published_at ON tests (published_at);

-- Questions
CREATE TABLE IF NOT EXISTS questions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    test_id UUID NOT NULL,
    question_type question_type NOT NULL,
    question_text TEXT NOT NULL,
    image_url VARCHAR(500) NULL,
    score INTEGER NOT NULL CHECK (score > 0),
    order_index INTEGER NOT NULL DEFAULT 0,
    correct_answer TEXT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT fk_questions_test FOREIGN KEY (test_id) REFERENCES tests(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_questions_test_id ON questions (test_id);
CREATE INDEX IF NOT EXISTS idx_questions_order ON questions (test_id, order_index);

-- Answers
CREATE TABLE IF NOT EXISTS answers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id UUID NOT NULL,
    answer_text TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT fk_answers_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_answers_question_id ON answers (question_id);
CREATE INDEX IF NOT EXISTS idx_answers_order ON answers (question_id, order_index);
