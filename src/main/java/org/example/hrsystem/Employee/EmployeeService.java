package org.example.hrsystem.Employee;

import org.example.hrsystem.Department.Department;
import org.example.hrsystem.Department.DepartmentRepository;
import org.example.hrsystem.Employee.dto.EmployeeRequestDTO;
import org.example.hrsystem.Employee.dto.EmployeeResponseDTO;
import org.example.hrsystem.Expertise.Expertise;
import org.example.hrsystem.Expertise.ExpertiseRepository;
import org.example.hrsystem.Team.Team;
import org.example.hrsystem.Team.TeamRepository;
import org.example.hrsystem.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.jpa.repository.JpaRepository;
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
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private TeamRepository teamRepository;
    private final BigDecimal TAX_RATE = new BigDecimal("0.15");
    private final BigDecimal FIXED_DEDUCTION = new BigDecimal("500.00");

    public EmployeeResponseDTO addEmployee(EmployeeRequestDTO employeeRequestDTO) {
        Employee employee = employeeMapper.toEntity(employeeRequestDTO);
        if (employeeRequestDTO.getExpertise() != null && !employeeRequestDTO.getExpertise().isEmpty()) {
            List<Expertise> expertises = expertiseRepository.findAllByNameIn(employeeRequestDTO.getExpertise());
            if (expertises.size() != employeeRequestDTO.getExpertise().size()) {
                throw new NotFoundException("Expert Doesn't Exist");
            }
            employee.setExpertises(expertises);
        }
        if (employeeRequestDTO.getManagerId() != null) {
            Optional<Employee> employeeManager = employeeRepository.findById(employeeRequestDTO.getManagerId());
            if (employeeManager.isEmpty()) {
                throw new NotFoundException("Manager Doesn't Exist");
            }
            employee.setManager(employeeManager.get());
        }
        if (employeeRequestDTO.getDepartmentName() != null ) {
            Optional<Department> employeeDepartment=departmentRepository.findByName(employeeRequestDTO.getDepartmentName());
            if (employeeDepartment.isEmpty()) {
                throw new NotFoundException("Department Doesn't Exist");
            }
            else  employee.setDepartment(employeeDepartment.get());
        }
        if (employeeRequestDTO.getTeamName() != null ) {
            Optional<Team> employeeTeam=teamRepository.findByName(employeeRequestDTO.getTeamName());
            if (employeeTeam.isEmpty()) {
                throw new NotFoundException("Team Doesn't Exist");
            }
            else  employee.setTeam(employeeTeam.get());
        }

        BigDecimal netSalary = calculateNetSalary(employeeRequestDTO.getGrossSalary());
        employee.setNetSalary(netSalary);
        System.out.println("imma here");
        Employee newEmployee =employeeRepository.save(employee);
        return employeeMapper.toResponse(newEmployee);
    }

    private BigDecimal calculateNetSalary(BigDecimal grossSalary) {
        //ross Salary - (Gross Salary * 0.15) - 500
        if (grossSalary == null) return BigDecimal.ZERO;
        BigDecimal taxAmount = grossSalary.multiply(TAX_RATE);
        BigDecimal totalDeductions = taxAmount.add(FIXED_DEDUCTION);
        return grossSalary.subtract(totalDeductions)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public EmployeeResponseDTO getEmployeeInfo(Long employeeId) {
        Optional<Employee> employee= employeeRepository.findById(employeeId);
        if (employee.isEmpty()) {
            throw new NotFoundException("Employee Doesn't Exist");
        }

       return employeeMapper.toResponse(employee.get());
    }

}
