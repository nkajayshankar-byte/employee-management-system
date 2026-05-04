package com.EmployeeManagement.dao.impl;

import com.EmployeeManagement.dao.AssetDAO;
import com.EmployeeManagement.entity.Asset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AssetDAOImpl implements AssetDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Asset save(Asset asset) {
        if (asset.getId() == null || asset.getId().isEmpty()) {
            asset.setId(UUID.randomUUID().toString());
            String sql = "INSERT INTO assets (id, employeeId, assetName, assetType, serialNumber, status, assignedDate, returnDate, conditions, description, remarks, createdAt, updatedAt, employeeName) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, 
                asset.getId(), 
                asset.getEmployeeId(), 
                asset.getAssetName(), 
                asset.getAssetType(), 
                asset.getSerialNumber(), 
                asset.getStatus(), 
                asset.getAssignedDate(), 
                asset.getReturnDate(), 
                asset.getConditions(), 
                asset.getDescription(), 
                asset.getRemarks(), 
                asset.getCreatedAt(), 
                asset.getUpdatedAt(), 
                asset.getEmployeeName()
            );
        } else {
            String sql = "UPDATE assets SET employeeId = ?, assetName = ?, assetType = ?, serialNumber = ?, status = ?, assignedDate = ?, returnDate = ?, conditions = ?, description = ?, remarks = ?, updatedAt = ?, employeeName = ? WHERE id = ?";
            jdbcTemplate.update(sql, 
                asset.getEmployeeId(), 
                asset.getAssetName(), 
                asset.getAssetType(), 
                asset.getSerialNumber(), 
                asset.getStatus(), 
                asset.getAssignedDate(), 
                asset.getReturnDate(), 
                asset.getConditions(), 
                asset.getDescription(), 
                asset.getRemarks(), 
                asset.getUpdatedAt(), 
                asset.getEmployeeName(), 
                asset.getId()
            );
        }
        return asset;
    }

    @Override
    public Optional<Asset> findById(String id) {
        String sql = "SELECT * FROM assets WHERE id = ?";
        try {
            Asset asset = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Asset.class), id);
            return Optional.ofNullable(asset);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Asset> findAll() {
        String sql = "SELECT * FROM assets";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Asset.class));
    }

    @Override
    public List<Asset> findByEmployeeId(String employeeId) {
        String sql = "SELECT * FROM assets WHERE employeeId = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Asset.class), employeeId);
    }

    @Override
    public List<Asset> findByAssetNameContainingIgnoreCaseOrSerialNumberContainingIgnoreCase(String name, String serial) {
        String sql = "SELECT * FROM assets WHERE LOWER(assetName) LIKE LOWER(?) OR LOWER(serialNumber) LIKE LOWER(?)";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Asset.class), "%" + name + "%", "%" + serial + "%");
    }

    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM assets WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
