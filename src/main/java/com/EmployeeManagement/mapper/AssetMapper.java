package com.EmployeeManagement.mapper;

import com.EmployeeManagement.dto.AssetDTO;
import com.EmployeeManagement.entity.Asset;
import org.springframework.stereotype.Component;

@Component
public class AssetMapper {

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
        // employeeName is not set here because it's a transient virtual field populated by DAO JOIN
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
