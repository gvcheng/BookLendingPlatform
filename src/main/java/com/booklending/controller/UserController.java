package com.booklending.controller;

import com.booklending.entity.User;
import com.booklending.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 查看所有用户（管理员功能）
    @GetMapping("/list")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "user/list";
    }

    // 查看用户详情
    @GetMapping("/view/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "user/view";
    }

    // 更新用户状态（启用/禁用）
    @PostMapping("/update-status")
    public String updateUserStatus(@RequestParam Long userId, @RequestParam Boolean active, Model model) {
        try {
            userService.updateUserStatus(userId, active);
            model.addAttribute("message", "用户状态更新成功！");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/user/list";
    }
    
    // 显示个人中心页面（当前登录用户）
    @GetMapping("/profile")
    public String showProfile(Model model) {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        
        if (user == null) {
            model.addAttribute("error", "用户信息不存在！");
            return "redirect:/";
        }
        
        model.addAttribute("user", user);
        return "user/edit";
    }
    
    // 根据ID查看用户profile（管理员功能）
    @GetMapping("/profile/{id}")
    public String showProfileById(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        
        if (user == null) {
            model.addAttribute("error", "用户信息不存在！");
            return "redirect:/user/list";
        }
        
        model.addAttribute("user", user);
        return "user/edit";
    }
    
    // 更新用户信息（支持管理员编辑和用户自编辑）
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam Long id, 
                              @RequestParam String username, 
                              @RequestParam String email, 
                              @RequestParam String phone,
                              @RequestParam(required = false) Double balance,
                              @RequestParam String password, 
                              @RequestParam String confirmPassword, 
                              Model model) {
        try {
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
                return "redirect:/login";
            }
            
            String currentUsername = authentication.getName();
            User currentUser = userService.getUserByUsername(currentUsername);
            
            // 获取要编辑的用户
            User targetUser = userService.getUserById(id);
            if (targetUser == null) {
                model.addAttribute("error", "用户不存在！");
                return "redirect:/user/list";
            }
            
            // 权限检查：只能编辑自己的信息，除非是管理员
            boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin && !currentUser.getId().equals(id)) {
                model.addAttribute("error", "无权修改此用户信息！");
                model.addAttribute("user", targetUser);
                return "user/edit";
            }
            
            // 更新用户信息
            // 管理员可以修改用户名和余额
            if (isAdmin) {
                targetUser.setUsername(username);
                if (balance != null) {
                    targetUser.setBalance(balance);
                }
            }
            targetUser.setEmail(email);
            targetUser.setPhone(phone);
            
            // 如果填写了密码，则更新密码
            if (password != null && !password.isEmpty()) {
                if (!password.equals(confirmPassword)) {
                    model.addAttribute("error", "两次输入的密码不一致！");
                    model.addAttribute("user", targetUser);
                    return "user/edit";
                }
                targetUser.setPassword(password); // 注意：在实际项目中应该加密密码
            }
            
            // 保存更新
            userService.saveUser(targetUser);
            model.addAttribute("message", "用户信息更新成功！");
            model.addAttribute("user", targetUser);
        } catch (Exception e) {
            model.addAttribute("error", "更新失败：" + e.getMessage());
            // 重新查询用户信息以显示到页面
            User user = userService.getUserById(id);
            model.addAttribute("user", user);
        }
        
        return "user/edit";
    }
}