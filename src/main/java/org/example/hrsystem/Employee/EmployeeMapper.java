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
//        employee.setDepartment(employeeRequestDTO.getDepartment());
        employee.setGender(String.valueOf(employeeRequestDTO.getGender()));
        employee.setTeam(employeeRequestDTO.getTeam());
        employee.setGrossSalary(employeeRequestDTO.getGrossSalary());
        return employee;
    }
    public EmployeeResponseDTO toResponse(Employee employee){
        return EmployeeResponseDTO.builder().
                id(employee.getId()).
                name(employee.getName())
//                departmentName(employee.getDepartment().getName())
                .team(employee.getTeam())
                .manager(employee.getManager())
                .build();
    }
}
