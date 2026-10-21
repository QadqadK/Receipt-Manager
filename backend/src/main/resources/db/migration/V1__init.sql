CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE receipts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    store_name VARCHAR(200) NOT NULL,
    receipt_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_receipts_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_receipts_user_date ON receipts (user_id, receipt_date);

CREATE TABLE receipt_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    receipt_id BIGINT NOT NULL,
    item_name VARCHAR(200) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    CONSTRAINT fk_items_receipt FOREIGN KEY (receipt_id) REFERENCES receipts (id) ON DELETE CASCADE
);
