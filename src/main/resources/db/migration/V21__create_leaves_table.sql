create table if not exists leave_record(
id BIGINT AUTO_INCREMENT PRIMARY KEY,
employee_id BIGINT NOT NULL,
days INT NOT NULL ,
total_leave_days INT NOT NULL DEFAULT 0,
start_date DATE NOT NULL,
end_date DATE NOT NULL,
notes VARCHAR(250),
CONSTRAINT FK_EmployeeLeave FOREIGN KEY ( employee_id ) REFERENCES employee(id)
)
