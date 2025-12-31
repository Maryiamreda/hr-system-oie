package org.example.hrsystem.Employee;

import jakarta.persistence.*;
import lombok.Data;
import org.example.hrsystem.enums.Gender;

import java.time.LocalDate;


@Entity
@Data
@Table(name = "employee")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Gender gender;
    private LocalDate birthDate;
    private LocalDate graduationDate;
    private String department;
    private String team;
    private Float grossSalary;
}
