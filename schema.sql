-- ============================================================
-- Employee Performance Evaluation System - Database Schema
-- Run this whole file in MySQL Workbench (lightning bolt button)
-- ============================================================

DROP DATABASE IF EXISTS epes_db;
CREATE DATABASE epes_db;
USE epes_db;

-- 1. TEAMS (manager_id foreign key is added after users table exists)
CREATE TABLE teams (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    manager_id  INT NULL
);

-- 2. USERS (both managers and employees)
CREATE TABLE users (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(100) NOT NULL,
    email     VARCHAR(100) NOT NULL UNIQUE,
    password  VARCHAR(255) NOT NULL,          -- stored as SHA-256 hash
    role      ENUM('MANAGER','EMPLOYEE') NOT NULL,
    team_id   INT NULL,
    team_role VARCHAR(50) NULL,               -- e.g. Developer, Tester
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE SET NULL
);

-- Now link teams.manager_id to users
ALTER TABLE teams
    ADD FOREIGN KEY (manager_id) REFERENCES users(id) ON DELETE SET NULL;

-- 3. EVALUATIONS (scores out of 10 per criteria)
CREATE TABLE evaluations (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    employee_id    INT NOT NULL,
    manager_id     INT NOT NULL,
    quality        INT NOT NULL CHECK (quality BETWEEN 1 AND 10),
    teamwork       INT NOT NULL CHECK (teamwork BETWEEN 1 AND 10),
    punctuality    INT NOT NULL CHECK (punctuality BETWEEN 1 AND 10),
    productivity   INT NOT NULL CHECK (productivity BETWEEN 1 AND 10),
    communication  INT NOT NULL CHECK (communication BETWEEN 1 AND 10),
    total_score    DECIMAL(4,2) NOT NULL,     -- average of the 5 scores
    eval_date      DATE NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (manager_id)  REFERENCES users(id) ON DELETE CASCADE
);

-- 4. GOALS (set by manager or by the employee themselves)
CREATE TABLE goals (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    employee_id  INT NOT NULL,
    set_by       INT NOT NULL,
    description  VARCHAR(255) NOT NULL,
    target_date  DATE NOT NULL,
    status       ENUM('PENDING','IN_PROGRESS','COMPLETED') DEFAULT 'PENDING',
    FOREIGN KEY (employee_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (set_by)      REFERENCES users(id) ON DELETE CASCADE
);

-- 5. FEEDBACK
CREATE TABLE feedback (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    employee_id  INT NOT NULL,
    manager_id   INT NOT NULL,
    comments     VARCHAR(500) NOT NULL,
    rating       INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (manager_id)  REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- DEMO DATA (so your screens have something to show)
-- Passwords: manager123 and emp123 (stored as SHA-256 hashes)
-- ============================================================

INSERT INTO teams (name) VALUES ('Development Team');

INSERT INTO users (name, email, password, role, team_id, team_role) VALUES
('Rahul Mehta', 'manager@epes.com', SHA2('manager123', 256), 'MANAGER',  1, 'Team Lead'),
('Priya Sharma', 'priya@epes.com',  SHA2('emp123', 256),     'EMPLOYEE', 1, 'Developer'),
('Amit Verma',   'amit@epes.com',   SHA2('emp123', 256),     'EMPLOYEE', 1, 'Tester');

UPDATE teams SET manager_id = 1 WHERE id = 1;

INSERT INTO evaluations
(employee_id, manager_id, quality, teamwork, punctuality, productivity, communication, total_score, eval_date) VALUES
(2, 1, 6, 7, 8, 6, 7, 6.80, '2026-06-30'),
(2, 1, 7, 7, 8, 7, 7, 7.20, '2026-07-31'),
(2, 1, 8, 8, 8, 7, 8, 7.80, '2026-08-31'),
(2, 1, 8, 9, 9, 8, 8, 8.40, '2026-09-30'),
(3, 1, 5, 6, 7, 6, 6, 6.00, '2026-06-30'),
(3, 1, 6, 6, 7, 6, 7, 6.40, '2026-07-31'),
(3, 1, 7, 7, 7, 7, 7, 7.00, '2026-08-31');

INSERT INTO goals (employee_id, set_by, description, target_date, status) VALUES
(2, 1, 'Complete Java Spring training course', '2026-11-30', 'IN_PROGRESS'),
(3, 1, 'Write test cases for the payment module', '2026-11-15', 'PENDING'),
(2, 2, 'Learn Docker basics', '2026-12-20', 'PENDING');

INSERT INTO feedback (employee_id, manager_id, comments, rating) VALUES
(2, 1, 'Great improvement in code quality this month.', 5),
(3, 1, 'Good testing work, please improve documentation.', 4);

-- Quick check: should show 3 users
SELECT id, name, email, role FROM users;
