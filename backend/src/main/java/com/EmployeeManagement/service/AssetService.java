package com.EmployeeManagement.service;

import com.EmployeeManagement.dto.AssetDTO;
import com.EmployeeManagement.entity.Asset;
import com.EmployeeManagement.entity.User;
import com.EmployeeManagement.mapper.AssetMapper;
import com.EmployeeManagement.dao.AssetDAO;
import com.EmployeeManagement.dao.UserDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class AssetService {

    @Autowired
    private AssetDAO assetDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private AssetMapper assetMapper;

    public void addAsset(AssetDTO dto) {

        Asset asset = assetMapper.toEntity(dto);
  
        if (asset.getAssignedDate() == null) {
            asset.setAssignedDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        }

        if (asset.getConditions() == null) {
            asset.setConditions("Good");
        }

        asset.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        asset.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

        assetDAO.save(asset);
    }

    public List<AssetDTO> getAllAssets() {

        List<Asset> assets = assetDAO.findAll();
        List<AssetDTO> dtoList = new ArrayList<>();

        for (Asset asset : assets) {
            dtoList.add(assetMapper.toDTO(asset));
        }

        return dtoList;
    }

    public AssetDTO getAssetById(String id) {
        Optional<Asset> assetOpt = assetDAO.findById(id);
        return assetOpt.map(assetMapper::toDTO).orElse(null);
    }

    public List<AssetDTO> getAssetsByEmployee(String empId) {

        List<Asset> assets = assetDAO.findByEmployeeId(empId);
        List<AssetDTO> dtoList = new ArrayList<>();

        for (Asset asset : assets) {
            dtoList.add(assetMapper.toDTO(asset));
        }

        return dtoList;
    }

    public void updateAsset(AssetDTO dto) {

        Optional<Asset> optional = assetDAO.findById(dto.getId());
        if (optional.isEmpty()) return;

        Asset existing = optional.get();

        existing.setEmployeeId(dto.getEmployeeId());

        if (dto.getEmployeeId() != null) {
            Optional<User> userOpt = userDAO.findById(dto.getEmployeeId());
            existing.setEmployeeName(
                userOpt.map(User::getName).orElse("Unknown")
            );
        }

        existing.setAssetName(dto.getAssetName());
        existing.setAssetType(dto.getAssetType());
        existing.setSerialNumber(dto.getSerialNumber());
        existing.setStatus(dto.getStatus());

        if (dto.getAssignedDate() != null) {
            existing.setAssignedDate(dto.getAssignedDate());
        }

        existing.setReturnDate(dto.getReturnDate());

        if (dto.getConditions() != null) {
            existing.setConditions(dto.getConditions());
        }

        existing.setRemarks(dto.getRemarks());

        existing.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

        assetDAO.save(existing);
    }

    public void deleteAsset(String id) {
        assetDAO.deleteById(id);
    }

    public List<AssetDTO> searchAssets(String term) {

        List<Asset> assets = assetDAO.findAll();
        List<AssetDTO> dtoList = new ArrayList<>();

        String search = term.toLowerCase();

        for (Asset asset : assets) {

            boolean match =
                    (asset.getAssetName() != null && asset.getAssetName().toLowerCase().contains(search)) ||
                    (asset.getSerialNumber() != null && asset.getSerialNumber().toLowerCase().contains(search));

            if (match) {
                dtoList.add(assetMapper.toDTO(asset));
            }
        }

        return dtoList;
    }
}