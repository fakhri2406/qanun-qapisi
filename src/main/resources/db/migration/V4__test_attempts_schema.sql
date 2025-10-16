-- Attempt Status Enum
CREATE TYPE attempt_status AS ENUM ('IN_PROGRESS', 'COMPLETED');

-- Test Attempts
CREATE TABLE IF NOT EXISTS test_attempts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    test_id UUID NOT NULL,
    total_score INTEGER NOT NULL DEFAULT 0,
    max_possible_score INTEGER NOT NULL,
    status attempt_status NOT NULL DEFAULT 'IN_PROGRESS',
    started_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    submitted_at TIMESTAMP WITHOUT TIME ZONE NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT fk_test_attempts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_test_attempts_test FOREIGN KEY (test_id) REFERENCES tests(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_test_attempts_user_id ON test_attempts (user_id);
CREATE INDEX IF NOT EXISTS idx_test_attempts_test_id ON test_attempts (test_id);
CREATE INDEX IF NOT EXISTS idx_test_attempts_user_test ON test_attempts (user_id, test_id);
CREATE INDEX IF NOT EXISTS idx_test_attempts_status ON test_attempts (status);

-- User Answers
CREATE TABLE IF NOT EXISTS user_answers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    test_attempt_id UUID NOT NULL,
    question_id UUID NOT NULL,
    selected_answer_ids UUID[] NULL,
    open_text_answer TEXT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    score_earned INTEGER NOT NULL DEFAULT 0,
    answered_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT fk_user_answers_attempt FOREIGN KEY (test_attempt_id) REFERENCES test_attempts(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_answers_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_user_answers_attempt_id ON user_answers (test_attempt_id);
CREATE INDEX IF NOT EXISTS idx_user_answers_question_id ON user_answers (question_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_user_answers_attempt_question ON user_answers (test_attempt_id, question_id);
