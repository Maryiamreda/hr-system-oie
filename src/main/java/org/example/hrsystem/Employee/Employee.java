package org.example.hrsystem.Employee;

import jakarta.persistence.*;
import lombok.Data;
import org.example.hrsystem.Expertise.Expertise;
import org.example.hrsystem.enums.Gender;

import java.time.LocalDate;
import java.util.List;


@Entity
@Data
@Table(name = "employee")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String gender;
    private LocalDate birthDate;
    private LocalDate graduationDate;
    private String department;
    private String team;
    private Float grossSalary;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "employee_expertise", //name the join table
            joinColumns = @JoinColumn(name = "employee_id"),
            inverseJoinColumns = @JoinColumn(name = "expertise_id")
    )
    private List<Expertise> expertisesId;

}
