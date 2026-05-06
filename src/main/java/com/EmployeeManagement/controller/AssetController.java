package com.EmployeeManagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.EmployeeManagement.dto.AssetDTO;
import com.EmployeeManagement.service.AssetService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/assets")
public class AssetController {

    @Autowired
    private AssetService service;

    @GetMapping
    public ResponseEntity<List<AssetDTO>> getAll() {
        return ResponseEntity.ok(service.getAllAssets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetDTO> getById(@PathVariable String id) {
        AssetDTO dto = service.getAssetById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<?> getByEmployee(@PathVariable String employeeId) {
        try {
            return ResponseEntity.ok(service.getAssetsByEmployee(employeeId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/search/{term}")
    public ResponseEntity<List<AssetDTO>> search(@PathVariable String term) {
        return ResponseEntity.ok(service.searchAssets(term));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody AssetDTO dto) {
        service.addAsset(dto);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Asset Created");
        response.put("status", true);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable String id, @RequestBody AssetDTO dto) {
        dto.setId(id);
        service.updateAsset(dto);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Asset Updated");
        response.put("status", true);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String id) {
        service.deleteAsset(id);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Asset Deleted");
        response.put("status", true);

        return ResponseEntity.ok(response);
    }
}