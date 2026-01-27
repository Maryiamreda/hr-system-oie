package org.example.hrsystem.Employee;

import org.example.hrsystem.Department.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByName(String uuidName);

    List<Employee> findByDepartmentName(String uniqueDepartmentName);
    @Query(nativeQuery = true,value = "SELECT Count(*) FROM employee_expertise WHERE employee_id=:employeeId" )
    int countEmployeeExpertise(@Param("employeeId") Long employeeId);

    List<Employee> findByManager(Employee managerId);
}
