alter table employee ADD COLUMN first_name VARCHAR(255);
alter table employee ADD COLUMN last_name VARCHAR(255);

update employee
set
    first_name = SUBSTRING(name, 1, LOCATE(' ', name) - 1),
    last_name = SUBSTRING(name, LOCATE(' ', name) + 1)
where LOCATE(' ', name) > 0;

update employee
set
    first_name = name,
    last_name = ''
where LOCATE(' ', name) = 0;

alter table employee MODIFY COLUMN first_name VARCHAR(255) NOT NULL;
alter table employee MODIFY COLUMN last_name VARCHAR(255) NOT NULL;
alter table employee drop if exists  name ;
