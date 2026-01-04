package org.example.hrsystem.Employee;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.example.hrsystem.enums.Gender;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class EmployeeRequestDTO {
    @NotNull(message = "Employee name cannot be empty")
    private String name;
    private Gender gender;
    private LocalDate birthDate;
    private LocalDate graduationDate;
    @NotNull(message = "Department cannot be empty")
    private String department;
    private String team;
    @Positive(message = "Salary must be positive")
    private Float grossSalary;
    private List<String> expertise;
}
