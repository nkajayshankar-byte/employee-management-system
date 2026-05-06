package com.EmployeeManagement.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.EmployeeManagement.dto.CompanyDTO;
import com.EmployeeManagement.entity.Company;
import com.EmployeeManagement.entity.Location;
import com.EmployeeManagement.service.CompanyService;
import com.EmployeeManagement.service.FileStorageService;

@RestController
@RequestMapping("/api/company")
public class CompanyController {

    @Autowired private CompanyService service;

    @GetMapping
    public Map<String, Object> getCompany() {
        return service.getCompanyDetails();
    }

    @PostMapping
    public CompanyDTO save(@RequestBody CompanyDTO companyDto) {
        return service.saveCompany(companyDto);
    }

    @PostMapping("/location")
    public Company addLocation(@RequestBody Location loc) {
        return service.addLocation(loc);
    }
    
    @PostMapping("/locations/replace")
    public Company replaceLocations(@RequestBody List<Location> locations) {
        return service.updateLocations(locations);
    }

    @Autowired
    private FileStorageService fileStorageService;

    @DeleteMapping("/location/{id}")
    public void delete(@PathVariable String id) {
        service.deleteLocation(id);
    }

    @PostMapping("/upload-image")
    public Map<String, String> uploadImage(@RequestParam("file") MultipartFile file) {
        String url = fileStorageService.save(file);
        return Map.of("url", url);
    }
}
