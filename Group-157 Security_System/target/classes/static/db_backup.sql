-- Fake Database Backup
-- Generated for honeypot simulation

CREATE DATABASE company_db;
USE company_db;

-- Users Table
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50),
    password VARCHAR(100),
    role VARCHAR(20)
);

INSERT INTO users (username, password, role) VALUES
('admin', 'Admin@123', 'ADMIN'),
('hr_user', 'hr@portal1', 'HR'),
('finance', 'fin@secure', 'FINANCE'),
('dev_user', 'devpass2024', 'DEVELOPER'),
('guest', 'guest123', 'GUEST');

-- Employees Table
CREATE TABLE employees (
    emp_id INT PRIMARY KEY,
    name VARCHAR(100),
    department VARCHAR(50),
    salary INT
);

INSERT INTO employees VALUES
(101, 'Amit Sharma', 'Finance', 60000),
(102, 'Neha Verma', 'HR', 75000),
(103, 'Rohit Singh', 'IT', 85000),
(104, 'Priya Nair', 'Marketing', 55000),
(105, 'Karan Patel', 'Operations', 50000);

-- Transactions Table
CREATE TABLE transactions (
    txn_id INT PRIMARY KEY AUTO_INCREMENT,
    emp_id INT,
    amount INT,
    txn_date DATE
);

INSERT INTO transactions (emp_id, amount, txn_date) VALUES
(101, 5000, '2025-03-01'),
(102, 7000, '2025-03-02'),
(103, 8000, '2025-03-03'),
(104, 4000, '2025-03-04'),
(105, 3000, '2025-03-05');

-- End of Backup
