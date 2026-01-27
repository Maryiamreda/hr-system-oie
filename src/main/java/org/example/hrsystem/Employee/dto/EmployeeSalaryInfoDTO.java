package org.example.hrsystem.Employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class EmployeeSalaryInfoDTO {
    private BigDecimal grossSalary;
    private BigDecimal netSalary;
}