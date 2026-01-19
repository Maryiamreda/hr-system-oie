alter table employee add department_id BIGINT;
alter table employee add constraint fk_department
    foreign key (department_id)  REFERENCES department(id);