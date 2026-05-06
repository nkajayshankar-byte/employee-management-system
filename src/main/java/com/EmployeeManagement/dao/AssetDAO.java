package com.EmployeeManagement.dao;

import com.EmployeeManagement.entity.Asset;
import java.util.List;
import java.util.Optional;

public interface AssetDAO {
    Asset save(Asset asset);
    Optional<Asset> findById(String id);
    List<Asset> findAll();
    List<Asset> findByEmployeeId(String employeeId);
    List<Asset> findByAssetNameContainingIgnoreCaseOrSerialNumberContainingIgnoreCase(String name, String serial);
    void deleteById(String id);
    void deleteByEmployeeId(String employeeId);
}
