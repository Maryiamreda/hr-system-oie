package org.example.hrsystem.Employee;

import jakarta.persistence.*;
import lombok.*;
import org.example.hrsystem.Department.Department;
import org.example.hrsystem.Expertise.Expertise;
import org.example.hrsystem.Team.Team;
import org.example.hrsystem.enums.Degree;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Entity
@Data
@Builder
@Table(name = "employee")
@ToString(exclude = {"manager", "expertises", "team"})
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    private String gender;
    private LocalDate birthDate;
    private LocalDate graduationDate;
    @Column(name="years_experience")
    private LocalDate yearsOfExperience;
    @Column(name = "national_id", unique = true)
    private String nationalId;
    @Column(name = "hire_date")
    private LocalDate hireDate;
    @Enumerated(EnumType.STRING)
    private Degree degree;
    @Column(name = "gross_salary", precision = 12, scale = 2)
    private BigDecimal grossSalary;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "employee_expertise", //name the join table
            joinColumns = @JoinColumn(name = "employee_id"),
            inverseJoinColumns = @JoinColumn(name = "expertise_id")
    )
    private List<Expertise> expertises;
    @ManyToOne
    @JoinColumn(referencedColumnName = "id")
    private Employee manager;
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;
}
