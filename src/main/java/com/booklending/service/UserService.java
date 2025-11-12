package com.booklending.service;

import com.booklending.entity.User;
import java.util.List;

public interface UserService {
    // 查询所有用户
    List<User> getAllUsers();
    
    // 根据ID查询用户
    User getUserById(Long id);
    
    // 根据用户名查询用户
    User getUserByUsername(String username);
    
    // 保存用户
    User saveUser(User user);
    
    // 删除用户
    void deleteUser(Long id);
    
    // 更新用户状态
    void updateUserStatus(Long id, boolean active);
}