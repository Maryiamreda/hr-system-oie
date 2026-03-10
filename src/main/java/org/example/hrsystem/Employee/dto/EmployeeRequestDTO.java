package org.example.hrsystem.Employee.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.example.hrsystem.enums.Degree;
import org.example.hrsystem.enums.Gender;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.example.hrsystem.utilities.EmployeeMessageConstants.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeRequestDTO {
    @Size(min = 2, message = EMPLOYEE_NAME_MIN_LENGTH )
    @NotNull(message = ERROR_EMPLOYEE_FIRST_NAME_EMPTY)
    private String firstName;
    @NotNull(message = ERROR_EMPLOYEE_LAST_NAME_EMPTY)
    private String lastName;
    @NotNull(message = ERROR_EMPLOYEE_NATIONAL_ID_EMPTY)
    private String nationalId;
    private Degree degree;
    private Gender gender;
    private LocalDate birthDate;
    private LocalDate graduationDate;
    @NotNull(message = ERROR_DEPARTMENT_NAME_EMPTY)
    private String departmentName;
    private String teamName;
    @NotNull(message = ERROR_SALARY_EMPTY)
    @Positive(message = ERROR_SALARY_POSITIVE)
    private BigDecimal grossSalary;
    private List<String> expertise;
    @NotNull(message = ERROR_MANAGER_NAME_EMPTY)
    private Long managerId;
}
