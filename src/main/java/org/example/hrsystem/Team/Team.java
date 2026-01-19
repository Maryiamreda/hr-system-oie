package org.example.hrsystem.Team;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "team")
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", nullable = false, unique = true)
    private String name;
}