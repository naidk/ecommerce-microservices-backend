ALTER TABLE customer 
ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT 'default_password',
ADD COLUMN role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER';

-- Remove default constraint after populating existing rows (optional, but good practice)
ALTER TABLE customer ALTER COLUMN password DROP DEFAULT;
ALTER TABLE customer ALTER COLUMN role DROP DEFAULT;
