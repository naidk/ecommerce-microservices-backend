-- 1. First, create the default Amazon Vendor so we have a valid foreign key target
INSERT INTO vendor (id, company_name, tax_id, contact_email, approval_status, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000000', 
    'Amazon Official', 
    'TAX-AMZN-001', 
    'admin@amazon.com', 
    'APPROVED', 
    NOW(), 
    NOW()
) ON CONFLICT DO NOTHING;

-- 2. Add the column to the product table (nullable for now)
ALTER TABLE product ADD COLUMN vendor_id UUID;

-- 3. Update all existing rows to point to the new Amazon vendor
UPDATE product SET vendor_id = '00000000-0000-0000-0000-000000000000' WHERE vendor_id IS NULL;

-- 4. Now that there are no nulls, alter the column to be NOT NULL and add foreign key constraint
ALTER TABLE product ALTER COLUMN vendor_id SET NOT NULL;
ALTER TABLE product ADD CONSTRAINT fk_product_vendor FOREIGN KEY (vendor_id) REFERENCES vendor (id);
