package com.booklending.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.booklending.service.BookService;
import com.booklending.service.UserService;
import com.booklending.service.BorrowService;
import com.booklending.entity.User;
import java.util.List;

@Controller
public class IndexController {
    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BorrowService borrowService;

    @GetMapping("/")
    public String index(Model model) {
        try {
            logger.debug("Processing index page request");
            
            // 从数据库获取图书统计信息
            int totalBooks = bookService.getAllBooks().size();
            int availableBooks = bookService.getAllBooks().stream()
                .filter(book -> book.getAvailableCopies() > 0)
                .mapToInt(book -> book.getAvailableCopies())
                .sum();
            
            // 从数据库获取用户统计信息
            int totalUsers = userService.getAllUsers().size();
            
            // 从数据库获取借阅统计信息
            int currentBorrows = borrowService.getAllBorrowRecords().size();
            int overdueBooks = borrowService.getOverdueBorrowRecords().size();
            
            // 获取当前用户的借阅信息
            int myBorrows = 0;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getPrincipal().toString())) {
                String username = authentication.getName();
                User currentUser = userService.getUserByUsername(username);
                if (currentUser != null) {
                    myBorrows = borrowService.getBorrowRecordsByUser(currentUser.getId()).size();
                }
            }
            
            // 添加热门图书到模型
            model.addAttribute("popularBooks", bookService.getPopularBooks(5));
            
            // 将统计数据添加到模型
            model.addAttribute("totalBooks", totalBooks);
            model.addAttribute("availableBooks", availableBooks);
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("currentBorrows", currentBorrows);
            model.addAttribute("myBorrows", myBorrows);
            model.addAttribute("overdueBooks", overdueBooks);
            
            return "index";
        } catch (Exception e) {
            logger.error("Error processing index request", e);
            model.addAttribute("errorMessage", "An error occurred while loading the page: " + e.getMessage());
            model.addAttribute("totalBooks", 0);
            model.addAttribute("availableBooks", 0);
            model.addAttribute("totalUsers", 0);
            model.addAttribute("currentBorrows", 0);
            model.addAttribute("myBorrows", 0);
            model.addAttribute("overdueBooks", 0);
            model.addAttribute("popularBooks", List.of());
            return "index";
        }
    }
}