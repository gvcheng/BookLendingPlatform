package com.booklending.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import com.booklending.entity.User;
import com.booklending.service.UserService;

@Controller
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "用户名或密码错误，请重试");
        }
        return "login";
    }

    @PostMapping("/login")
    public String loginPost(@RequestParam String username, @RequestParam String password, Model model) {
        try {
            // 进行身份验证
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            // 将身份验证对象存储到安全上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            return "redirect:/";
        } catch (AuthenticationException e) {
            model.addAttribute("error", "用户名或密码错误，请重试");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout() {
        SecurityContextHolder.getContext().setAuthentication(null);
        return "redirect:/";
    }
    
    // 显示注册页面
    @GetMapping("/register")
    public String register() {
        return "register";
    }
    
    // 处理注册请求
    @PostMapping("/register")
    public String registerPost(@RequestParam String username, 
                              @RequestParam String password, 
                              @RequestParam String email, 
                              Model model) {
        try {
            // 检查用户名是否已存在
            if (userService.getUserByUsername(username) != null) {
                model.addAttribute("error", "用户名已存在，请选择其他用户名");
                return "register";
            }
            
            // 创建新用户
            User user = new User();
            user.setUsername(username);
            user.setPassword(password); // 注意：在实际项目中应该加密密码
            user.setEmail(email);
            user.setRole("USER"); // 普通用户角色
            user.setActive(true); // 默认激活用户
            
            // 保存用户
            userService.saveUser(user);
            
            // 注册成功后自动登录
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("error", "注册失败：" + e.getMessage());
            return "register";
        }
    }
}