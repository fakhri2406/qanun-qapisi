CREATE UNIQUE INDEX IF NOT EXISTS uq_questions_test_order
    ON questions (test_id, order_index);

CREATE UNIQUE INDEX IF NOT EXISTS uq_answers_question_order
    ON answers (question_id, order_index);
