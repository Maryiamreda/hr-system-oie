update employee
set employee.department_id = (
    select id
    from department
    where employee.department = name
)