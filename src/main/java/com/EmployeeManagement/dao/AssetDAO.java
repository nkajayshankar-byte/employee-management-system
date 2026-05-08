package com.EmployeeManagement.dao;

import com.EmployeeManagement.entity.Asset;
import java.util.List;
import java.util.Optional;

public interface AssetDAO {
    Asset save(Asset asset);
    Optional<Asset> findById(Long id);
    List<Asset> findAll();
    List<Asset> findByEmployeeId(Long employeeId);
    List<Asset> findByAssetNameContainingIgnoreCaseOrSerialNumberContainingIgnoreCase(String name, String serial);
    void deleteById(Long id);
    void deleteByEmployeeId(Long employeeId);
}
