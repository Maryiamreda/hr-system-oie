package org.example.hrsystem.Employee;

import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {
    public Employee toEntity(EmployeeRequestDTO employeeRequestDTO){
        Employee employee=new Employee();
        employee.setName(employeeRequestDTO.getName());
        employee.setBirthDate(employeeRequestDTO.getBirthDate());
        employee.setGraduationDate(employeeRequestDTO.getGraduationDate());
        employee.setDepartment(employeeRequestDTO.getDepartment());
        employee.setGender(employeeRequestDTO.getGender());
        employee.setTeam(employeeRequestDTO.getTeam());
        employee.setGrossSalary(employeeRequestDTO.getGrossSalary());
        return employee;
    }
}
