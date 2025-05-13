package com.atudw.demo.service;

import com.atudw.demo.entity.User;
import com.atudw.demo.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public List<User> findUsersVulnerable(String username) {
        String sql = "SELECT * FROM users WHERE username = '" + username + "'";
        return entityManager.createNativeQuery(sql, User.class).getResultList();
    }

    // Safe method - uses the parameterized query
    public List<User> findUsersSafe(String username) {
        return userRepository.findByUsernameSafe(username);
    }
}