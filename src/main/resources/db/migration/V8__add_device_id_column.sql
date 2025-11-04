ALTER TABLE users ADD COLUMN device_id VARCHAR(255);

CREATE INDEX idx_users_device_id ON users(device_id);
