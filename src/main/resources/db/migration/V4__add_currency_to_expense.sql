ALTER TABLE expense
    ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'PLN';

ALTER TABLE expense
    ADD CONSTRAINT chk_expense_currency
        CHECK (currency IN ('PLN', 'EUR', 'USD', 'GBP', 'UAH', 'CZK'));