package org.example.hrsystem.Employee.dto;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.example.hrsystem.Employee.Employee;

import java.util.List;
@Data
@Builder
@AllArgsConstructor
public class EmployeeResponseDTO {
    @Id
    @NotNull(message = "ID cannot be null")
    private Long id;
    private String name;
    @NotNull(message = "Department name be empty")
    private String departmentName;
    private String team;
    private List<Long> expertise;
    private Employee manager;
}
