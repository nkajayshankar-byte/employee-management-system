package com.EmployeeManagement.controller;

import com.EmployeeManagement.dto.EmployeeDTO;
import com.EmployeeManagement.service.EmployeeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "http://localhost:4200")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getUsers(@RequestParam(required = false) String role) {
        return ResponseEntity.ok(employeeService.getUsersByRole(role));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getById(@PathVariable String id) {

        EmployeeDTO dto = employeeService.getEmployeeById(id);

        return (dto == null)
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(dto);
    }

    // ---------------- GET BY EMAIL ----------------
    @GetMapping("/email/{email}")
    public ResponseEntity<EmployeeDTO> getByEmail(@PathVariable String email) {

        EmployeeDTO dto = employeeService.getEmployeeByEmail(email);

        return (dto == null)
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(dto);
    }

    @GetMapping("/search/{term}")
    public ResponseEntity<List<EmployeeDTO>> search(@PathVariable String term) {
        return ResponseEntity.ok(employeeService.searchEmployees(term));
    }

    @PostMapping
    public ResponseEntity<EmployeeDTO> add(@RequestBody EmployeeDTO dto) {
        return ResponseEntity.ok(employeeService.addEmployee(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> update(@PathVariable String id,
                                              @RequestBody EmployeeDTO dto) {

        EmployeeDTO updated = employeeService.updateEmployee(id, dto);

        return (updated == null)
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {

        return employeeService.deleteEmployee(id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<Void> bulkDelete(@RequestBody List<String> ids) {
        employeeService.bulkDeleteEmployees(ids);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/upload-image")
    public ResponseEntity<String> uploadImage(@PathVariable String id,
                                              @RequestParam MultipartFile file)
            throws IOException {

        String url = employeeService.uploadImage(id, file);

        return (url == null)
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(url);
    }
}