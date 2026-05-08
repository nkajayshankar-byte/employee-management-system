package com.EmployeeManagement.dao.impl;

import com.EmployeeManagement.dao.AssetDAO;
import com.EmployeeManagement.entity.Asset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class AssetDAOImpl implements AssetDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Asset> rowMapper = (rs, rowNum) -> {
        Asset asset = new Asset();
        asset.setId(rs.getLong("id"));
        asset.setEmployeeId(rs.getObject("employeeId", Long.class));
        asset.setAssetName(rs.getString("assetName"));
        asset.setAssetType(rs.getString("assetType"));
        asset.setSerialNumber(rs.getString("serialNumber"));
        asset.setStatus(rs.getString("status"));
        asset.setAssignedDate(rs.getTimestamp("assignedDate") != null ? rs.getTimestamp("assignedDate").toLocalDateTime() : null);
        asset.setReturnDate(rs.getTimestamp("returnDate") != null ? rs.getTimestamp("returnDate").toLocalDateTime() : null);
        asset.setConditions(rs.getString("conditions"));
        asset.setDescription(rs.getString("description"));
        asset.setRemarks(rs.getString("remarks"));
        asset.setCreatedAt(rs.getTimestamp("createdAt") != null ? rs.getTimestamp("createdAt").toLocalDateTime() : null);
        asset.setUpdatedAt(rs.getTimestamp("updatedAt") != null ? rs.getTimestamp("updatedAt").toLocalDateTime() : null);
        
        // Joined field
        asset.setEmployeeName(rs.getString("employeeName"));
        
        return asset;
    };

    private final String BASE_SELECT = "SELECT a.*, u.name AS employeeName " +
                                       "FROM assets a " +
                                       "LEFT JOIN users u ON a.employeeId = u.id ";

    @Override
    public Asset save(Asset asset) {
        if (asset.getId() == null) {
            String sql = "INSERT INTO assets (employeeId, assetName, assetType, serialNumber, status, assignedDate, returnDate, conditions, description, remarks, createdAt, updatedAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setObject(1, asset.getEmployeeId());
                ps.setString(2, asset.getAssetName());
                ps.setString(3, asset.getAssetType());
                ps.setString(4, asset.getSerialNumber());
                ps.setString(5, asset.getStatus());
                ps.setObject(6, asset.getAssignedDate());
                ps.setObject(7, asset.getReturnDate());
                ps.setString(8, asset.getConditions());
                ps.setString(9, asset.getDescription());
                ps.setString(10, asset.getRemarks());
                ps.setObject(11, asset.getCreatedAt());
                ps.setObject(12, asset.getUpdatedAt());
                return ps;
            }, keyHolder);

            asset.setId(keyHolder.getKey().longValue());
        } else {
            String sql = "UPDATE assets SET employeeId = ?, assetName = ?, assetType = ?, serialNumber = ?, status = ?, assignedDate = ?, returnDate = ?, conditions = ?, description = ?, remarks = ?, updatedAt = ? WHERE id = ?";
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
                asset.getId()
            );
        }
        return asset;
    }

    @Override
    public Optional<Asset> findById(Long id) {
        String sql = BASE_SELECT + "WHERE a.id = ?";
        try {
            Asset asset = jdbcTemplate.queryForObject(sql, rowMapper, id);
            return Optional.ofNullable(asset);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Asset> findAll() {
        String sql = BASE_SELECT;
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public List<Asset> findByEmployeeId(Long employeeId) {
        String sql = BASE_SELECT + "WHERE a.employeeId = ?";
        return jdbcTemplate.query(sql, rowMapper, employeeId);
    }

    @Override
    public List<Asset> findByAssetNameContainingIgnoreCaseOrSerialNumberContainingIgnoreCase(String name, String serial) {
        String sql = BASE_SELECT + "WHERE LOWER(a.assetName) LIKE ? OR LOWER(a.serialNumber) LIKE ?";
        String searchPattern = "%" + name.toLowerCase() + "%";
        return jdbcTemplate.query(sql, rowMapper, searchPattern, searchPattern);
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM assets WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void deleteByEmployeeId(Long employeeId) {
        String sql = "DELETE FROM assets WHERE employeeId = ?";
        jdbcTemplate.update(sql, employeeId);
    }
}
