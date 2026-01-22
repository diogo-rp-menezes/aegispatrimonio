-- Add status column to departamentos table
ALTER TABLE departamentos ADD COLUMN status VARCHAR(50);
UPDATE departamentos SET status = 'ATIVO' WHERE status IS NULL;
ALTER TABLE departamentos MODIFY COLUMN status VARCHAR(50) NOT NULL;
