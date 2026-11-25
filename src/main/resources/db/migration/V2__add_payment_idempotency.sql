ALTER TABLE payments
ADD COLUMN idempotency_key VARCHAR(255);

ALTER TABLE payments
ADD COLUMN request_hash VARCHAR(64);

ALTER TABLE payments
ADD CONSTRAINT uq_payments_idempotency_key
UNIQUE (idempotency_key);