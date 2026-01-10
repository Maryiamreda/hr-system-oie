package org.example.hrsystem.Expertise;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @JsonBackReference
    private List<Employee> employeesId;
}
