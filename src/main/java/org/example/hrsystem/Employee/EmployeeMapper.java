package org.example.hrsystem.Employee;


import org.example.hrsystem.Employee.dto.EmployeeRequestDTO;
import org.example.hrsystem.Employee.dto.EmployeeResponseDTO;
import org.example.hrsystem.Expertise.ExpertiseRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {
@Autowired
private ExpertiseRepository expertiseRepository;
    public Employee toEntity(EmployeeRequestDTO employeeRequestDTO){
        Employee employee=new Employee();
        employee.setName(employeeRequestDTO.getName());
        employee.setBirthDate(employeeRequestDTO.getBirthDate());
        employee.setGraduationDate(employeeRequestDTO.getGraduationDate());
        employee.setGender(String.valueOf(employeeRequestDTO.getGender()));
        employee.setGrossSalary(employeeRequestDTO.getGrossSalary());
        return employee;
    }
    public EmployeeResponseDTO toResponse(Employee employee){
        EmployeeResponseDTO employeeResponseDTO=EmployeeResponseDTO.builder().
                id(employee.getId()).
                name(employee.getName())
                .departmentName(employee.getDepartment().getName())
                .manager(employee.getManager())
                .expertises(employee.getExpertises())
                .build();
        if (employee.getTeam()!=null) employeeResponseDTO.setTeamName(employee.getTeam().getName());
        return employeeResponseDTO;
    }
}
