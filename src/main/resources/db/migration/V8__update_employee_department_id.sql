UPDATE employee
INNER JOIN department ON employee.department = department.name
SET employee.department_id = department.id;