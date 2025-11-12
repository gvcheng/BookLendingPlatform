package com.booklending.mapper;

import com.booklending.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    // 添加用户
    int insert(User user);
    
    // 更新用户
    int update(User user);
    
    // 删除用户
    int delete(Long id);
    
    // 根据ID查询用户
    User selectById(Long id);
    
    // 根据用户名查询用户
    User selectByUsername(String username);
    
    // 查询所有用户
    List<User> selectAll();
    
    // 更新用户密码
    int updatePassword(Long id, String password);
    
    // 更新用户状态
    int updateActiveStatus(Long id, boolean active);
    
    // 检查用户名是否存在
    int countByUsername(String username);
}