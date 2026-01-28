package org.example.hrsystem.Employee;

import com.gravity9.jsonpatch.mergepatch.JsonMergePatch;
import jakarta.validation.Valid;
import org.example.hrsystem.Employee.dto.EmployeeRequestDTO;
import org.example.hrsystem.Employee.dto.EmployeeResponseDTO;
import org.example.hrsystem.Employee.dto.EmployeeSalaryInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PatchMapping(path = "/{employeeId}", consumes = "application/merge-patch+json")
    public ResponseEntity<String> updateEmployee(
            @PathVariable Long employeeId,
            @RequestBody JsonMergePatch patch) throws Exception {

        employeeService.updateEmployee(employeeId, patch);
        return ResponseEntity.ok( SUCCESS_EMPLOYEE_DATA_UPDATED );
    }
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<String> deleteEmployee(
            @PathVariable Long employeeId
    ) {
       employeeService.deleteEmployee(employeeId);
        return ResponseEntity.ok( SUCCESS_EMPLOYEE_DELETED );
    }
}
