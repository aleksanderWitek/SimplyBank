-- Schema for the simplybank_test MySQL database.
-- Mirrors production db/simply_bank_db_script.sql exactly, minus the CREATE DATABASE/USE and the DROP markers.
-- Loaded automatically by Spring Boot on every integration test run.

CREATE TABLE IF NOT EXISTS user_account (
    id INT NOT NULL AUTO_INCREMENT,
    login VARCHAR(20) NOT NULL,
    password VARCHAR(60) NOT NULL,
    role VARCHAR(50) NOT NULL,
    create_date DATETIME NOT NULL,
    modify_date DATETIME,
    delete_date DATETIME,
    CONSTRAINT pk_user_account_id PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS client (
    id INT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(30) NOT NULL,
    last_name VARCHAR(30) NOT NULL,
    city VARCHAR(30) NOT NULL,
    street VARCHAR(30) NOT NULL,
    house_number VARCHAR(10) NOT NULL,
    identification_number VARCHAR(15) NOT NULL,
    create_date DATETIME NOT NULL,
    modify_date DATETIME,
    delete_date DATETIME,
    CONSTRAINT pk_client_id PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS employee (
    id INT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(30) NOT NULL,
    last_name VARCHAR(30) NOT NULL,
    create_date DATETIME NOT NULL,
    modify_date DATETIME,
    delete_date DATETIME,
    CONSTRAINT pk_employee PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS user_account_client (
    user_account_client_id INT NOT NULL AUTO_INCREMENT,
    user_account_id INT NOT NULL UNIQUE,
    client_id INT NOT NULL UNIQUE,
    create_date DATETIME NOT NULL,
    delete_date DATETIME,
    CONSTRAINT pk_user_account_client PRIMARY KEY(user_account_client_id),
    CONSTRAINT fk_user_account_user_account_client FOREIGN KEY(user_account_id) REFERENCES user_account(id),
    CONSTRAINT fk_client_user_account_client FOREIGN KEY(client_id) REFERENCES client(id),
    CONSTRAINT uk_user_account_client UNIQUE(user_account_id, client_id)
);

CREATE TABLE IF NOT EXISTS user_account_employee (
    user_account_employee_id INT NOT NULL AUTO_INCREMENT,
    user_account_id INT NOT NULL UNIQUE,
    employee_id INT NOT NULL UNIQUE,
    create_date DATETIME NOT NULL,
    delete_date DATETIME,
    CONSTRAINT pk_user_account_employee PRIMARY KEY(user_account_employee_id),
    CONSTRAINT fk_user_account_user_account_employee FOREIGN KEY(user_account_id) REFERENCES user_account(id),
    CONSTRAINT fk_employee_user_account_employee FOREIGN KEY(employee_id) REFERENCES employee(id),
    CONSTRAINT uk_user_account_employee UNIQUE(user_account_id, employee_id)
);

CREATE TABLE IF NOT EXISTS bank_account (
    id INT NOT NULL AUTO_INCREMENT,
    number VARCHAR(15) NOT NULL UNIQUE,
    account_type VARCHAR(20) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    balance DECIMAL(14,2) NOT NULL,
    create_date DATETIME NOT NULL,
    modify_date DATETIME,
    delete_date DATETIME,
    CONSTRAINT pk_bank_account PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS bank_account_client (
    bank_account_client_id INT NOT NULL AUTO_INCREMENT,
    bank_account_id INT NOT NULL,
    client_id INT NOT NULL,
    create_date DATETIME NOT NULL,
    delete_date DATETIME,
    CONSTRAINT pk_bank_account_client PRIMARY KEY(bank_account_client_id),
    CONSTRAINT fk_bank_account_bank_account_client FOREIGN KEY(bank_account_id) REFERENCES bank_account(id),
    CONSTRAINT fk_client_bank_account_client FOREIGN KEY(client_id) REFERENCES client(id),
    CONSTRAINT uk_bank_account_client UNIQUE(bank_account_id, client_id)
);

CREATE TABLE IF NOT EXISTS transaction (
    id INT NOT NULL AUTO_INCREMENT,
    transaction_type ENUM('TRANSFER', 'DEPOSIT', 'WITHDRAWAL') NOT NULL,
    currency VARCHAR(3) NOT NULL,
    amount DECIMAL(14,2) NOT NULL,
    bank_account_id_from INT,
    bank_account_id_to INT,
    description VARCHAR(255),
    create_date DATETIME NOT NULL,
    CONSTRAINT pk_transaction PRIMARY KEY(id),
    CONSTRAINT fk_transaction_bank_account_from FOREIGN KEY(bank_account_id_from) REFERENCES bank_account(id),
    CONSTRAINT fk_transaction_bank_account_to FOREIGN KEY(bank_account_id_to) REFERENCES bank_account(id)
);
