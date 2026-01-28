package org.example.hrsystem.Employee;

import jakarta.persistence.*;
import lombok.*;
import org.example.hrsystem.Department.Department;
import org.example.hrsystem.Expertise.Expertise;
import org.example.hrsystem.Team.Team;

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
    private String name;
    private String gender;
    private LocalDate birthDate;
    private LocalDate graduationDate;
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
