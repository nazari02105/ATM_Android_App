-- Database for ATM_Android_App
-- The app connects to the database "atm" and uses two tables: card and loan.

CREATE DATABASE IF NOT EXISTS atm CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE atm;

-- One row per ATM card.
-- qr_value is the text stored in the card's QR code (the app uses the person's name).
CREATE TABLE IF NOT EXISTS card (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    qr_value VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    balance  INT NOT NULL DEFAULT 0
);

-- One row per loan. qr_value links the loan to a card.
CREATE TABLE IF NOT EXISTS loan (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    loan_amount       INT NOT NULL,
    repayment_period  INT NOT NULL,
    installments_paid INT NOT NULL DEFAULT 0,
    qr_value          VARCHAR(255) NOT NULL
);
