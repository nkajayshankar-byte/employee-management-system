package com.EmployeeManagement.dao.impl;

import com.EmployeeManagement.dao.AnnouncementDAO;
import com.EmployeeManagement.entity.Announcement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class AnnouncementDAOImpl implements AnnouncementDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final class AnnouncementRowMapper implements RowMapper<Announcement> {
        @Override
        public Announcement mapRow(ResultSet rs, int rowNum) throws SQLException {
            Announcement announcement = new Announcement();
            announcement.setId(rs.getLong("id"));
            announcement.setSubject(rs.getString("subject"));
            announcement.setContent(rs.getString("content"));
            announcement.setScheduledTime(rs.getTimestamp("scheduled_time").toLocalDateTime());
            announcement.setStatus(rs.getString("status"));
            announcement.setTargetAudience(rs.getString("target_audience"));
            return announcement;
        }
    }

    @Override
    public int save(Announcement announcement) {
        String sql = "INSERT INTO announcement (subject, content, scheduled_time, status, target_audience) VALUES (?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql, announcement.getSubject(), announcement.getContent(),
                announcement.getScheduledTime(), announcement.getStatus(), announcement.getTargetAudience());
    }

    @Override
    public List<Announcement> findPendingAnnouncementsDue() {
        String sql = "SELECT * FROM announcement WHERE status = 'PENDING' AND scheduled_time <= NOW()";
        return jdbcTemplate.query(sql, new AnnouncementRowMapper());
    }

    @Override
    public int updateStatus(Long id, String status) {
        String sql = "UPDATE announcement SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, id);
    }
}
