package org.example.hrsystem.Employee;
import lombok.Data;
import org.example.hrsystem.enums.Gender;
import java.time.LocalDate;


@Data
public class EmployeeRequestDTO {
    private String name;
    private Gender gender;
    private LocalDate birthDate;
    private LocalDate graduationDate;
    private String department;
    private String team;
    private Float grossSalary;
}
