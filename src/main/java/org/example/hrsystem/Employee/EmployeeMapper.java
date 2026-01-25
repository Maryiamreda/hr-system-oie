package org.example.hrsystem.Employee;


import org.example.hrsystem.Employee.dto.EmployeeRequestDTO;
import org.example.hrsystem.Employee.dto.EmployeeResponseDTO;
import org.example.hrsystem.Expertise.Expertise;
import org.example.hrsystem.Expertise.ExpertiseRepository;
import org.example.hrsystem.enums.Gender;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {
    @Autowired
    private ExpertiseRepository expertiseRepository;

    public Employee toEntity(EmployeeRequestDTO employeeRequestDTO) {
        Employee employee = new Employee();
        employee.setName(employeeRequestDTO.getName());
        employee.setBirthDate(employeeRequestDTO.getBirthDate());
        employee.setGraduationDate(employeeRequestDTO.getGraduationDate());
        employee.setGender(String.valueOf(employeeRequestDTO.getGender()));
        employee.setGrossSalary(employeeRequestDTO.getGrossSalary());
        return employee;
    }

    public EmployeeResponseDTO toResponse(Employee employee) {
        EmployeeResponseDTO employeeResponseDTO = EmployeeResponseDTO.builder().
                id(employee.getId()).
                name(employee.getName())
                .departmentName(employee.getDepartment().getName())
                .manager(employee.getManager())
                .expertises(employee.getExpertises())
                .build();
        if (employee.getTeam() != null) employeeResponseDTO.setTeamName(employee.getTeam().getName());
        return employeeResponseDTO;
    }

    public EmployeeRequestDTO toDto(Employee employee) {
        EmployeeRequestDTO dto = new EmployeeRequestDTO();
        dto.setName(employee.getName());
        dto.setGender(Gender.valueOf(employee.getGender()));
        dto.setBirthDate(employee.getBirthDate());
        dto.setGraduationDate(employee.getGraduationDate());
        dto.setGrossSalary(employee.getGrossSalary());
        if (employee.getManager() != null) {
            dto.setManagerId(employee.getManager().getId());
        }
        if (employee.getDepartment() != null) {
            dto.setDepartmentName(employee.getDepartment().getName());
        }
        if (employee.getTeam() != null) {
            dto.setTeamName(employee.getTeam().getName());
        }
        if (employee.getExpertises() != null && !employee.getExpertises().isEmpty()) {
            dto.setExpertise(employee.getExpertises().stream()
                    .map(Expertise::getName)
                    .toList());
        }

        return dto;
    }


}
