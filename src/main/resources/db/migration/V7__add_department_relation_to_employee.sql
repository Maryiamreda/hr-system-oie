alter table employee add department_id BIGINT;
alter table employee add constraint fk_department
foreign key (department_id)  REFERENCES department(id);
--
-- ALTER TABLE Employee
--     ADD CONSTRAINT FK_Employee_Department
--         FOREIGN KEY (DepartmentID) REFERENCES Department(DepartmentID);