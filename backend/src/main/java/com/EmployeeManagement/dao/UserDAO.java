package com.EmployeeManagement.dao;

import com.EmployeeManagement.entity.Role;
import com.EmployeeManagement.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserDAO {
    User save(User user);
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findAll();
    long countByRole(Role role);
    List<User> findByRole(Role role);
    void deleteById(String id);
}
