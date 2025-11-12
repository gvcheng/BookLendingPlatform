package com.booklending.service.impl;

import com.booklending.entity.User;
import com.booklending.mapper.UserMapper;
import com.booklending.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<User> getAllUsers() {
        return userMapper.selectAll();
    }

    @Override
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    @Transactional
    public User saveUser(User user) {
        if (user.getId() == null) {
            // 新增用户
            user.setCreatedAt(new Date());
            user.setUpdatedAt(new Date());
            if (user.getActive() == null) {
                user.setActive(true);
            }
            if (user.getRole() == null || user.getRole().isEmpty()) {
                user.setRole("USER");
            }
            if (user.getBalance() == null) {
                user.setBalance(0.0);
            }
            userMapper.insert(user);
            return user;
        } else {
            // 更新用户
            user.setUpdatedAt(new Date());
            userMapper.update(user);
            return userMapper.selectById(user.getId());
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        userMapper.delete(id);
    }

    @Override
    @Transactional
    public void updateUserStatus(Long id, boolean active) {
        userMapper.updateActiveStatus(id, active);
    }
}