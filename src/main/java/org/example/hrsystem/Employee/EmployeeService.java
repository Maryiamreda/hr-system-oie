package org.example.hrsystem.Employee;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gravity9.jsonpatch.mergepatch.JsonMergePatch;
import jakarta.validation.Valid;
import org.example.hrsystem.Department.Department;
import org.example.hrsystem.Department.DepartmentRepository;
import org.example.hrsystem.Employee.dto.EmployeeRequestDTO;
import org.example.hrsystem.Employee.dto.EmployeeResponseDTO;
import org.example.hrsystem.Employee.dto.EmployeeSalaryInfoDTO;
import org.example.hrsystem.Expertise.Expertise;
import org.example.hrsystem.Expertise.ExpertiseRepository;
import org.example.hrsystem.LeaveRecord.LeaveRecord;
import org.example.hrsystem.LeaveRecord.LeaveRecordMapper;
import org.example.hrsystem.LeaveRecord.LeaveRepository;
import org.example.hrsystem.LeaveRecord.LeaveRequestDto;
import org.example.hrsystem.utilities.SalaryCalculator;
import org.example.hrsystem.Team.Team;
import org.example.hrsystem.Team.TeamRepository;
import org.example.hrsystem.exception.BadRequestException;
import org.example.hrsystem.exception.ConflictException;
import org.example.hrsystem.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;

import static org.example.hrsystem.utilities.EmployeeMessageConstants.*;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private LeaveRecordMapper leaveRecordMapper;
    @Autowired
    private ExpertiseRepository expertiseRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SalaryCalculator salaryCalculator;
    @Autowired
    private LeaveRepository leaveRepository;

    public EmployeeResponseDTO addEmployee(EmployeeRequestDTO employeeRequestDTO) {
        Employee employee = employeeMapper.toEntity(employeeRequestDTO);
        if (employeeRepository.existsByNationalId(employeeRequestDTO.getNationalId())) {
            throw new ConflictException(ERROR_NATIONAL_ID_EXISTS);
        }
        employee.setManager(getManagerOrThrow(employeeRequestDTO.getManagerId()));
        employee.setDepartment(getDepartmentOrThrow(employeeRequestDTO.getDepartmentName()));
        if (employeeRequestDTO.getTeamName() != null) {
            employee.setTeam(getTeamOrThrow(employeeRequestDTO.getTeamName()));
        }
        if (employeeRequestDTO.getExpertise() != null) {
            employee.setExpertises(getExpertiseOrThrow(employeeRequestDTO.getExpertise()));
        }

        Employee newEmployee = employeeRepository.save(employee);
        return employeeMapper.toResponse(newEmployee);
    }

    public EmployeeResponseDTO getEmployeeResponseDTO(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException(ERROR_EMPLOYEE_NOT_EXIST));

        return employeeMapper.toResponse(employee);
    }

    public EmployeeSalaryInfoDTO getEmployeeSalaryInfoDTO(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException(ERROR_EMPLOYEE_NOT_EXIST));
        BigDecimal grossSalary = employee.getGrossSalary();
        BigDecimal netSalary = salaryCalculator.calculateNetSalary(grossSalary);
        return EmployeeSalaryInfoDTO.builder().grossSalary(grossSalary).netSalary(netSalary).build();
    }

    public Page<EmployeeResponseDTO> getTeamEmployeesResponseList(String teamName, Pageable pageable) {
        Page<Employee> employeePage = employeeRepository.findByTeamName(teamName, pageable);
        return employeePage.map(employeeMapper::toResponse);
    }

    public Page<EmployeeResponseDTO> getDirectSubordinates(Long managerId, Pageable pageable) {
        Employee manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new NotFoundException(ERROR_MANAGER_NOT_EXIST));
        Page<Employee> directSubordinates = employeeRepository.findByManager(manager, pageable);
        return directSubordinates.map(employeeMapper::toResponse);
    }

    public List<EmployeeResponseDTO> getRecursiveSubordinates(Long managerId, Pageable pageable) {
        employeeRepository.findById(managerId)
                .orElseThrow(() -> new NotFoundException(ERROR_MANAGER_NOT_EXIST));
        List<Employee> recursiveSubordinates = employeeRepository.findRecursiveSubordinates(managerId, pageable);
        return recursiveSubordinates.stream().map(employeeMapper::toResponse).toList();

    }

    public void updateEmployee(Long employeeId, JsonMergePatch patch) throws Exception {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException(ERROR_EMPLOYEE_NOT_EXIST));

        EmployeeRequestDTO currentDto = employeeMapper.toDto(employee);
        JsonNode dtoNode = objectMapper.convertValue(currentDto, JsonNode.class);
        JsonNode patchedNode = patch.apply(dtoNode);
        EmployeeRequestDTO patchedDto = objectMapper.treeToValue(patchedNode, EmployeeRequestDTO.class);
        if (patchedDto.getFirstName() != null && patchedDto.getFirstName().length() > 2) {
            employee.setFirstName(patchedDto.getFirstName());
        }
        if (patchedDto.getLastName() != null && patchedDto.getLastName().length() > 2) {
            employee.setLastName(patchedDto.getLastName());
        }
        if (patchedDto.getNationalId() != null && !patchedDto.getNationalId().equals(employee.getNationalId())) {
            if (employeeRepository.existsByNationalId(patchedDto.getNationalId())) {
                throw new ConflictException(ERROR_NATIONAL_ID_EXISTS);
            }
            employee.setNationalId(patchedDto.getNationalId());
        }
        employee.setGender(patchedDto.getGender() == null ? null : String.valueOf(patchedDto.getGender()));
        employee.setDegree(patchedDto.getDegree());
        employee.setBirthDate(patchedDto.getBirthDate());
        employee.setYearsOfExperience(patchedDto.getYearsOfExperience());
        employee.setGraduationDate(patchedDto.getGraduationDate());
        if (patchedDto.getGrossSalary() != null && !Objects.equals(patchedDto.getGrossSalary(), employee.getGrossSalary())) {
            employee.setGrossSalary(patchedDto.getGrossSalary());
        }
        if (!Objects.equals(patchedDto.getManagerId(), employee.getManager().getId())) {
            employee.setManager(getManagerOrThrow(patchedDto.getManagerId()));
        }

        if (!Objects.equals(patchedDto.getDepartmentName(), employee.getDepartment().getName())) {
            employee.setDepartment(getDepartmentOrThrow(patchedDto.getDepartmentName()));
        }
        if (!Objects.equals(patchedDto.getTeamName(), employee.getTeam().getName())) {
            employee.setTeam(getTeamOrThrow(patchedDto.getTeamName()));
        }

        if (patchedDto.getExpertise() != null && !patchedDto.getExpertise().isEmpty()) {
            List<Expertise> expertises = getExpertiseOrThrow(patchedDto.getExpertise());
            if (employee.getExpertises() != null) {
                expertises.addAll(employee.getExpertises());
            }
            employee.setExpertises(expertises);
        } else {
            employee.setExpertises(null);
        }

        employeeRepository.save(employee);
    }

    @Transactional
    public void deleteEmployee(Long employeeId) {
        Employee employeeToDelete = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException(ERROR_EMPLOYEE_NOT_EXIST));
        Employee upperManager = employeeToDelete.getManager();

        if (upperManager == null) {
            throw new ConflictException(ERROR_DELETING_EXECUTIVE_EMPLOYEE);
        }
        employeeRepository.updateSubordinatesManager(employeeToDelete, upperManager);
        employeeRepository.delete(employeeToDelete);
    }

    //private helper methods
    //METHOD TO CHECK THE EXISTENCE OF AN ELEMENT AND RETURN THE DATA IF ITS VALID
    private Employee getManagerOrThrow(Long managerId) {
        if (managerId == null) {
            throw new BadRequestException(ERROR_MANAGER_NAME_EMPTY);
        }

        return employeeRepository.findById(managerId).orElseThrow(
                () -> new BadRequestException(ERROR_MANAGER_NOT_EXIST));
    }

    public LeaveRecord addEmployeeLeaveRequest(@Valid LeaveRequestDto leaveRequestDto, Long employeeId) {
        LeaveRecord leaveRecord = leaveRecordMapper.toEntity(leaveRequestDto);
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(()->new NotFoundException(ERROR_EMPLOYEE_NOT_EXIST));
        leaveRecord.setEmployee(employee);


        LocalDate current = leaveRequestDto.getStartDate();
        LocalDate endDate = leaveRequestDto.getStartDate().plusDays(leaveRequestDto.getDays());
        while (current.isBefore(endDate)) {
            if (current.getDayOfWeek() == DayOfWeek.FRIDAY || current.getDayOfWeek() == DayOfWeek.SATURDAY) {
                endDate = endDate.plusDays(1);
            }
            current = current.plusDays(1);
        }
        leaveRecord.setEndDate(endDate);

        Optional<LeaveRecord> record = leaveRepository.findLastRecordByEmployeeId(employeeId);
        if (record.isPresent()) {
            leaveRecord.setTotalLeaveDays(record.get().getTotalLeaveDays() + leaveRequestDto.getDays());
        } else {
            leaveRecord.setTotalLeaveDays(leaveRequestDto.getDays());
        }
        return leaveRepository.save(leaveRecord);

    }

    private Department getDepartmentOrThrow(String departmentName) {
        if (departmentName == null) {
            throw new BadRequestException(ERROR_DEPARTMENT_NAME_EMPTY);
        }
        return departmentRepository.findByName(departmentName).orElseThrow(
                () -> new BadRequestException(ERROR_DEPARTMENT_NOT_EXIST)
        );
    }

    private Team getTeamOrThrow(String teamName) {
        if (teamName == null) {
            throw new BadRequestException(ERROR_TEAM_NAME_EMPTY);
        }
        return teamRepository.findByName(teamName).orElseThrow(
                () -> new BadRequestException(ERROR_TEAM_NOT_EXIST));
    }

    private List<Expertise> getExpertiseOrThrow(List<String> requestExpertises) {
        List<Expertise> expertises = expertiseRepository.findAllByNameIn(requestExpertises);
        if (expertises.size() != requestExpertises.size()) {
            throw new BadRequestException(ERROR_EXPERT_NOT_EXIST);
        }
        return expertises;

    }


}
