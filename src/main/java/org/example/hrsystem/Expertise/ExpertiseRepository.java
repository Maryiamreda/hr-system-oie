package org.example.hrsystem.Expertise;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpertiseRepository extends JpaRepository<Expertise,Long> {
    List<Expertise> findAllByNameIn(List<String> expertise);
}
