CREATE DATABASE simply_bank_db;

USE simply_bank_db;

CREATE TABLE user_account (
id INT NOT NULL AUTO_INCREMENT,
login VARCHAR(12) NOT NULL,
password VARCHAR(12) NOT NULL,
role VARCHAR(50) NOT NULL,
CONSTRAINT pk_user_account_id PRIMARY KEY(id));

CREATE TABLE client (
id INT NOT NULL AUTO_INCREMENT,
first_name VARCHAR(30) NOT NULL,
last_name VARCHAR(30) NOT NULL,
city VARCHAR(30) NOT NULL,
street VARCHAR(30) NOT NULL,
house_number VARCHAR(10) NOT NULL,
identification_number VARCHAR(15) NOT NULL,
CONSTRAINT pk_client_id PRIMARY KEY(id));

DROP TABLE client;

CREATE TABLE employee (
id INT NOT NULL AUTO_INCREMENT,
first_name VARCHAR(30) NOT NULL,
last_name VARCHAR(30) NOT NULL,
CONSTRAINT pk_employee PRIMARY KEY(id));

DROP TABLE employee;

CREATE TABLE user_account_client (
user_account_client_id INT NOT NULL AUTO_INCREMENT,
user_account_id INT NOT NULL UNIQUE,
client_id INT NOT NULL UNIQUE,
CONSTRAINT pk_user_account_client PRIMARY KEY(user_account_client_id),
CONSTRAINT fk_user_account_user_account_client FOREIGN KEY(user_account_id) REFERENCES user_account(id),
CONSTRAINT fk_client_user_account_client FOREIGN KEY(client_id) REFERENCES client(id));

DROP TABLE user_account_client;

CREATE TABLE user_account_employee (
user_account_employee_id INT NOT NULL AUTO_INCREMENT,
user_account_id INT NOT NULL UNIQUE,
employee_id INT NOT NULL UNIQUE,
CONSTRAINT pk_user_account_employee PRIMARY KEY(user_account_employee_id),
CONSTRAINT fk_user_account_user_account_employee FOREIGN KEY(user_account_id) REFERENCES user_account(id),
CONSTRAINT fk_employee_user_account_employee FOREIGN KEY(employee_id) REFERENCES employee(id));

DROP TABLE user_account_employee;

CREATE TABLE bank_account (
id INT NOT NULL AUTO_INCREMENT,
number VARCHAR(15) NOT NULL UNIQUE,
account_type VARCHAR(20) NOT NULL,
currency VARCHAR(3) NOT NULL,
balance DECIMAL(14,2) NOT NULL,
CONSTRAINT pk_bank_account PRIMARY KEY(id));

CREATE TABLE transaction (
id INT NOT NULL AUTO_INCREMENT,
currency VARCHAR(3) NOT NULL,
amount DECIMAL(14,2) NOT NULL,
date DATETIME NOT NULL,
CONSTRAINT pk_transaction PRIMARY KEY(id));

CREATE TABLE transaction_bank_account (
transaction_bank_account_id INT NOT NULL AUTO_INCREMENT,
transaction_id INT NOT NULL UNIQUE,
bank_account_id INT NOT NULL UNIQUE,
role VARCHAR(9) NOT NULL,
CONSTRAINT pk_transaction_bank_account PRIMARY KEY(transaction_bank_account_id),
CONSTRAINT fk_transaction_transaction_bank_account FOREIGN KEY(transaction_id) REFERENCES transaction(id),
CONSTRAINT fk_bank_account_transaction_bank_account FOREIGN KEY(bank_account_id) REFERENCES bank_account(id));