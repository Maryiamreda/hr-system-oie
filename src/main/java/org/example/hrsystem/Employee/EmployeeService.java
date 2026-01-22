package org.example.hrsystem.Employee;

import org.example.hrsystem.Department.Department;
import org.example.hrsystem.Department.DepartmentRepository;
import org.example.hrsystem.Employee.dto.EmployeeRequestDTO;
import org.example.hrsystem.Employee.dto.EmployeeResponseDTO;
import org.example.hrsystem.Expertise.Expertise;
import org.example.hrsystem.Expertise.ExpertiseRepository;
import org.example.hrsystem.Team.Team;
import org.example.hrsystem.Team.TeamRepository;
import org.example.hrsystem.exception.BadRequestException;
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
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private TeamRepository teamRepository;
    private final BigDecimal TAX_RATE = new BigDecimal("0.15");
    private final BigDecimal FIXED_DEDUCTION = new BigDecimal("500.00");

    public EmployeeResponseDTO addEmployee(EmployeeRequestDTO employeeRequestDTO) {
        Employee employee = employeeMapper.toEntity(employeeRequestDTO);

        validateAndUpdateExpertise(employee, employeeRequestDTO);
        validateAndUpdateManager(employee, employeeRequestDTO);
        validateAndUpdateDepartment(employee, employeeRequestDTO);
        validateAndUpdateTeam(employee, employeeRequestDTO);
        BigDecimal netSalary = calculateNetSalary(employeeRequestDTO.getGrossSalary());
        employee.setNetSalary(netSalary);
        Employee newEmployee = employeeRepository.save(employee);
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
        Optional<Employee> employee = employeeRepository.findById(employeeId);
        if (employee.isEmpty()) {
            throw new BadRequestException("Employee Doesn't Exist");
        }

        return employeeMapper.toResponse(employee.get());
    }

    public void updateEmployee(Long employeeId, EmployeeRequestDTO employeeRequestDTO) {
        Optional<Employee> employee = employeeRepository.findById(employeeId);
        if (employee.isEmpty()) {
            throw new BadRequestException("Employee Doesn't Exist");
        }
        Employee updatedEmployee = employee.get();
        if (employeeRequestDTO.getName() != null) {updatedEmployee.setName(employeeRequestDTO.getName());}
        if (employeeRequestDTO.getGrossSalary() != null) {
            updatedEmployee.setGrossSalary(employeeRequestDTO.getGrossSalary());
            updatedEmployee.setNetSalary(calculateNetSalary(employeeRequestDTO.getGrossSalary()));
        }
        //UNREQUIRED ATTRIBUTES
        if (employeeRequestDTO.getGender() != null) {updatedEmployee.setGender(String.valueOf(employeeRequestDTO.getGender()));}
        if (employeeRequestDTO.getBirthDate() != null) {updatedEmployee.setBirthDate(employeeRequestDTO.getBirthDate());}
        if (employeeRequestDTO.getGraduationDate() != null) {updatedEmployee.setGraduationDate(employeeRequestDTO.getGraduationDate());}


        validateAndUpdateExpertise(updatedEmployee, employeeRequestDTO);

        validateAndUpdateManager(updatedEmployee, employeeRequestDTO);
        validateAndUpdateDepartment(updatedEmployee, employeeRequestDTO);

        validateAndUpdateTeam(updatedEmployee, employeeRequestDTO);
        Employee savedUpdatedEmployee = employeeRepository.save(updatedEmployee);
//        return employeeMapper.toResponse(savedUpdatedEmployee);
    }


    //private helper methods
    //METHOD TO CHECK THE EXISTENCE OF AN ELEMENT AND SETTING THE DATA IF ITS VALID
    private void validateAndUpdateExpertise(Employee employee, EmployeeRequestDTO dto) {
        if (dto.getExpertise() != null && !dto.getExpertise().isEmpty()) {
            List<Expertise> expertises = expertiseRepository.findAllByNameIn(dto.getExpertise());
            if (expertises.size() != dto.getExpertise().size()) {
                throw new BadRequestException("Expert Doesn't Exist");
            }
            if(employee.getExpertises()!=null) {
                expertises.addAll(employee.getExpertises());
            }
            employee.setExpertises(expertises);
        }
    }

    private void validateAndUpdateManager(Employee employee, EmployeeRequestDTO dto) {
        if (dto.getManagerId() != null) {
            Optional<Employee> employeeManager = employeeRepository.findById(dto.getManagerId());
            if (employeeManager.isEmpty()) {
                throw new BadRequestException("Manager Doesn't Exist");

            }

            employee.setManager(employeeManager.get());
        }
    }

    private void validateAndUpdateDepartment(Employee employee, EmployeeRequestDTO dto) {
        if (dto.getDepartmentName() != null ) {
            Optional<Department> employeeDepartment=departmentRepository.findByName(dto.getDepartmentName());
            if (employeeDepartment.isEmpty()) {
                throw new BadRequestException("Department Doesn't Exist");
            }
            else  employee.setDepartment(employeeDepartment.get());
        }
    }

    private void validateAndUpdateTeam(Employee employee, EmployeeRequestDTO dto) {
        if (dto.getTeamName() != null ) {

            Optional<Team> employeeTeam=teamRepository.findByName(dto.getTeamName());
            if (employeeTeam.isEmpty()) {
                throw new BadRequestException("Team Doesn't Exist");
            }
            else  employee.setTeam(employeeTeam.get());
        }
    }
}
