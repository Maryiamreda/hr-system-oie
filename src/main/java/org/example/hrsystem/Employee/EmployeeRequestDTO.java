package org.example.hrsystem.Employee;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.example.hrsystem.Expertise.Expertise;
import org.example.hrsystem.enums.Gender;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class EmployeeRequestDTO {
    @NotNull(message = "Employee name cannot be empty")
    @Size(min = 2, message = "Employee name cannot be less letters than 3 ")
    private String name;
    private Gender gender;
    private LocalDate birthDate;
    private LocalDate graduationDate;
    @NotNull(message = "Department cannot be empty")
    private String department;
    private String team;
    @NotNull(message = "Salary name cannot be empty")
    @Positive(message = "Salary must be positive")
    private Float grossSalary;
    private List<Long> expertise;
    private Long managerId;
}
