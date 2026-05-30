CREATE DATABASE IF NOT EXISTS ai_analyzer;
USE ai_analyzer;

CREATE TABLE IF NOT EXISTS prompts (
    prompt_id INT AUTO_INCREMENT PRIMARY KEY,
    original_prompt TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS optimized_prompts (
    opt_id INT AUTO_INCREMENT PRIMARY KEY,
    prompt_id INT NOT NULL,
    optimized_prompt TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (prompt_id) REFERENCES prompts(prompt_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS analysis (
    analysis_id INT AUTO_INCREMENT PRIMARY KEY,
    prompt_id INT NOT NULL,
    token_count INT NOT NULL,
    complexity VARCHAR(20) NOT NULL,
    recommended_model VARCHAR(50) NOT NULL,
    cost_category VARCHAR(20) NOT NULL,
    suggestion TEXT,
    analyzed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (prompt_id) REFERENCES prompts(prompt_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    analysis_id INT NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'SENT',
    sent_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (analysis_id) REFERENCES analysis(analysis_id) ON DELETE CASCADE
);
