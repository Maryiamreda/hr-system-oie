package org.example.hrsystem.Employee;

import com.gravity9.jsonpatch.mergepatch.JsonMergePatch;
import jakarta.validation.Valid;
import org.example.hrsystem.Employee.dto.EmployeeRequestDTO;
import org.example.hrsystem.Employee.dto.EmployeeResponseDTO;
import org.example.hrsystem.Employee.dto.EmployeeSalaryInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

import static org.example.hrsystem.utilities.EmployeeMessageConstants.*;


@RestController
@RequestMapping("/hr/api/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> addEmployee(@RequestBody @Valid EmployeeRequestDTO employee) {
        EmployeeResponseDTO employee1 = employeeService.addEmployee(employee);
        return new ResponseEntity<>(employee1, HttpStatus.CREATED);
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeInfo(
            @PathVariable Long employeeId
    ) {
        EmployeeResponseDTO employee = employeeService.getEmployeeResponseDTO(employeeId);
        return new ResponseEntity<>(employee, HttpStatus.OK);
    }

    @GetMapping("/{employeeId}/salary")
    public ResponseEntity<EmployeeSalaryInfoDTO> getEmployeeSalaryInfo(
            @PathVariable Long employeeId
    ) {
        EmployeeSalaryInfoDTO employee = employeeService.getEmployeeSalaryInfoDTO(employeeId);
        return new ResponseEntity<>(employee, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeResponseDTO>> getTeamEmployees(
            @RequestParam String teamName,
            @PageableDefault(size = 3) Pageable pageable
    ) {
        Page<EmployeeResponseDTO> teamEmployees = employeeService.getTeamEmployeesResponseList(teamName, pageable);
        return new ResponseEntity<>(teamEmployees, HttpStatus.OK);
    }

    @GetMapping("/{managerId}/subordinates")
    public ResponseEntity<Page<EmployeeResponseDTO>> getManagerDirectSubordinates(
            @PathVariable Long managerId,
            @PageableDefault(size = 3) Pageable pageable
    ) {
        Page<EmployeeResponseDTO> subordinates = employeeService.getDirectSubordinates(managerId, pageable);
        return new ResponseEntity<>(subordinates, HttpStatus.OK);
    }

    //    @GetMapping("/{managerId}/hierarchy")
//    public ResponseEntity<Page<EmployeeResponseDTO>> getManagerRecursiveSubordinates(
//            @PathVariable Long managerId,
//            @PageableDefault(size = 3) Pageable pageable
//    ) {
//        Page<EmployeeResponseDTO> subordinates = employeeService.getRecursiveSubordinates(managerId,pageable);
//        return new ResponseEntity<>(subordinates,HttpStatus.OK);
//    }
    @GetMapping("/{managerId}/hierarchy")
    public ResponseEntity<List<Employee>> getManagerRecursiveSubordinates(
            @PathVariable Long managerId,
            @PageableDefault(size = 3) Pageable pageable
    ) {
        List<Employee> subordinates = employeeService.getRecursiveSubordinates(managerId, pageable);
        return new ResponseEntity<>(subordinates, HttpStatus.OK);
    }

    @PatchMapping(path = "/{employeeId}", consumes = "application/merge-patch+json")
    public ResponseEntity<String> updateEmployee(
            @PathVariable Long employeeId,
            @RequestBody JsonMergePatch patch) throws Exception {

        employeeService.updateEmployee(employeeId, patch);
        return ResponseEntity.ok(SUCCESS_EMPLOYEE_DATA_UPDATED);
    }

    @DeleteMapping("/{employeeId}")
    public ResponseEntity<String> deleteEmployee(
            @PathVariable Long employeeId
    ) {
        employeeService.deleteEmployee(employeeId);
        return ResponseEntity.ok(SUCCESS_EMPLOYEE_DELETED);
    }
}
