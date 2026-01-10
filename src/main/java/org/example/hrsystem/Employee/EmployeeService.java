package org.example.hrsystem.Employee;

import org.example.hrsystem.Expertise.Expertise;
import org.example.hrsystem.Expertise.ExpertiseRepository;
import org.example.hrsystem.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final BigDecimal TAX_RATE = new BigDecimal("0.15");
    private final BigDecimal FIXED_DEDUCTION = new BigDecimal("500.00");

    public Employee addEmployee(EmployeeRequestDTO employeeRequestDTO) {
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
        BigDecimal netSalary = calculateNetSalary(employeeRequestDTO.getGrossSalary());
        employee.setNetSalary(netSalary);
        return employeeRepository.save(employee);
    }

    private BigDecimal calculateNetSalary(BigDecimal grossSalary) {
        //ross Salary - (Gross Salary * 0.15) - 500
        if (grossSalary == null) return BigDecimal.ZERO;
        BigDecimal taxAmount = grossSalary.multiply(TAX_RATE);
        BigDecimal totalDeductions = taxAmount.add(FIXED_DEDUCTION);
        return grossSalary.subtract(totalDeductions)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public Employee getEmployeeInfo(Long employeeId) {
        Optional<Employee> employee= employeeRepository.findById(employeeId);
        if (employee.isEmpty()) {
            throw new NotFoundException();
        }
       return employee.get();
    }

}
