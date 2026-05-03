-- One-time setup for SimplyBank integration tests.
-- Run against local MySQL as root BEFORE executing any @SpringBootTest class.
-- Usage: mysql -u root -p < src/test/resources/create-test-db.sql

CREATE DATABASE IF NOT EXISTS simplybank_test;
CREATE USER IF NOT EXISTS 'simplybank_test'@'localhost' IDENTIFIED BY 'test_password';
GRANT ALL PRIVILEGES ON simplybank_test.* TO 'simplybank_test'@'localhost';
FLUSH PRIVILEGES;
