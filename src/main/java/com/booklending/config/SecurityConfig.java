package com.booklending.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import com.booklending.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserService userService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 暂时使用明文密码验证以便测试
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword.toString();
            }
            
            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return rawPassword.toString().equals(encodedPassword);
            }
        };
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            com.booklending.entity.User user = userService.getUserByUsername(username);
            if (user == null) {
                throw new org.springframework.security.core.userdetails.UsernameNotFoundException("用户不存在");
            }
            
            org.springframework.security.core.userdetails.User.UserBuilder builder = 
                org.springframework.security.core.userdetails.User.withUsername(username)
                    .password(user.getPassword())
                    .roles(user.getRole());
            
            return builder.build();
        };
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
    }
    
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                // 允许所有人访问首页、登录页、注册页、可借图书列表、健康检查和plain接口
                .antMatchers("/", "/login", "/register", "/book/available", "/health", "/simple", "/plain", "/css/**", "/js/**", "/images/**").permitAll()
                // 允许已登录用户查看书籍详情和归还自己的书籍
                .antMatchers("/book/detail/**", "/borrow/return").authenticated()
                // 允许已登录用户访问个人中心
                .antMatchers("/user/profile", "/user/profile/update").authenticated()
                // 需要管理员权限的接口（排除个人中心相关路径）
                .antMatchers("/user/list", "/user/status", "/user/update-status", "/user/edit/**", "/user/view/**", "/user/profile/{id}", "/book/add", "/book/edit/**", "/book/delete/**", "/borrow/overdue", "/borrow/list", "/borrow/edit/**", "/borrow/delete/**", "/borrow/search").hasRole("ADMIN")
                // 需要登录但不需要管理员权限的接口（用户借阅图书、查看个人借阅记录、图书列表和搜索）
                .antMatchers("/borrow/borrow-book", "/borrow/userBorrows", "/book/list", "/book/search", "/book/available/search").authenticated()
                // 其他接口默认拒绝访问，确保安全性
                .anyRequest().denyAll()
            .and()
                .formLogin()
                    .loginPage("/login")
                    .defaultSuccessUrl("/")
                    .failureUrl("/login?error=true")
                    .permitAll()
            .and()
                .logout()
                    .logoutSuccessUrl("/")
                    .permitAll()
            .and()
                .csrf().disable(); // 简化开发，实际项目中应该启用
    }
}