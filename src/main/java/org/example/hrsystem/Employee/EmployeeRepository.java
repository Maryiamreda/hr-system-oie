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
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByName(String uuidName);
    Optional<Employee> findByNationalId(String nationalId);
    List<Employee> findByDepartmentName(String uniqueDepartmentName);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Employee e SET e.manager = :newManager WHERE e.manager = :oldManager")
    void updateSubordinatesManager(@Param("oldManager") Employee oldManager,
                                   @Param("newManager") Employee newManager);

    List<Employee> findByManager(Employee managerId);

    Page<Employee> findByManager(Employee managerId, Pageable pageable);

    Page<Employee> findByTeamName(String teamName, Pageable pageable);

    @Query(value = """
            WITH RECURSIVE subordinates(id,
                            name,
                            gender,
                            first_name,
                            last_name,
                            national_id,
                            degree,
                            birth_date,
                            graduation_date,
                            hire_date,
                            gross_salary, manager_id, department_id, team_id) AS (
                SELECT id, name, gender, first_name, last_name,national_id, degree, birth_date, graduation_date,hire_date, gross_salary, manager_id, department_id, team_id
                FROM employee WHERE manager_id = :managerId
                UNION ALL
                SELECT e.id,
                       e.name,
                       e.first_name,
                       e.last_name,
                       e.national_id,   e.degree, e.gender, e.birth_date, e.graduation_date, e.hire_date, e.gross_salary, e.manager_id, e.department_id, e.team_id
                FROM employee e
                JOIN subordinates s ON e.manager_id = s.id
            )
            SELECT * FROM subordinates
            """,
            nativeQuery = true)
    List<Employee> findRecursiveSubordinates(@Param("managerId") Long managerId, Pageable pageable);


    boolean existsByNationalId( String nationalId);
}
