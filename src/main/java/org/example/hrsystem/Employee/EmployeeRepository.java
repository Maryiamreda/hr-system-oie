package org.example.hrsystem.Employee;

import org.example.hrsystem.Department.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee,Long> {
    List<Employee> findByName(String uuidName);

    List<Employee> findByDepartment(Department department);

}
