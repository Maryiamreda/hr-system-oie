create table if not exists salary_record (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
employee_id BIGINT NOT NULL,
amount DECIMAL(10,2) NOT NULL ,
salary_month  TINYINT NOT NULL,
salary_year   SMALLINT NOT NULL,
type VARCHAR(50) NOT NULL,
CONSTRAINT FK_Employee_Salary_Record FOREIGN KEY ( employee_id ) REFERENCES employee(id)
);