package com.EmployeeManagement.dao;

import com.EmployeeManagement.entity.Announcement;
import java.util.List;

public interface AnnouncementDAO {
    int save(Announcement announcement);
    List<Announcement> findPendingAnnouncementsDue();
    int updateStatus(Long id, String status);
}
