package org.example.hrsystem.Employee;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gravity9.jsonpatch.mergepatch.JsonMergePatch;
import org.example.hrsystem.Department.Department;
import org.example.hrsystem.Department.DepartmentRepository;
import org.example.hrsystem.Employee.dto.EmployeeRequestDTO;
import org.example.hrsystem.Employee.dto.EmployeeResponseDTO;
import org.example.hrsystem.Expertise.Expertise;
import org.example.hrsystem.Expertise.ExpertiseRepository;
import org.example.hrsystem.Team.Team;
import org.example.hrsystem.Team.TeamRepository;
import org.example.hrsystem.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
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
    @Autowired
    private ObjectMapper objectMapper;
    private final BigDecimal TAX_RATE = new BigDecimal("0.15");
    private final BigDecimal FIXED_DEDUCTION = new BigDecimal("500.00");

    public EmployeeResponseDTO addEmployee(EmployeeRequestDTO employeeRequestDTO) {
        Employee employee = employeeMapper.toEntity(employeeRequestDTO);


        if (employeeRequestDTO.getManagerId() != null) {
            employee.setManager(validateManager(employeeRequestDTO.getManagerId()));
        }
        if (employeeRequestDTO.getDepartmentName() != null) {
            employee.setDepartment(validateDepartment(employeeRequestDTO.getDepartmentName()));
        }

        if (employeeRequestDTO.getTeamName() != null) {
            employee.setTeam(validateTeam(employeeRequestDTO.getTeamName()));
        }
        if (employeeRequestDTO.getExpertise() != null) {
            employee.setExpertises(validateExpertise(employeeRequestDTO.getExpertise()));

        }

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

    public EmployeeResponseDTO getEmployeeResponseDTO(Long employeeId) {
        Optional<Employee> employee = employeeRepository.findById(employeeId);
        if (employee.isEmpty()) {
            throw new BadRequestException("Employee Doesn't Exist");
        }

        return employeeMapper.toResponse(employee.get());
    }


    public void updateEmployee(Long employeeId, JsonMergePatch patch) throws Exception {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BadRequestException("Employee Doesn't Exist"));

        EmployeeRequestDTO currentDto = employeeMapper.toDto(employee);
        JsonNode dtoNode = objectMapper.convertValue(currentDto, JsonNode.class);
        JsonNode patchedNode = patch.apply(dtoNode);
        EmployeeRequestDTO patchedDto = objectMapper.treeToValue(patchedNode, EmployeeRequestDTO.class);
        if (patchedDto.getName() != null && patchedDto.getName().length() > 2) {
            employee.setName(patchedDto.getName());
        }
        employee.setGender(String.valueOf(patchedDto.getGender()));
        employee.setBirthDate(patchedDto.getBirthDate());
        employee.setGraduationDate(patchedDto.getGraduationDate());
        if (patchedDto.getGrossSalary() != null && !Objects.equals(patchedDto.getGrossSalary(), employee.getGrossSalary())) {
            employee.setGrossSalary(patchedDto.getGrossSalary());
            employee.setNetSalary(calculateNetSalary(patchedDto.getGrossSalary()));
        }
        if (patchedDto.getManagerId() != null &&
                !Objects.equals(patchedDto.getManagerId(), employee.getManager().getId())) {
            employee.setManager(validateManager(patchedDto.getManagerId()));
        }

        if (patchedDto.getDepartmentName() != null &&
                !Objects.equals(patchedDto.getDepartmentName(), employee.getDepartment().getName())) {
            employee.setDepartment(validateDepartment(patchedDto.getDepartmentName()));
        }

        if (patchedDto.getTeamName() != null &&
                !Objects.equals(patchedDto.getTeamName(), employee.getTeam().getName())) {
            employee.setTeam(validateTeam(patchedDto.getTeamName()));
        }

        if (patchedDto.getExpertise() != null && !patchedDto.getExpertise().isEmpty()) {
            List<Expertise> expertises = validateExpertise(patchedDto.getExpertise());
            if (employee.getExpertises() != null) {
                expertises.addAll(employee.getExpertises());
            }
            employee.setExpertises(expertises);

        }

        employeeRepository.save(employee);
    }

    //private helper methods
    //METHOD TO CHECK THE EXISTENCE OF AN ELEMENT AND RETURN THE DATA IF ITS VALID
    private Employee validateManager(Long managerId) {
        Optional<Employee> employeeManager = employeeRepository.findById(managerId);
        if (employeeManager.isEmpty()) {
            throw new BadRequestException("Manager Doesn't Exist");
        }
        return employeeManager.get();

    }

    private Department validateDepartment(String departmentName) {
        Optional<Department> employeeDepartment = departmentRepository.findByName(departmentName);
        if (employeeDepartment.isEmpty()) {
            throw new BadRequestException("Department Doesn't Exist");
        }
        return employeeDepartment.get();
    }

    private Team validateTeam(String teamName) {
        Optional<Team> employeeTeam = teamRepository.findByName(teamName);
        if (employeeTeam.isEmpty()) {
            throw new BadRequestException("Team Doesn't Exist");
        }
        return employeeTeam.get();
    }

    private List<Expertise> validateExpertise(List<String> requestExpertises) {
        List<Expertise> expertises = expertiseRepository.findAllByNameIn(requestExpertises);
        if (expertises.size() != requestExpertises.size()) {
            throw new BadRequestException("Expert Doesn't Exist");
        }
        return expertises;

    }
}
