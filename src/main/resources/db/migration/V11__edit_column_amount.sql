ALTER TABLE transactions ADD amount FLOAT;

ALTER TABLE transactions ALTER COLUMN  amount SET NOT NULL;

ALTER TABLE transactions DROP COLUMN ammount;