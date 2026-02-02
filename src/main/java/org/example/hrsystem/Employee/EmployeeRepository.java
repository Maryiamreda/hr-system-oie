package org.example.hrsystem.Employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByName(String uuidName);

    List<Employee> findByDepartmentName(String uniqueDepartmentName);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Employee e SET e.manager = :newManager WHERE e.manager = :oldManager")
    void updateSubordinatesManager(@Param("oldManager") Employee oldManager,
                                   @Param("newManager") Employee newManager);

    List<Employee> findByManager(Employee managerId);
    Page<Employee> findByManager(Employee managerId , Pageable pageable);
    Page<Employee> findByTeamName(String teamName, Pageable pageable);
}
