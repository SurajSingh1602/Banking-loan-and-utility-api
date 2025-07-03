-- DDL for roles table (if you want to use a separate roles table)
-- CREATE TABLE IF NOT EXISTS roles (
--    id BIGINT AUTO_INCREMENT PRIMARY KEY,
--    name VARCHAR(50) NOT NULL UNIQUE
-- );

-- DDL for users table (adjust as per your User entity's fields)
CREATE TABLE IF NOT EXISTS users (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   username VARCHAR(255) NOT NULL UNIQUE,
   password VARCHAR(255) NOT NULL,
   roles VARCHAR(255) NOT NULL
);

-- DDL for utility_bills table
CREATE TABLE IF NOT EXISTS utility_bills (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   biller_name VARCHAR(255) NOT NULL,
   account_number VARCHAR(255) NOT NULL,
   amount DOUBLE NOT NULL,
   due_date DATE NOT NULL,
   status VARCHAR(50),
   user_id BIGINT NOT NULL,
   FOREIGN KEY (user_id) REFERENCES users(id)
);

-- DDL for bank_accounts table
CREATE TABLE IF NOT EXISTS bank_accounts (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   account_number VARCHAR(255) NOT NULL UNIQUE,
   account_type VARCHAR(50) NOT NULL,
   balance DOUBLE NOT NULL,
   user_id BIGINT NOT NULL,
   FOREIGN KEY (user_id) REFERENCES users(id)
);

-- DDL for transactions table
CREATE TABLE IF NOT EXISTS transactions (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   account_id BIGINT NOT NULL,
   type VARCHAR(50) NOT NULL, -- e.g., DEPOSIT, WITHDRAWAL, TRANSFER
   amount DOUBLE NOT NULL,
   transaction_date DATETIME NOT NULL,
   description VARCHAR(255),
   FOREIGN KEY (account_id) REFERENCES bank_accounts(id)
);

-- DDL for loans table
CREATE TABLE IF NOT EXISTS loans (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   user_id BIGINT NOT NULL,
   loan_amount DOUBLE NOT NULL,
   interest_rate DOUBLE NOT NULL,
   term_months INT NOT NULL,
   start_date DATE NOT NULL,
   due_date DATE NOT NULL,
   status VARCHAR(50) NOT NULL, -- e.g., PENDING, APPROVED, REJECTED, PAID, OVERDUE
   FOREIGN KEY (user_id) REFERENCES users(id)
);


-- Insert initial users for testing (passwords are 'password' encoded with BCrypt)
-- Generated with BCryptPasswordEncoder().encode("password")
INSERT IGNORE INTO users (id, username, password, roles) VALUES (1, 'user1', '$2a$10$A.4o/1M1Q1R1S1T1U1V1W1X1Y1Z1A1B1C1D1E1F1G1H1I1J1K1L1M1N1O1P1Q1R1S1', 'ROLE_USER');
INSERT IGNORE INTO users (id, username, password, roles) VALUES (2, 'admin1', '$2a$10$A.4o/1M1Q1R1S1T1U1V1W1X1Y1Z1A1B1C1D1E1F1G1H1I1J1K1L1M1N1O1P1Q1R1S1', 'ROLE_ADMIN,ROLE_USER');

-- Insert initial utility bills (example data)
INSERT IGNORE INTO utility_bills (id, biller_name, account_number, amount, due_date, status, user_id) VALUES (1, 'Electricity Co.', 'UB-123456789', 75.50, '2025-07-15', 'UNPAID', 1);
INSERT IGNORE INTO utility_bills (id, biller_name, account_number, amount, due_date, status, user_id) VALUES (2, 'Water Board', 'UB-987654321', 30.25, '2025-06-01', 'OVERDUE', 1);

-- Insert initial bank accounts (example data)
INSERT IGNORE INTO bank_accounts (id, account_number, account_type, balance, user_id) VALUES (1, 'ACC-001-USER1', 'SAVINGS', 1500.00, 1);
INSERT IGNORE INTO bank_accounts (id, account_number, account_type, balance, user_id) VALUES (2, 'ACC-002-USER1', 'CHECKING', 500.00, 1);
INSERT IGNORE INTO bank_accounts (id, account_number, account_type, balance, user_id) VALUES (3, 'ACC-001-ADMIN1', 'SAVINGS', 5000.00, 2);

-- Insert initial transactions (example data)
INSERT IGNORE INTO transactions (id, account_id, type, amount, transaction_date, description) VALUES (1, 1, 'DEPOSIT', 1000.00, '2025-06-10 10:00:00', 'Initial Deposit');
INSERT IGNORE INTO transactions (id, account_id, type, amount, transaction_date, description) VALUES (2, 1, 'WITHDRAWAL', 200.00, '2025-06-12 14:30:00', 'ATM Withdrawal');

-- Insert initial loans (example data)
INSERT IGNORE INTO loans (id, user_id, loan_amount, interest_rate, term_months, start_date, due_date, status) VALUES (1, 1, 10000.00, 0.05, 12, '2025-06-01', '2026-06-01', 'APPROVED');
INSERT IGNORE INTO loans (id, user_id, loan_amount, interest_rate, term_months, start_date, due_date, status) VALUES (2, 2, 50000.00, 0.03, 60, '2025-05-15', '2030-05-15', 'PENDING');