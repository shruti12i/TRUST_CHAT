CREATE DATABASE IF NOT EXISTS trust_chat;
USE trust_chat;

CREATE TABLE IF NOT EXISTS users (
    user_id    INT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  UNIQUE NOT NULL,
    password   VARCHAR(255) NOT NULL,
    email      VARCHAR(100) UNIQUE NOT NULL,
    role       ENUM('admin', 'teacher', 'student') NOT NULL DEFAULT 'student',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    is_active  BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS messages (
    message_id   INT AUTO_INCREMENT PRIMARY KEY,
    sender_id    INT  NOT NULL,
    receiver_id  INT  NOT NULL,
    message_text TEXT NOT NULL,
    sent_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read      BOOLEAN DEFAULT FALSE,
    is_blocked   BOOLEAN DEFAULT FALSE,
    block_reason VARCHAR(255),
    FOREIGN KEY (sender_id)   REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_sender   (sender_id),
    INDEX idx_receiver (receiver_id),
    INDEX idx_sent_at  (sent_at)
);

CREATE TABLE IF NOT EXISTS policy_rules (
    rule_id    INT AUTO_INCREMENT PRIMARY KEY,
    rule_name  VARCHAR(100) NOT NULL,
    rule_type  ENUM('keyword', 'time', 'user_role') NOT NULL,
    rule_value TEXT NOT NULL,
    is_active  BOOLEAN DEFAULT TRUE,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL,
    UNIQUE KEY unique_rule (rule_name, rule_type)
);

CREATE TABLE IF NOT EXISTS message_logs (
    log_id    INT AUTO_INCREMENT PRIMARY KEY,
    user_id   INT NOT NULL,
    action    VARCHAR(50) NOT NULL,
    details   TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_timestamp (timestamp)
);

CREATE TABLE IF NOT EXISTS offline_messages (
    offline_id   INT AUTO_INCREMENT PRIMARY KEY,
    sender_id    INT  NOT NULL,
    receiver_id  INT  NOT NULL,
    message_text TEXT NOT NULL,
    queued_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivered_at TIMESTAMP NULL,
    FOREIGN KEY (sender_id)   REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(user_id) ON DELETE CASCADE
);

INSERT IGNORE INTO users (username, password, email, role)
VALUES ('admin', 'admin123', 'admin@trustchat.com', 'admin');

INSERT IGNORE INTO users (username, password, email, role)
VALUES ('teacher1', 'teacher123', 'teacher1@trustchat.com', 'teacher');

INSERT IGNORE INTO users (username, password, email, role)
VALUES ('student1', 'student123', 'student1@trustchat.com', 'student');

INSERT IGNORE INTO policy_rules (rule_name, rule_type, rule_value, created_by, is_active)
VALUES ('Block Bad Words', 'keyword', 'spam,badword,offensive', 1, TRUE);

INSERT IGNORE INTO policy_rules (rule_name, rule_type, rule_value, created_by, is_active)
VALUES ('School Hours Only', 'time', '08:00:00-20:00:00', 1, TRUE);

INSERT IGNORE INTO policy_rules (rule_name, rule_type, rule_value, created_by, is_active)
VALUES ('Student-to-Student Block', 'user_role', 'student->student:deny', 1, TRUE);

DELETE p1 FROM policy_rules p1
INNER JOIN policy_rules p2
WHERE p1.rule_id > p2.rule_id
  AND p1.rule_name = p2.rule_name
  AND p1.rule_type = p2.rule_type;

DELETE FROM policy_rules
WHERE (rule_name, rule_type) NOT IN (
    ('Block Bad Words', 'keyword'),
    ('School Hours Only', 'time'),
    ('Student-to-Student Block', 'user_role')
);
