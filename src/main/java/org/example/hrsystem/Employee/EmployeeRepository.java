package org.example.hrsystem.Employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee,Long> {
    Optional<Employee> findByName(String uuidName);

    Optional<Employee> findByDepartment(String uuidDepartmentName);
}
