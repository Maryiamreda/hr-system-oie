
alter table employee ADD COLUMN national_id VARCHAR(20);
alter table employee ADD COLUMN degree VARCHAR(50);
alter table employee ADD COLUMN hire_date DATE DEFAULT '9999-01-01' ;

UPDATE employee
SET
    national_id = '<not-provided>' || id
WHERE national_id IS NULL;

alter table employee
    add CONSTRAINT uk_employee_national_id UNIQUE (national_id);

alter table employee alter column national_id VARCHAR(20) NOT NULL;
alter table employee alter column hire_date DATE NOT NULL;