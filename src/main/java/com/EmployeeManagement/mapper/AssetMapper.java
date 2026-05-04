package com.EmployeeManagement.mapper;

import com.EmployeeManagement.dto.AssetDTO;
import com.EmployeeManagement.entity.Asset;
import com.EmployeeManagement.entity.User;
import com.EmployeeManagement.dao.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AssetMapper {

    @Autowired
    private UserDAO userDAO;

    public AssetDTO toDTO(Asset asset) {
        if (asset == null) return null;

        AssetDTO dto = new AssetDTO();
        dto.setId(asset.getId());
        dto.setEmployeeId(asset.getEmployeeId());
        dto.setEmployeeName(asset.getEmployeeName());
        dto.setAssetName(asset.getAssetName());
        dto.setAssetType(asset.getAssetType());
        dto.setSerialNumber(asset.getSerialNumber());
        dto.setStatus(asset.getStatus());
        dto.setAssignedDate(asset.getAssignedDate());
        dto.setReturnDate(asset.getReturnDate());
        dto.setConditions(asset.getConditions());
        dto.setDescription(asset.getDescription());
        dto.setRemarks(asset.getRemarks());

        return dto;
    }

    public Asset toEntity(AssetDTO dto) {
        if (dto == null) return null;

        Asset asset = new Asset();
        asset.setId(dto.getId());
        asset.setEmployeeId(dto.getEmployeeId());

        if (dto.getEmployeeId() != null) {
            Optional<User> userOpt = userDAO.findById(dto.getEmployeeId());
            asset.setEmployeeName(
                userOpt.map(User::getName).orElse("Unknown")
            );
        }

        asset.setAssetName(dto.getAssetName());
        asset.setAssetType(dto.getAssetType());
        asset.setSerialNumber(dto.getSerialNumber());
        asset.setStatus(dto.getStatus());
        asset.setAssignedDate(dto.getAssignedDate());
        asset.setReturnDate(dto.getReturnDate());
        asset.setConditions(dto.getConditions());
        asset.setDescription(dto.getDescription());
        asset.setRemarks(dto.getRemarks());

        return asset;
    }
}
