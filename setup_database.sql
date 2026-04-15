-- ═══════════════════════════════════════════════════
--  QuantumLibrary — MySQL Database Setup Script
--  MySQL User: root | Password: hussain07
--
--  HOW TO RUN:
--  1. Open MySQL Workbench or Command Prompt
--  2. Connect with: mysql -u root -phussain07
--  3. Run this file: source C:/Users/HUSSAIN/Desktop/33/setup_database.sql
--     OR paste the entire content into MySQL Workbench and Execute
-- ═══════════════════════════════════════════════════

-- Step 1: Create the database
CREATE DATABASE IF NOT EXISTS quantumlibrary
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE quantumlibrary;

-- Step 2: Create users table
CREATE TABLE IF NOT EXISTS users (
  id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name       VARCHAR(100) NOT NULL,
  email      VARCHAR(150) NOT NULL UNIQUE,
  password   VARCHAR(255) NOT NULL,   -- BCrypt encoded (handled by Spring Boot)
  role       VARCHAR(20)  NOT NULL DEFAULT 'ROLE_MEMBER',
  phone      VARCHAR(20),
  join_date  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  active     TINYINT(1)   NOT NULL DEFAULT 1
);

-- Step 3: Create books table
CREATE TABLE IF NOT EXISTS books (
  id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  title       VARCHAR(200) NOT NULL,
  author      VARCHAR(150) NOT NULL,
  genre       VARCHAR(50),
  year        INT,
  isbn        VARCHAR(30),
  stock       INT          NOT NULL DEFAULT 1,
  rating      INT          DEFAULT 5,
  description TEXT,
  cover_url   VARCHAR(500)
);

-- Step 4: Create borrow_records table (stores borrow date, due date, return date)
CREATE TABLE IF NOT EXISTS borrow_records (
  id          BIGINT    NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id     BIGINT    NOT NULL,
  book_id     BIGINT    NOT NULL,
  borrow_date DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,   -- exact borrow time
  due_date    DATETIME  NOT NULL,                              -- 14 days from borrow
  return_date DATETIME  NULL,                                  -- NULL = not returned yet
  returned    TINYINT(1) NOT NULL DEFAULT 0,
  fine_amount DECIMAL(10,2) DEFAULT 0.00,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);

-- Step 5: Create fines table
CREATE TABLE IF NOT EXISTS fines (
  id         BIGINT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
  borrow_id  BIGINT         NOT NULL,
  user_id    BIGINT         NOT NULL,
  amount     DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
  paid       TINYINT(1)     NOT NULL DEFAULT 0,
  created_at DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  paid_at    DATETIME       NULL,
  FOREIGN KEY (borrow_id) REFERENCES borrow_records(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id)   REFERENCES users(id)          ON DELETE CASCADE
);

-- ═══════════════════════════════════════════════════
--  NOTE: User passwords are inserted by Spring Boot on
--  first startup using BCrypt encoding (DataLoader.java)
--  Do NOT insert plain-text passwords here.
--  Just run the app — it will auto-create all 5 members + admin.
-- ═══════════════════════════════════════════════════

-- Optional: Verify tables created
SHOW TABLES;
SELECT 'Database setup complete! Now run the Spring Boot app to seed users and books.' AS STATUS;
