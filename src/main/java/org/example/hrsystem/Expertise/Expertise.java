package org.example.hrsystem.Expertise;

import jakarta.persistence.*;
import lombok.Data;
import org.example.hrsystem.Employee.Employee;

import java.util.List;

@Entity
@Data
@Table(name = "expertise")
public class Expertise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @ManyToMany(mappedBy = "expertisesId")
    private List<Employee> employeesId;
}
