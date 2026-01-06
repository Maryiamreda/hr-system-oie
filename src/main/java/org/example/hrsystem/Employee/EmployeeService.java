package org.example.hrsystem.Employee;

import org.example.hrsystem.Expertise.Expertise;
import org.example.hrsystem.Expertise.ExpertiseRepository;
import org.example.hrsystem.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private ExpertiseRepository expertiseRepository;

    public Employee addEmployee(EmployeeRequestDTO employeeRequestDTO) {
        //Net Salary = Gross Salary - (Gross Salary * 0.15) - 500
        Employee employee = employeeMapper.toEntity(employeeRequestDTO);
        if (employeeRequestDTO.getExpertise() != null && !employeeRequestDTO.getExpertise().isEmpty()) {
            List<Expertise> expertises = expertiseRepository.findAllById(employeeRequestDTO.getExpertise());
            if (expertises.size() != employeeRequestDTO.getExpertise().size()) {
                throw new NotFoundException();
            }
            employee.setExpertisesId(expertises);
        }
        if (employeeRequestDTO.getManagerId() != null) {
            Optional<Employee> employeeManager = employeeRepository.findById(employeeRequestDTO.getManagerId());
            if (employeeManager.isEmpty()) {
                throw new NotFoundException();
            }
            employee.setManager(employeeManager.get());
        }
        float netSalary = (float) (employeeRequestDTO.getGrossSalary() - (employeeRequestDTO.getGrossSalary() * 0.15) - 500);
        employee.setNetSalary(netSalary);
        return employeeRepository.save(employee);
    }
}
